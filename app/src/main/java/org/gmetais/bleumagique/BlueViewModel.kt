package org.gmetais.bleumagique

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.util.Log

private const val TAG = "BLE/BlueViewModel"

typealias LiveState = MutableLiveData<LampState>

class BlueViewModel(appCtx: Context) : ViewModel() {
    internal var connected = MutableLiveData<Boolean>()
    internal var state = LiveState().apply { value = LampState(false, 0) }
    private val controller = BlueController(appCtx, state, connected)

    fun toggle() = controller.toggle()

    override fun onCleared() = controller.clear()

    fun setTemp(temp: Int) {
        Log.d(TAG, "setTemp $temp")
        controller.setTemp(temp)
    }

    class Factory(private val appCtx: Context): ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return BlueViewModel(appCtx) as T
        }
    }
}

class LampState(var on: Boolean, var temp: Int)

internal fun LiveState.update(on: Boolean = value?.on ?: false, temp: Int = value?.temp ?: 0) {
    Log.d(TAG, "update ${this.value?.on} -> $on,\n${this.value?.temp} -> $temp")
    postValue(value?.apply { this.on = on; this.temp = temp })
}