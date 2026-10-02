package com.vanta.clashadvisor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager

class CaptureService : Service() {

    companion object {
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"

        private const val CHANNEL = "clash_advisor"
        private const val NOTIFICATION_ID = 1001
    }

    private var projection: MediaProjection? = null
    private var display: VirtualDisplay? = null
    private var reader: ImageReader? = null

    private val vision = VisionEngine()
    private val strategy = StrategyEngine()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        startForeground(
            NOTIFICATION_ID,
            notification()
        )

        val resultCode =
            intent?.getIntExtra(
                EXTRA_RESULT_CODE,
                -1
            ) ?: return START_NOT_STICKY

        val data =
            intent.getParcelableExtra<Intent>(
                EXTRA_RESULT_DATA
            ) ?: return START_NOT_STICKY

        val manager =
            getSystemService(
                MEDIA_PROJECTION_SERVICE
            ) as MediaProjectionManager

        projection =
            manager.getMediaProjection(
                resultCode,
                data
            )

        startCapture()

        return START_STICKY
    }

    private fun startCapture() {

        val metrics = DisplayMetrics()

        val windowManager =
            getSystemService(
                Context.WINDOW_SERVICE
            ) as WindowManager

        @Suppress("DEPRECATION")
        windowManager.defaultDisplay
            .getMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        reader = ImageReader.newInstance(
            width,
            height,
            android.graphics.PixelFormat.RGBA_8888,
            2
        )

        reader?.setOnImageAvailableListener(
            { imageReader ->

                val image =
                    imageReader.acquireLatestImage()
                        ?: return@setOnImageAvailableListener

                try {
                    val plane = image.planes[0]

                    val buffer = plane.buffer

                    val bitmap =
                        Bitmap.createBitmap(
                            width,
                            height,
                            Bitmap.Config.ARGB_8888
                        )

                    bitmap.copyPixelsFromBuffer(buffer)

                    analyzeFrame(bitmap)

                } finally {
                    image.close()
                }

            },
            null
        )

        display =
            projection?.createVirtualDisplay(
                "ClashAdvisor",
                width,
                height,
                density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader?.surface,
                null,
                null
            )
    }

    private fun analyzeFrame(bitmap: Bitmap) {

        val detected =
            vision.analyze(bitmap)

        val state =
            BattleState(
                timestamp = System.currentTimeMillis(),
                units = detected,
                friendlyElixir = 10f,
                enemyElixir = 5f
            )

        val recommendations =
            strategy.recommend(state)

        if (recommendations.isNotEmpty()) {
            val recommendation =
                recommendations.first()

            // Recommendation dispatch point.
            // Start OverlayService here when required.
        }

        bitmap.recycle()
    }

    private fun notification(): Notification {
        return Notification.Builder(
            this,
            CHANNEL
        )
            .setContentTitle("Clash Advisor")
            .setContentText("Battle analysis active")
            .setSmallIcon(
                android.R.drawable.ic_menu_info_details
            )
            .build()
    }

    private fun createNotificationChannel() {

        val channel =
            NotificationChannel(
                CHANNEL,
                "Clash Advisor",
                NotificationManager.IMPORTANCE_LOW
            )

        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        display?.release()
        reader?.close()
        projection?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
