package org.gmetais.bleumagique

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_control.*

private const val TAG = "BLE/ControlActivity"
private const val REQUEST_ENABLE_BT = 9

class ControlActivity : AppCompatActivity() {

    private val model by lazy { ViewModelProviders.of(this, BlueViewModel.Factory(applicationContext)).get(BlueViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        model.isOn.observe(this, Observer { toggleButton(it!!) })
        model.connected.observe(this, Observer { onConnectionChanged(it!!) })
        // TODO
//        if (btAdapter?.isEnabled != true) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
//        }
    }

    private fun onConnectionChanged(connected: Boolean) {
        button.isEnabled = connected
        if (connected) Toast.makeText(this, "service ready", Toast.LENGTH_LONG).show()
    }

    private fun toggleButton(active: Boolean) {
        textView.text = if (active) "Allumé" else "Éteint"
        button.text = if (active) "Éteindre" else "Allumer"
    }

    fun toggle(@Suppress("UNUSED_PARAMETER") v: View) {
        model.toggle()
    }
}
