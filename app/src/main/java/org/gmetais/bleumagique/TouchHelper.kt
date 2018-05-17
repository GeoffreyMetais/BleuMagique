package org.gmetais.bleumagique

import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_control.*
import kotlin.math.min

class TouchHelper(private val activity: ControlActivity) : View.OnTouchListener {
    private var lastY = -1f
    private val screenHeight by lazy {
        val screen = DisplayMetrics()
        activity.windowManager.defaultDisplay.getRealMetrics(screen)
        min(screen.heightPixels, screen.widthPixels)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val y = when(event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> event.rawY
            MotionEvent.ACTION_UP -> -1f
            else -> return false
        }
        if (lastY != -1f && y != -1f) {
            activity.seekBar.changeTemp((255*(lastY-y)/screenHeight).toInt())
        }
        lastY = y
        return true
    }
}