package com.vanta.clashadvisor

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    companion object {
        private const val CAPTURE_REQUEST = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
        }

        val title = TextView(this).apply {
            text = "CLASH ADVISOR"
            textSize = 28f
        }

        val description = TextView(this).apply {
            text = """
                Live battle analysis

                • Screen capture
                • Enemy detection
                • Counter recommendation
                • Elixir-aware strategy
                • Floating overlay
            """.trimIndent()

            textSize = 17f
            setPadding(0, 32, 0, 48)
        }

        val overlayButton = Button(this).apply {
            text = "ENABLE OVERLAY"
            setOnClickListener {
                requestOverlayPermission()
            }
        }

        val startButton = Button(this).apply {
            text = "START ANALYZER"
            setOnClickListener {
                requestScreenCapture()
            }
        }

        root.addView(title)
        root.addView(description)
        root.addView(overlayButton)
        root.addView(startButton)

        setContentView(root)
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun requestScreenCapture() {
        val manager =
            getSystemService(MEDIA_PROJECTION_SERVICE)
                    as MediaProjectionManager

        startActivityForResult(
            manager.createScreenCaptureIntent(),
            CAPTURE_REQUEST
        )
    }

    @Deprecated("Activity result API retained for minimal project")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (
            requestCode == CAPTURE_REQUEST &&
            resultCode == RESULT_OK &&
            data != null
        ) {
            val serviceIntent =
                Intent(this, CaptureService::class.java).apply {
                    putExtra(
                        CaptureService.EXTRA_RESULT_CODE,
                        resultCode
                    )
                    putExtra(
                        CaptureService.EXTRA_RESULT_DATA,
                        data
                    )
                }

            startForegroundService(serviceIntent)
        }
    }
}
