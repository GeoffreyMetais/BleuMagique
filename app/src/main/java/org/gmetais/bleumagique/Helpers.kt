package org.gmetais.bleumagique

import android.widget.SeekBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

fun String.toByteArray(): ByteArray {
    val length = length
    val data = ByteArray(length / 2)
    for (i in 0 until length step 2) {
        data[i / 2] = ((Character.digit(this[i], 16) shl 4) + Character.digit(this[i + 1], 16)).toByte()
    }
    return data
}

fun ByteArray.toHexString(): String {
    val builder = StringBuilder()
    for (b in this) builder.append(String.format("%02X", b))
    return builder.toString()
}

fun Byte.toUInt() = toInt() and 0xFF

fun Int.toHexByte(): String = Integer.toHexString(this).let { if (this < 16) "0$it" else it}

object EmptySeekbarListener : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean){}
    override fun onStartTrackingTouch(seekBar: SeekBar?){}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}

internal fun SeekBar.setTemp(temp: Int, listener: SeekBar.OnSeekBarChangeListener? = null) {
    if (listener != null) setOnSeekBarChangeListener(null)
    if (android.os.Build.VERSION.SDK_INT >= 24) setProgress(temp, true)
    else progress = temp
    if (listener != null) setOnSeekBarChangeListener(listener)
}

internal fun SeekBar.changeTemp(delta: Int) = setTemp(progress+delta)

object AppScope : CoroutineScope {
    @ExperimentalCoroutinesApi
    override val coroutineContext = Dispatchers.Main.immediate
}
