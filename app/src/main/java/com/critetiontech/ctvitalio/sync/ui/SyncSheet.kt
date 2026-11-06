package com.critetiontech.ctvitalio.sync.ui

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.graphics.ColorUtils
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.BuildConfig
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.sync.domain.DeviceState
import com.critetiontech.ctvitalio.sync.domain.Night
import com.critetiontech.ctvitalio.sync.domain.StateSectionModel
import com.critetiontech.ctvitalio.sync.domain.SyncCopy
import com.critetiontech.ctvitalio.sync.domain.SyncDates

/**
 * A Vitalio screen: the blue header scrolls away over a rounded sheet of cards, and only the tab bar
 * stays pinned.
 *
 * A screen is rebuilt only when [rebuildKey] changes. Small changes, like answering the evening
 * question, go through [update] on the live views instead, so they can animate in place.
 */
abstract class SyncSheetFragment : Fragment() {
    protected val vm: SyncViewModel by activityViewModels()
    protected lateinit var ui: SyncUi
    private lateinit var content: LinearLayout
    protected lateinit var body: LinearLayout
        private set

    private var lastKey: Any? = null

    /** Kept across back navigation, so returning to a screen does not replay its entrance. */
    private var hasEntered = false

    /** True only while the first build for a screen is running, so components can queue a reveal. */
    protected var isEntering = false
        private set

    protected open val showsBack: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ui = SyncUi(requireContext())
        content = ui.column()
        return NestedScrollView(requireContext()).apply {
            setBackgroundColor(ui.header)
            isFillViewport = true
            overScrollMode = View.OVER_SCROLL_NEVER
            addView(content)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vm.snapshot.observe(viewLifecycleOwner) { render(it) }
    }

    override fun onDestroyView() {
        lastKey = null
        super.onDestroyView()
    }

    private fun render(snapshot: SyncSnapshot) {
        val key = rebuildKey(snapshot)
        if (key != lastKey) {
            lastKey = key
            isEntering = !hasEntered
            content.removeAllViews()
            content.addView(header(snapshot))
            body = ui.column().apply {
                background = sheetBackground()
                setPadding(ui.dp(GUTTER), ui.dp(20), ui.dp(GUTTER), ui.dp(28))
            }
            content.addView(body, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
            build(body, snapshot)
            if (isEntering) playEntrance()
            isEntering = false
            hasEntered = true
        }
        update(snapshot)
    }

    /** What forces a full rebuild. The clock's minutes never do. */
    protected open fun rebuildKey(snapshot: SyncSnapshot): Any =
        snapshot.copy(moment = snapshot.moment.copy(minute = 0))

    protected abstract fun build(body: LinearLayout, snapshot: SyncSnapshot)

    /** Applies small changes to the live views. Runs after every snapshot, including right after a build. */
    protected open fun update(snapshot: SyncSnapshot) = Unit

    protected open fun headerDate(snapshot: SyncSnapshot): String = SyncDates.longDate(snapshot.moment.logicalDay)

    protected open fun headerLabel(snapshot: SyncSnapshot): String = ui.copy("header.wordmark")

    private fun sheetBackground() = android.graphics.drawable.GradientDrawable().apply {
        setColor(ui.page)
        val r = ui.dp(28).toFloat()
        cornerRadii = floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f)
    }

    /** Sections rise and fade in one after another. Reduced-motion settings shorten this to nothing. */
    private fun playEntrance() {
        val rise = ui.dp(14).toFloat()
        for (i in 0 until body.childCount) {
            val child = body.getChildAt(i)
            child.alpha = 0f
            child.translationY = rise
            child.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(i.coerceAtMost(MAX_STAGGERED) * STAGGER_MS)
                .setDuration(320)
                .setInterpolator(DecelerateInterpolator(1.6f))
                .start()
        }
    }

    /** The blue Vitalio header: a round account button, a label, and the date. */
    private fun header(snapshot: SyncSnapshot): View = with(ui) {
        val top = row().apply { minimumHeight = dp(56) }
        if (showsBack) {
            top.addWithMargins(
                headerButton(R.drawable.ic_sync_chevron_back, 18) { findNavController().navigateUp() },
                end = 12,
                width = dp(44),
                height = dp(44),
            )
        } else {
            top.addWithMargins(accountButton(), end = 12, width = dp(44), height = dp(44))
        }

        val words = column()
        words.addWithMargins(small(headerLabel(snapshot), 14f, ColorUtils.setAlphaComponent(onHeader, 210)))
        words.addWithMargins(
            text(headerDate(snapshot), 18f, onHeader, bold).apply { maxLines = 1 },
            top = 1,
        )
        if (BuildConfig.DEBUG) {
            words.setOnLongClickListener {
                SyncPreviewPanel.show(requireActivity(), vm) { navigate(R.id.sync_states) }
                true
            }
        }
        top.addWithMargins(words, width = 0, weight = 1f)
        if (showsBack) top.addWithMargins(accountButton(), start = 12, width = dp(44), height = dp(44))

        column().apply {
            setBackgroundColor(header)
            setPadding(dp(GUTTER), dp(10), dp(GUTTER), dp(18))
            addWithMargins(top)
        }
    }

