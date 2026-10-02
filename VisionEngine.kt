package com.vanta.clashadvisor

import android.graphics.Bitmap

class VisionEngine {

    fun analyze(frame: Bitmap): List<DetectedUnit> {
        /*
         * Detector integration point.
         *
         * Put the ONNX/TFLite model in:
         *
         * app/src/main/assets/models/
         *
         * The production implementation converts the bitmap
         * into the model tensor and returns detected cards.
         */

        return emptyList()
    }
}
