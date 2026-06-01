package com.critetiontech.ctvitalio.utils


import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

object ColorDetector {

    fun detectColor(bitmap: Bitmap): String {

        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val hsv = Mat()

        Imgproc.cvtColor(
            mat,
            hsv,
            Imgproc.COLOR_RGB2HSV
        )

        val redMask = Mat()
        val blueMask = Mat()
        val greenMask = Mat()
        val yellowMask = Mat()

        Core.inRange(
            hsv,
            Scalar(0.0,120.0,70.0),
            Scalar(10.0,255.0,255.0),
            redMask
        )

        Core.inRange(
            hsv,
            Scalar(100.0,150.0,0.0),
            Scalar(140.0,255.0,255.0),
            blueMask
        )

        Core.inRange(
            hsv,
            Scalar(35.0,50.0,50.0),
            Scalar(85.0,255.0,255.0),
            greenMask
        )

        Core.inRange(
            hsv,
            Scalar(20.0,100.0,100.0),
            Scalar(35.0,255.0,255.0),
            yellowMask
        )

        val red = Core.countNonZero(redMask)
        val blue = Core.countNonZero(blueMask)
        val green = Core.countNonZero(greenMask)
        val yellow = Core.countNonZero(yellowMask)

        return when {

            red > blue &&
                    red > green &&
                    red > yellow -> "Red"

            blue > red &&
                    blue > green &&
                    blue > yellow -> "Blue"

            green > red &&
                    green > blue &&
                    green > yellow -> "Green"

            yellow > red &&
                    yellow > blue &&
                    yellow > green -> "Yellow"

            else -> "Unknown"
        }
    }
}