    private fun accountButton(): View =
        headerButton(R.drawable.ic_sync_account, 24) { navigate(R.id.sync_account) }.apply {
            contentDescription = ui.copy("account.title")
        }

    private fun headerButton(icon: Int, iconDp: Int, onClick: () -> Unit): View = with(ui) {
        FrameLayout(context).apply {
            background = rounded(ColorUtils.setAlphaComponent(onHeader, 40), 22f)
            addView(
                ImageView(context).apply {
                    setImageResource(icon)
                    setColorFilter(onHeader)
                },
                FrameLayout.LayoutParams(dp(iconDp), dp(iconDp), Gravity.CENTER),
            )
            pressable(this, scale = 0.92f, onClick = onClick)
        }
    }

    protected fun navigate(destination: Int, args: Bundle? = null) {
        findNavController().navigate(destination, args, screenTransition)
    }

    protected fun openPlaceholder(titleKey: String) =
        navigate(R.id.sync_placeholder, bundleOf(SyncPlaceholderFragment.ARG_TITLE to titleKey))

    /** A gap between cards. */
    protected fun LinearLayout.addCard(card: View, top: Int = 14) = with(ui) {
        addWithMargins(card, top = if (childCount == 0) 0 else top)
    }

    /** Plays a drawing's reveal when the screen first opens, timed to arrive with its card. */
    protected fun SyncDrawingView.revealOnEntrance(order: Int = 0) {
        if (isEntering) playReveal(delayMs = 120L + order * STAGGER_MS)
    }

    /** The state section: the same card in every state; only its contents change. */
    protected fun stateCard(model: StateSectionModel, doorsEnabled: Boolean = true, revealOrder: Int = 0): View = with(ui) {
        card().apply {
            addWithMargins(sectionHeader(copy("state.eyebrow"), model.eyebrowLabel, model.showAttentionTick))
            val drawing = SyncDrawingView(context).apply {
                val last = model.dots.lastOrNull() != null
                spec = SyncDrawingView.Spec(
                    dots = model.dots,
                    dashed = model.dashedUsual,
                    // The large dot is last night, so a gap at the end leaves no large dot.
                    emphasizeLast = model.dashedUsual || last,
                    slots = if (model.dashedUsual) 14 else model.dots.size,
                    captionStart = if (model.dashedUsual) null else copy("drawing.your_usual"),
                    captionEnd = when {
                        model.dashedUsual -> copy("drawing.your_usual_from_night_7")
                        last -> copy("drawing.last_night")
                        else -> null
                    },
                )
                describe(model.reason)
                revealOnEntrance(revealOrder)
            }
            addWithMargins(drawing, top = 6)
            addWithMargins(lead(model.lead), top = 8)
            addWithMargins(small(model.reason), top = 6)
            if (model.showDoors) {
                addDivider(top = 12, bottom = 2)
                addWithMargins(doorRow(copy("door.whats_going_on")) {
                    if (doorsEnabled) openPlaceholder("placeholder.whats_going_on")
                })
                addDivider()
                addWithMargins(doorRow(copy("door.might_help")) {
                    if (doorsEnabled) openPlaceholder("placeholder.might_help")
                })
            }
        }
    }

    /** Appears by device state, never as a scold. */
    protected fun ringCard(nights: List<Night>, device: DeviceState): View? = with(ui) {
        if (device == DeviceState.OK) return@with null
        val lastSent = nights.lastOrNull { it.sleepMin != null || it.rhr != null }
        val label = lastSent?.let { copy("ring.last_sent", "day" to SyncDates.weekdayName(it.day)) }
            ?: copy("ring.never_sent")
        card().apply {
            addWithMargins(sectionHeader(copy("ring.title"), label))
            addWithMargins(small(copy("ring.body"), 15f, ink), top = 8)
        }
    }

    protected fun approvalLabel(key: String) =
        SyncCopy.text(if (SyncCopy.isApproved(key)) "states.approved" else "states.draft")

    companion object {
        const val GUTTER = 16
        private const val STAGGER_MS = 60L
        private const val MAX_STAGGERED = 5

        /** Going deeper slides in from the right; going back reverses it. */
        val screenTransition: NavOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.sync_enter)
            .setExitAnim(R.anim.sync_exit)
            .setPopEnterAnim(R.anim.sync_pop_enter)
            .setPopExitAnim(R.anim.sync_pop_exit)
            .build()
    }
}
