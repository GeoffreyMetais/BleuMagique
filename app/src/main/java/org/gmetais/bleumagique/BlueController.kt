package org.gmetais.bleumagique

import android.arch.lifecycle.MutableLiveData
import android.bluetooth.*
import android.content.Context
import java.util.*

private const val TAG = "BLE/BlueController"
private const val MB_OFF = "CC2433"
private const val MB_ON = "CC2333"
private const val MB_SERVICE = "0000ffe5-0000-1000-8000-00805f9b34fb"

private const val MB_CONTROL = "0000ffe9-0000-1000-8000-00805f9b34fb"
private const val MB_NOTIFY = "0000ffe4-0000-1000-8000-00805f9b34fb"
private const val CONFIG = "00002902-0000-1000-8000-00805f9b34fb"

class BlueController(appCtx: Context, private val isOn: MutableLiveData<Boolean>, private val connected: MutableLiveData<Boolean>) {

    private var btGatt : BluetoothGatt? = null
    private var btAdapter : BluetoothAdapter? = null
    private var service : BluetoothGattService? = null
    private var characteristic : BluetoothGattCharacteristic? = null

    private fun send(command: String) = characteristic?.let {
        btGatt?.writeCharacteristic(it.apply { value = hexStringToByteArray(command) })
    }

    fun toggle() = send(if (isOn.value == true) MB_OFF else MB_ON)
    fun setTemp(temp: Int) = send("56FF0000${temp.toHexByte()}0faa")

    private val gattCb = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED && btGatt?.discoverServices() == true)
            else connected.postValue(false)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            service = gatt?.getService(UUID.fromString(MB_SERVICE))
            characteristic = service?.getCharacteristic(UUID.fromString(MB_CONTROL))
            characteristic?.let {
                gatt?.setCharacteristicNotification(it, true)
                connected.postValue(true)
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when(characteristic?.value?.toHexString()) {
                    MB_OFF -> isOn.postValue(false)
                    MB_ON -> isOn.postValue(true)
                }
            }
            gatt!!.readCharacteristic(characteristic!!)
        }
    }

    init {
        btAdapter = (appCtx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        val device = btAdapter?.getRemoteDevice("F8:1D:78:63:4D:34")
        btGatt = device?.connectGatt(appCtx, true, gattCb)
    }

    fun clear() = btGatt?.close() ?: Unit
}