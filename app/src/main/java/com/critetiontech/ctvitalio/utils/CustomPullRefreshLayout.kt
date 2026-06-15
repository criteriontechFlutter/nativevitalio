package com.critetiontech.ctvitalio.utils

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.critetiontech.ctvitalio.R

class CustomPullRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), NestedScrollingParent3 {

    init {
        clipChildren = false
        clipToPadding = false
    }

    private val parentHelper = NestedScrollingParentHelper(this)

    private lateinit var headerView: View
    private lateinit var contentView: View   // NestedScrollView from XML
    private lateinit var arrowIcon: ImageView
    private lateinit var spinner: ProgressBar
    private lateinit var statusText: TextView

    private val headerH by lazy { (72 * resources.displayMetrics.density).toInt() }
    private var currentOffset = 0f
    private val THRESHOLD get() = headerH.toFloat()
    private val MAX_OFFSET get() = headerH * 1.5f

    var isRefreshing = false
        private set

    var onRefreshListener: (() -> Unit)? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        // First child from XML = the NestedScrollView
        contentView = getChildAt(0)

        headerView = LayoutInflater.from(context)
            .inflate(R.layout.custom_refresh_header, this, false)
        // Insert header BEHIND content (index 0) — content translates down to reveal it
        addView(headerView, 0)
        arrowIcon = headerView.findViewById(R.id.refreshArrow)
        spinner = headerView.findViewById(R.id.refreshSpinner)
        statusText = headerView.findViewById(R.id.refreshStatusText)
        // Concave (inward) bottom corners — 24dp radius
        val cornerPx = 24f * resources.displayMetrics.density
        headerView.background = ConcaveHeaderDrawable(
            ContextCompat.getColor(context, R.color.primaryBlue),
            cornerPx
        )
        headerView.post { headerView.translationY = -headerH.toFloat() }
    }

    fun setRefreshing(refreshing: Boolean) {
        if (!refreshing && isRefreshing) {
            isRefreshing = false
            animateCollapse()
        }
    }

    // ─── NestedScrollingParent3 ───────────────────────────────────────────

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean =
        !isRefreshing && (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0

    override fun onStartNestedScroll(child: View, target: View, axes: Int): Boolean =
        onStartNestedScroll(child, target, axes, ViewCompat.TYPE_TOUCH)

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) =
        parentHelper.onNestedScrollAccepted(child, target, axes, type)

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) =
        onNestedScrollAccepted(child, target, axes, ViewCompat.TYPE_TOUCH)

    override fun onStopNestedScroll(target: View, type: Int) {
        parentHelper.onStopNestedScroll(target, type)
        if (type == ViewCompat.TYPE_TOUCH && !isRefreshing) {
            if (currentOffset >= THRESHOLD) triggerRefresh() else animateCollapse()
        }
    }

    override fun onStopNestedScroll(target: View) =
        onStopNestedScroll(target, ViewCompat.TYPE_TOUCH)

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        // User scrolling back up while content is pushed down: restore first
        if (dy > 0 && currentOffset > 0 && !isRefreshing) {
            val consume = minOf(dy.toFloat(), currentOffset)
            applyOffset(currentOffset - consume)
            consumed[1] = consume.toInt()
        }
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) =
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH)

    override fun onNestedScroll(
        target: View, dxConsumed: Int, dyConsumed: Int,
        dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray
    ) {
        if (dyUnconsumed < 0 && !isRefreshing && type == ViewCompat.TYPE_TOUCH) {
            val pull = (-dyUnconsumed) * 0.55f
            val newOffset = (currentOffset + pull).coerceAtMost(MAX_OFFSET)
            applyOffset(newOffset)
            consumed[1] += dyUnconsumed
        }
    }

    override fun onNestedScroll(
        target: View, dxConsumed: Int, dyConsumed: Int,
        dxUnconsumed: Int, dyUnconsumed: Int, type: Int
    ) = onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, IntArray(2))

    override fun onNestedScroll(
        target: View, dxConsumed: Int, dyConsumed: Int,
        dxUnconsumed: Int, dyUnconsumed: Int
    ) = onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH, IntArray(2))

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean) = false
    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float) = false
    override fun getNestedScrollAxes() = parentHelper.nestedScrollAxes

    // ─── Internal helpers ─────────────────────────────────────────────────

    private fun applyOffset(offset: Float) {
        currentOffset = offset
        // Header slides down from above
        headerView.translationY = offset - headerH
        // Content slides down to reveal the header — no Z-order conflict
        contentView.translationY = offset
        val progress = (offset / THRESHOLD).coerceIn(0f, 1f)
        arrowIcon.rotation = progress * 180f
        statusText.text = if (progress >= 1f) "Release to refresh" else "Pull down to refresh"
    }

    private fun triggerRefresh() {
        isRefreshing = true
        currentOffset = headerH.toFloat()
        val interp = FastOutSlowInInterpolator()
        headerView.animate().translationY(0f).setDuration(180).setInterpolator(interp).start()
        contentView.animate().translationY(headerH.toFloat()).setDuration(180).setInterpolator(interp).start()
        arrowIcon.visibility = View.GONE
        spinner.visibility = View.VISIBLE
        statusText.text = "Updating..."
        onRefreshListener?.invoke()
    }

    private fun animateCollapse() {
        val interp = FastOutSlowInInterpolator()
        headerView.animate()
            .translationY(-headerH.toFloat())
            .setDuration(320)
            .setInterpolator(interp)
            .start()
        contentView.animate()
            .translationY(0f)
            .setDuration(320)
            .setInterpolator(interp)
            .withEndAction {
                currentOffset = 0f
                arrowIcon.apply { visibility = View.VISIBLE; rotation = 0f }
                spinner.visibility = View.GONE
                statusText.text = "Pull down to refresh"
            }
            .start()
    }
}