package org.gmetais.bleumagique

import android.arch.lifecycle.MutableLiveData
import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.*

private const val TAG = "BLE/BlueController"
private const val MB_CONTROL_SERVICE = "0000ffe5-0000-1000-8000-00805f9b34fb"
private const val MB_CONTROL_CHAR = "0000ffe9-0000-1000-8000-00805f9b34fb"
private const val MB_OFF = "CC2433"
private const val MB_ON = "CC2333"
private const val MB_DESCRIPTION_REQ = "EF0177"

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

    private fun send(command: String) = ctrlCharacteristic?.let {
        it.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        Log.d(TAG, "send $command:  ${btGatt?.writeCharacteristic(it.apply { value = hexStringToByteArray(command) })}")
    }

    fun toggle() = send(if (state.value?.on == true) MB_OFF else MB_ON)
    fun setTemp(temp: Int) = send("56FF0000${temp.toHexByte()}0faa")

    private val gattCb = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED && btGatt?.discoverServices() == true)
            else connected.postValue(false)
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
            send(MB_DESCRIPTION_REQ)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when(characteristic?.value?.toHexString()) {
                    MB_OFF -> state.update(false)
                    MB_ON -> state.update(true)
                }
            }
            gatt!!.readCharacteristic(characteristic!!)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val array = characteristic.value
            val on = array[2].compareTo(BYTE_VALUE_ON) == 0
            val temp = array[9].toInt()
            Log.d(TAG, "onCharacteristicChanged $temp")
            state.update(on, temp)
        }

    }

    init {
        btAdapter = (appCtx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        val device = btAdapter?.getRemoteDevice("F8:1D:78:63:4D:34")
        btGatt = device?.connectGatt(appCtx, true, gattCb)
    }

    fun clear() = btGatt?.close() ?: Unit
}