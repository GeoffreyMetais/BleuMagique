package org.gmetais.bleumagique

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context

private const val TAG = "BLE/BlueViewModel"

class BlueViewModel(appCtx: Context) : ViewModel() {
    internal var connected = MutableLiveData<Boolean>()
    internal var isOn = MutableLiveData<Boolean>()
    private val controller = BlueController(appCtx, isOn, connected)

    fun toggle() = controller.toggle()

    override fun onCleared() = controller.clear()

    fun setTemp(temp: Int) = controller.setTemp(temp)

    class Factory(private val appCtx: Context): ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return BlueViewModel(appCtx) as T
        }
    }
}
