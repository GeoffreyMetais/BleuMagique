package org.gmetais.bleumagique

import android.support.v4.view.GestureDetectorCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_control.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.min

class TouchHelper(private val activity: ControlActivity) : View.OnTouchListener {
    private var lastY = -1f
    private val actor = actor<Int>(UI, Channel.CONFLATED) {
        for (delta in channel) {
            val newTemp = min(max(2, activity.seekBar.progress + delta), 250)
            activity.model.setTemp(temp = newTemp)
            activity.seekBar.setTemp(newTemp, activity)
        }
    }
    private val screenHeight by lazy {
        val screen = DisplayMetrics()
        activity.windowManager.defaultDisplay.getRealMetrics(screen)
        min(screen.heightPixels, screen.widthPixels)
    }

    private val detector = GestureDetectorCompat(activity, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            activity.toggle(activity.imageView)
            return true
        }
    })

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (detector.onTouchEvent(event)) return true
        val y = when(event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> event.rawY
            MotionEvent.ACTION_UP -> -1f
            else -> return false
        }
        if (lastY != -1f && y != -1f) actor.offer((255*(lastY-y)/screenHeight).toInt())
        lastY = y
        return true
    }
}