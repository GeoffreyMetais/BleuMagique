package org.gmetais.bleumagique

import android.arch.lifecycle.MutableLiveData
import android.bluetooth.*
import android.content.Context
import java.util.*

class BlueController(appCtx: Context, private val isOn: MutableLiveData<Boolean>, private val connected: MutableLiveData<Boolean>) {

    private var btGatt : BluetoothGatt? = null
    private var btAdapter : BluetoothAdapter? = null
    private var service : BluetoothGattService? = null
    private var characteristic : BluetoothGattCharacteristic? = null

    internal fun send(command: String) = characteristic?.let { btGatt?.writeCharacteristic(it.apply { value = hexStringToByteArray(command) }) }

    private val gattCb = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED && btGatt?.discoverServices() == true)
            else connected.postValue(false)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            service = gatt?.getService(UUID.fromString("0000ffe5-0000-1000-8000-00805f9b34fb"))
            characteristic = service?.getCharacteristic(UUID.fromString("0000ffe9-0000-1000-8000-00805f9b34fb"))
            if (characteristic != null) connected.postValue(true)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val on = isOn.value ?: false
                isOn.postValue(!on)
            }
        }
    }

    init {
        btAdapter = (appCtx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        val device = btAdapter?.getRemoteDevice("F8:1D:78:63:4D:34")
        btGatt = device?.connectGatt(appCtx, true, gattCb)
    }

    fun clear() = btGatt?.close()
}