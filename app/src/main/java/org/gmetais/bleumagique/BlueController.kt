package org.gmetais.bleumagique

import android.arch.lifecycle.MutableLiveData
import android.bluetooth.*
import android.content.Context
import android.util.Log
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

private const val TAG = "BLE/BlueController"
private const val MB_CONTROL_SERVICE = "0000ffe5-0000-1000-8000-00805f9b34fb"
private const val MB_CONTROL_CHAR = "0000ffe9-0000-1000-8000-00805f9b34fb"
private const val MB_OFF = "CC2433"
private const val MB_ON = "CC2333"

private const val MB_DESCRIPTION_REQ = "EF0177"
private const val MB_TIME_REQ = "121A1B21"
private val MB_REQ_ARRAY = arrayListOf(MB_DESCRIPTION_REQ, MB_TIME_REQ)
private var reqCount = 0

private const val MB_NOTIFY_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb"
private const val MB_NOTIFY_CHAR = "0000ffe4-0000-1000-8000-00805f9b34fb"
private const val MB_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb"

private const val BYTE_VALUE_ON = 35

class BlueController(appCtx: Context, private val state: LiveState, private val connected: MutableLiveData<Boolean>) {

    private var btGatt : BluetoothGatt? = null
    private var btAdapter : BluetoothAdapter? = null
    private var ctrlService : BluetoothGattService? = null
    private var notifyService : BluetoothGattService? = null
    private var notifyCharacteristic : BluetoothGattCharacteristic? = null
    private var ctrlCharacteristic : BluetoothGattCharacteristic? = null

    fun request(req: String) = commander.offer(Request(req))

    fun toggle() = commander.offer(Power(state.value?.on != true))

    fun setTemp(temp: Int) {
        commander.offer(Temp(max(0, min(temp, 255))))
        state.update(temp = temp)
    }

    private val gattCb = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED && gatt.discoverServices())
            else {
                connected.postValue(false)
                return
            }
            state.value?.name = gatt.device.name
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            ctrlService = gatt.getService(UUID.fromString(MB_CONTROL_SERVICE))
            ctrlCharacteristic = ctrlService?.getCharacteristic(UUID.fromString(MB_CONTROL_CHAR))
            notifyService = gatt.getService(UUID.fromString(MB_NOTIFY_SERVICE))
            notifyCharacteristic = notifyService?.getCharacteristic(UUID.fromString(MB_NOTIFY_CHAR))
            notifyCharacteristic?.let {
                gatt.setCharacteristicNotification(it, true)
                val descriptor = it.getDescriptor(UUID.fromString(MB_DESCRIPTOR))
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
                connected.postValue(true)
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            request(MB_REQ_ARRAY[reqCount])
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when(characteristic.value.toHexString()) {
                    MB_OFF -> state.update(false)
                    MB_ON -> state.update(true)
                }
            }
            gatt.readCharacteristic(characteristic)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val array = characteristic.value
            if (array.size == 12 && array[0].compareTo(0x66) == 0/* && array[11].compareTo(0x99) == 0*/) {
                val on = array[2].compareTo(BYTE_VALUE_ON) == 0
                val temp = array[9].toUInt()
                state.update(on, temp)
            } else if (array.size == 11 && array[0].compareTo(0x13) == 0 && array[10].compareTo(0x31) == 0) {
                val year = 2000 + array[2].toUInt()
                val month = array[3].toUInt()
                val day = array[4].toUInt()
                val hour = array[5].toUInt()
                val minute = array[6].toUInt()
                val second = array[7].toUInt()
                val dayOfWeek = array[8].toUInt()
                Log.d(TAG, "$hour:$minute:$second, $day/$month/$year\n day of week: $dayOfWeek")
            }
            if (++reqCount < MB_REQ_ARRAY.size) request(MB_REQ_ARRAY[reqCount])
        }

    }

    init {
        btAdapter = (appCtx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        val device = btAdapter?.getRemoteDevice("F8:1D:78:63:4D:34")
        btGatt = device?.connectGatt(appCtx, true, gattCb)
    }

    fun clear() = btGatt?.close() ?: Unit

    private val commander = actor<Command>(UI, Channel.CONFLATED) {
        for (c in channel) {
            when (c) {
                is Request -> send(c.req)
                is Power -> send(if (c.on) MB_ON else MB_OFF)
                is Temp -> send("56000000${c.temp.toHexByte()}0Faa")
            }
            delay(100, TimeUnit.MILLISECONDS)
        }
    }

    private fun send(command: String) = ctrlCharacteristic?.let {
        val succes = btGatt?.writeCharacteristic(it.apply { value = command.toByteArray() })
        Log.d(TAG, "send $command:  $succes")
    }
}

sealed class Command
class Power(val on: Boolean) : Command()
class Temp(val temp: Int) : Command()
class Request(val req: String) : Command()