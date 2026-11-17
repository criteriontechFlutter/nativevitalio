package com.critetiontech.vitalioCorporate.UI

import android.app.Dialog
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.critetiontech.vitalioCorporate.R

abstract class BaseActivity : AppCompatActivity() {

    private var loaderDialog: Dialog? = null

    /** Paints the status/nav bar strips when [applySystemBarInsets] is active (Android 15+ edge-to-edge). */
    private var systemBarsBackground: SystemBarsBackground? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allow drawing behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }

    /**
     * Android 15+ with targetSdk 35 forces edge-to-edge: content draws under the status bar,
     * gesture bar and keyboard, and window.statusBarColor / navigationBarColor are ignored.
     *
     * Call after setContentView to pad [root] for those insets and paint the padded strips
     * with the colors passed to [setSystemBarsColor] / [setSystemBarsColorInt].
     * On older Android versions the insets are 0, so this is a no-op.
     */
    fun applySystemBarInsets(root: View) {
        val bg = systemBarsBackground ?: SystemBarsBackground().also { systemBarsBackground = it }
        bg.statusColor = window.statusBarColor
        bg.navColor = window.navigationBarColor
        root.background = bg
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottom = maxOf(bars.bottom, ime.bottom)
            v.updatePadding(left = bars.left, top = bars.top, right = bars.right, bottom = bottom)
            bg.topInset = bars.top
            bg.bottomInset = bottom
            bg.invalidateSelf()
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(root)
    }

    /**
     * Set status and navigation bar colors dynamically
     */
    fun setSystemBarsColor(statusBarColor: Int, navBarColor: Int, lightIcons: Boolean = true) {
        setSystemBarsColorInt(
            ContextCompat.getColor(this, statusBarColor),
            ContextCompat.getColor(this, navBarColor),
            lightIcons
        )
    }

    fun setSystemBarsColorInt(statusColorInt: Int, navColorInt: Int, lightIcons: Boolean = true) {
        window.statusBarColor = statusColorInt
        window.navigationBarColor = navColorInt

        systemBarsBackground?.apply {
            statusColor = statusColorInt
            navColor = navColorInt
            invalidateSelf()
        }

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = lightIcons
        insetsController.isAppearanceLightNavigationBars = lightIcons
    }

    /** Show a full-screen loading dialog. Call [hideLoading] to dismiss. */
    fun showLoading() {
        if (loaderDialog?.isShowing == true) return
        loaderDialog = Dialog(this).apply {
            setContentView(R.layout.dialog_loader)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }
    }

    /** Dismiss the loading dialog shown by [showLoading]. */
    fun hideLoading() {
        loaderDialog?.dismiss()
        loaderDialog = null
    }

    override fun onDestroy() {
        loaderDialog?.dismiss()
        loaderDialog = null
        super.onDestroy()
    }

    /** Draws the status bar color in the top inset and the nav bar color in the bottom inset. */
    private class SystemBarsBackground : Drawable() {
        var statusColor = 0
        var navColor = 0
        var topInset = 0
        var bottomInset = 0
        private val paint = Paint()

        override fun draw(canvas: Canvas) {
            val b = bounds
            if (topInset > 0) {
                paint.color = statusColor
                canvas.drawRect(b.left.toFloat(), b.top.toFloat(), b.right.toFloat(), (b.top + topInset).toFloat(), paint)
            }
            if (bottomInset > 0) {
                paint.color = navColor
                canvas.drawRect(b.left.toFloat(), (b.bottom - bottomInset).toFloat(), b.right.toFloat(), b.bottom.toFloat(), paint)
            }
        }

        override fun setAlpha(alpha: Int) = Unit
        override fun setColorFilter(colorFilter: ColorFilter?) = Unit
        @Deprecated("Deprecated in Java")
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }
}
