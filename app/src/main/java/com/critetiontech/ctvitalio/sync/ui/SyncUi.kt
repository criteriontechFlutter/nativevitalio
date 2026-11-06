package com.critetiontech.ctvitalio.sync.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.sync.domain.SyncCopy

/**
 * Shared components in Vitalio's look: white rounded cards on the dashboard background, bold Lato
 * titles and blue accents. Every tappable gets the same ripple, press scale and haptic, so the whole
 * of Sync responds the same way.
 */
class SyncUi(val context: Context) {
    val regular: Typeface = ResourcesCompat.getFont(context, R.font.lato_regular)!!
    val medium: Typeface = ResourcesCompat.getFont(context, R.font.lato_medium)!!
    val bold: Typeface = ResourcesCompat.getFont(context, R.font.lato_bold)!!
    val italic: Typeface = ResourcesCompat.getFont(context, R.font.lato_mediumitalic)!!

    val ink = color(R.color.sync_ink)
    val muted = color(R.color.sync_muted)
    val rule = color(R.color.sync_rule)
    val page = color(R.color.sync_page)
    val surface = color(R.color.sync_surface)
    val header = color(R.color.sync_header)
    val onHeader = color(R.color.sync_on_header)
    val accent = color(R.color.sync_accent)
    val onAccent = color(R.color.sync_on_accent)
    val tint = color(R.color.sync_tint)
    val attention = color(R.color.sync_attention)

    private fun color(id: Int) = ContextCompat.getColor(context, id)

    fun dp(v: Float) = (v * context.resources.displayMetrics.density).toInt()
    fun dp(v: Int) = dp(v.toFloat())
    fun sp(v: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, context.resources.displayMetrics)

    // Text

    fun text(words: CharSequence, size: Float, textColor: Int = ink, face: Typeface = regular) = TextView(context).apply {
        text = words
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
        setTextColor(textColor)
        typeface = face
        includeFontPadding = false
        setLineSpacing(0f, 1.08f)
    }

    /** A card's or section's title. */
    fun title(words: CharSequence, size: Float = 18f) = text(words, size, ink, bold)

    /** A state sentence or other line meant to be read at length. */
    fun lead(words: CharSequence, size: Float = 20f) = text(words, size, ink, medium)

    fun small(words: CharSequence, size: Float = 14f, textColor: Int = muted) = text(words, size, textColor)

    /** Applies the table's `*word*` markup: the person's own words, in italics. */
    fun styled(words: String): CharSequence {
        if (!words.contains('*')) return words
        val out = SpannableStringBuilder()
        words.split('*').forEachIndexed { i, part ->
            val start = out.length
            out.append(part)
            if (i % 2 == 1) out.setSpan(FaceSpan(italic), start, out.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return out
    }

    class FaceSpan(private val face: Typeface) : MetricAffectingSpan() {
        override fun updateDrawState(paint: TextPaint) {
            paint.typeface = face
        }

        override fun updateMeasureState(paint: TextPaint) {
            paint.typeface = face
        }
    }

    // Layout

    fun column() = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }

    fun row() = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    fun LinearLayout.addWithMargins(
        child: View,
        top: Int = 0,
        bottom: Int = 0,
        start: Int = 0,
        end: Int = 0,
        width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        weight: Float = 0f,
    ) {
        addView(child, LinearLayout.LayoutParams(width, height, weight).apply {
            topMargin = dp(top)
            bottomMargin = dp(bottom)
            marginStart = dp(start)
            marginEnd = dp(end)
        })
    }

    fun divider() = View(context).apply { setBackgroundColor(rule) }

    fun LinearLayout.addDivider(top: Int = 0, bottom: Int = 0) =
        addWithMargins(divider(), top = top, bottom = bottom, height = dp(1).coerceAtLeast(1))

    fun rounded(fill: Int, radiusDp: Float, stroke: Int? = null) = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = dp(radiusDp).toFloat()
        stroke?.let { setStroke(dp(1).coerceAtLeast(1), it) }
    }

    /** Vitalio's card: white, 20 dp corners, soft shadow. */
    fun card(padding: Int = 16) = column().apply {
        background = rounded(surface, 20f)
        setPadding(dp(padding), dp(padding), dp(padding), dp(padding))
        elevation = dp(1).toFloat()
        outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
        clipToOutline = true
    }

