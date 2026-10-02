package com.vanta.clashadvisor

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var panel: TextView

    override fun onCreate() {
        super.onCreate()

        windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        panel = TextView(this).apply {
            text = "CLASH ADVISOR\nWaiting for battle..."
            textSize = 15f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(215, 10, 15, 22))
            setPadding(28, 20, 28, 20)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 80

        windowManager.addView(panel, params)
    }

    fun showRecommendation(
        recommendation: Recommendation
    ) {
        panel.text =
            """
            CLASH ADVISOR

            PLAY:
            ${recommendation.card}

            ${recommendation.reason

            SCORE:
            ${"%.1f".format(recommendation.score)}
            """.trimIndent()
    }

    override fun onDestroy() {
        if (::panel.isInitialized) {
            windowManager.removeView(panel)
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
