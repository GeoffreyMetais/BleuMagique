package org.gmetais.bleumagique

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_control.*
import org.gmetais.bleumagique.R.id.imageView

private const val TAG = "BLE/ControlActivity"
private const val REQUEST_ENABLE_BT = 9

class ControlActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener by EmptySeekbarListener {

    internal val model by lazy { ViewModelProviders.of(this, BlueViewModel.Factory(applicationContext)).get(BlueViewModel::class.java) }
    private var power = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        imageView.isEnabled = false
        seekBar.isEnabled = false
        model.state.observe(this, Observer { updateState(it!!) })
        model.connected.observe(this, Observer { onConnectionChanged(it!!) })
        // TODO
//        if (btAdapter?.isEnabled != true) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
//        }
    }

    private fun onConnectionChanged(connected: Boolean) {
        imageView.isEnabled = connected
        seekBar.isEnabled = connected
        Snackbar.make(imageView, if (connected) "service ready" else "service unavailable", Snackbar.LENGTH_LONG).show()
        imageView.setOnTouchListener(TouchHelper(this))
    }

    private fun updateState(state: LampState) {
        if (state.on != power) {
            imageView.setImageResource(if (state.on) R.drawable.ic_lightbulb_outline else R.drawable.ic_lightbulb_outline_off)
            power = state.on
        }
        if (state.temp != seekBar.progress) seekBar.setTemp(state.temp, this)
        title = state.name
    }

    fun toggle(@Suppress("UNUSED_PARAMETER") v: View) = model.toggle()

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        model.setTemp(progress)
    }

    fun setColor(selectedColor: Int) {
        Snackbar.make(imageView, "color ${Integer.toHexString(selectedColor)}", Snackbar.LENGTH_LONG).show()
        imageView.setColorFilter(selectedColor, android.graphics.PorterDuff.Mode.SRC_IN)
    }
}
