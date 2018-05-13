package org.gmetais.bleumagique

import android.widget.SeekBar

fun hexStringToByteArray(hexString: String): ByteArray {
    val length = hexString.length
    val data = ByteArray(length / 2)
    for (i in 0 until length step 2) {
        data[i / 2] = ((Character.digit(hexString[i], 16) shl 4) + Character.digit(hexString[i + 1], 16)).toByte()
    }
    return data
}

fun ByteArray.toHexString(): String {
    val builder = StringBuilder()
    for (b in this) builder.append(String.format("%02X", b))
    return builder.toString()
}

fun Int.toHexByte(): String = Integer.toHexString(this).let { if (this < 16) "0$it" else it}

object EmptySeekbarListener : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean){}
    override fun onStartTrackingTouch(seekBar: SeekBar?){}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}