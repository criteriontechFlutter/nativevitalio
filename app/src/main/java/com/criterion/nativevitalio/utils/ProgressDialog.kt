import android.app.Dialog
import android.content.Context
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.criterion.nativevitalio.R

class ProgressDialog private constructor(
    context: Context,
    text: String = "Loading..."
) : Dialog(context, R.style.TransparentDialogTheme) {

    init {
        setContentView(R.layout.progress_dialog)
        findViewById<TextView>(R.id.tvLoadingText).text = text
        findViewById<LottieAnimationView>(R.id.lottieAnimation).playAnimation()
        setCancelable(false)
    }

    companion object {
        fun show(context: Context, text: String = "Loading..."): ProgressDialog? {
            return if (!context.isValid()) {
                ProgressDialog(context, text).apply { show() }
            } else {
                null
            }
        }
    }

    override fun onBackPressed() {} // Disable back button
}

// Extension to check valid context
fun Context.isValid(): Boolean = this is android.app.Activity && !isFinishing && !isDestroyed