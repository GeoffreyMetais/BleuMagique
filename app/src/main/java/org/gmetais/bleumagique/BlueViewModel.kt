package org.gmetais.bleumagique

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.bluetooth.*
import android.content.Context
import java.util.*

private const val TAG = "BLE/BlueViewModel"

@SuppressLint("StaticFieldLeak")
class BlueViewModel(appCtx: Context) : ViewModel() {

    private var btGatt : BluetoothGatt? = null
    private var btAdapter : BluetoothAdapter? = null
    private var service : BluetoothGattService? = null
    private var characteristic : BluetoothGattCharacteristic? = null
    internal var connected = MutableLiveData<Boolean>()
    internal var isOn = MutableLiveData<Boolean>()

    private val gattCb = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                btGatt?.discoverServices()
            } else {
                connected.postValue(false)
            }
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

    fun toggle() = send(if (isOn.value == true) "cc2433" else "cc2333")

    private fun send(command: String) = characteristic?.let { btGatt?.writeCharacteristic(it.apply { value = hexStringToByteArray(command) }) }

    override fun onCleared() {
        connected.value = false
        btGatt?.close()
    }

    init {
        btAdapter = (appCtx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        val device = btAdapter?.getRemoteDevice("F8:1D:78:63:4D:34")
        btGatt = device?.connectGatt(appCtx, true, gattCb)
    }

    class Factory(private val appCtx: Context): ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return BlueViewModel(appCtx) as T
        }
    }
}