    /** Ripple, a small press scale and a light haptic, on anything tappable. */
    @SuppressLint("ClickableViewAccessibility")
    fun pressable(view: View, scale: Float = 0.97f, onClick: () -> Unit) {
        val ripple = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, ripple, true)
        view.foreground = ContextCompat.getDrawable(context, ripple.resourceId)
        view.isClickable = true
        view.isFocusable = true
        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(scale).scaleY(scale).setDuration(90).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(160).setInterpolator(OvershootInterpolator(2f)).start()
            }
            false
        }
        view.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onClick()
        }
    }

    /** A chevron in a soft circle, Vitalio's "this opens something" mark. */
    fun chevronBadge(): View = FrameLayout(context).apply {
        background = rounded(tint, 16f)
        addView(
            ImageView(context).apply {
                setImageResource(R.drawable.ic_sync_chevron)
                setColorFilter(accent)
            },
            FrameLayout.LayoutParams(dp(16), dp(16), Gravity.CENTER),
        )
    }

    // Components

    /** A section's heading: bold title on the left, quiet label on the right. */
    fun sectionHeader(title: String, label: String? = null, attentionTick: Boolean = false): View = row().apply {
        if (attentionTick) {
            addWithMargins(
                View(context).apply { setBackgroundColor(attention) },
                end = 8,
                width = dp(14),
                height = dp(2),
            )
        }
        addWithMargins(title(title), width = 0, weight = 1f)
        if (label != null) {
            addWithMargins(small(label, 13f).apply { gravity = Gravity.END }, start = 10, width = SyncUi.WRAP)
        }
    }

    /** A tappable row with a chevron, used inside cards. Never under 52 dp. */
    fun doorRow(title: CharSequence, size: Float = 16f, face: Typeface = medium, onClick: () -> Unit): View = row().apply {
        minimumHeight = dp(52)
        addWithMargins(text(title, size, ink, face), width = 0, weight = 1f)
        addWithMargins(chevronBadge(), start = 10, width = dp(32), height = dp(32))
        pressable(this, scale = 0.985f, onClick = onClick)
    }

    /** A door that stands alone as its own card. */
    fun doorCard(title: CharSequence, onClick: () -> Unit): View = card(padding = 0).apply {
        setPadding(dp(16), dp(4), dp(12), dp(4))
        val line = row().apply { minimumHeight = dp(52) }
        line.addWithMargins(text(title, 16f, ink, medium), width = 0, weight = 1f)
        line.addWithMargins(chevronBadge(), start = 10, width = dp(32), height = dp(32))
        addWithMargins(line)
        pressable(this, onClick = onClick)
    }

    /** A day in a soft pill, for record rows. */
    fun dayPill(day: String): TextView = text(day, 12f, accent, bold).apply {
        gravity = Gravity.CENTER
        background = rounded(tint, 10f)
        setPadding(0, dp(5), 0, dp(5))
    }

    /** A record row: the day, the person's own words in italics, and a small note. */
    fun recordRow(day: String, entry: CharSequence, note: CharSequence?): View = row().apply {
        gravity = Gravity.TOP
        setPadding(0, dp(10), 0, dp(10))
        addWithMargins(dayPill(day), top = 1, end = 12, width = dp(46))
        val body = column()
        body.addWithMargins(text(entry, 16f, ink, italic))
        if (note != null) body.addWithMargins(small(note, 13f), top = 3)
        addWithMargins(body, width = 0, weight = 1f)
    }

    /** A number next to the person's usual, for Last night. */
    fun metricTile(label: String, value: String, usual: String): View = column().apply {
        background = rounded(page, 14f)
        setPadding(dp(10), dp(10), dp(8), dp(10))
        addWithMargins(small(label, 12f).apply { minLines = 2 })
        addWithMargins(text(value, 20f, ink, bold), top = 2)
        addWithMargins(small(usual, 12f), top = 2)
    }

    /** A ledger row: label, value and the person's usual, at least 44 dp. */
    fun ledgerRow(label: String, value: String, unit: String?, usual: String): View = row().apply {
        minimumHeight = dp(44)
        addWithMargins(small(label, 14f), width = 0, weight = 1.15f)
        val words = if (unit != null) "$value $unit" else value
        addWithMargins(text(words, 15f, ink, bold).apply { gravity = Gravity.END }, width = 0, weight = 0.85f)
        addWithMargins(small(usual, 13f).apply { gravity = Gravity.END }, width = 0, weight = 1f)
    }

    /** A quiet outlined pill button that keeps a 44 dp touch target. */
    fun pillButton(words: String, onClick: () -> Unit): TextView = text(words, 14f, accent, bold).apply {
        gravity = Gravity.CENTER
        minHeight = dp(44)
        setPadding(dp(20), 0, dp(20), 0)
        background = rounded(surface, 22f, stroke = accent)
        pressable(this, onClick = onClick)
    }

    fun copy(key: String, vararg slots: Pair<String, Any>) = SyncCopy.text(key, *slots)

    companion object {
        val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
    }
}

/**
 * A choice or chip. Selecting one fills it with the accent colour and gives a small bounce, so an
 * answer visibly lands. Animators follow the system's animation setting, so reduced motion is honoured.
 */
@SuppressLint("ViewConstructor")
class SyncToggle(
    private val ui: SyncUi,
    word: String,
    size: Float,
    radiusDp: Float,
    private val offFill: Int,
    onClick: () -> Unit,
) : AppCompatTextView(ui.context) {
    private val shape = ui.rounded(offFill, radiusDp, stroke = ui.rule)
    private var on = false
    private var colorAnimator: ValueAnimator? = null

    init {
        text = word
        gravity = Gravity.CENTER
        typeface = ui.medium
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
        setTextColor(ui.ink)
        maxLines = 1
        background = shape
        ui.pressable(this, onClick = onClick)
    }

    fun setOn(selected: Boolean, animate: Boolean) {
        if (selected == on) return
        on = selected
        val fill = if (selected) ui.accent else offFill
        val words = if (selected) ui.onAccent else ui.ink
        shape.setStroke(ui.dp(1).coerceAtLeast(1), if (selected) ui.accent else ui.rule)
        colorAnimator?.cancel()
        if (!animate) {
            shape.setColor(fill)
            setTextColor(words)
            return
        }
        val fromFill = shape.color?.defaultColor ?: offFill
        val fromWords = currentTextColor
        colorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 180
            addUpdateListener {
                val t = it.animatedFraction
                shape.setColor(blend(fromFill, fill, t))
                setTextColor(blend(fromWords, words, t))
            }
            start()
        }
        if (selected) {
            scaleX = 0.92f
            scaleY = 0.92f
            animate().scaleX(1f).scaleY(1f).setDuration(260).setInterpolator(OvershootInterpolator(3f)).start()
        }
    }

    private fun blend(from: Int, to: Int, t: Float): Int =
        androidx.core.graphics.ColorUtils.blendARGB(from, to, t)
}
