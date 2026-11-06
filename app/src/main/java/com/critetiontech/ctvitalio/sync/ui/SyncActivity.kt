package com.critetiontech.ctvitalio.sync.ui

import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.critetiontech.ctvitalio.BuildConfig
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ActivitySyncBinding
import com.critetiontech.ctvitalio.sync.data.SyncFixtures
import com.critetiontech.ctvitalio.sync.domain.SyncCopy

/**
 * Hosts Sync: two tabs, Home and Your data, with Account in the header. Separate from the Vitalio
 * screens, so the existing app flow is unchanged until someone decides how the two connect.
 */
class SyncActivity : AppCompatActivity() {
    private val vm: SyncViewModel by viewModels()
    private lateinit var binding: ActivitySyncBinding
    private lateinit var nav: NavController
    private lateinit var ui: SyncUi
    private lateinit var tabs: List<Tab>
    private lateinit var pill: View
    private var yourDataActive = false

    private class Tab(val icon: ImageView, val label: TextView)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Drafts wait on clinical review, so only debug builds show them.
        SyncCopy.draftsEnabled = BuildConfig.DEBUG
        super.onCreate(savedInstanceState)
        binding = ActivitySyncBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ui = SyncUi(this)

        val night = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, binding.root).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = !night
        }

        // The real status bar renders above the header; no fake one is drawn.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            binding.syncStatusScrim.layoutParams = binding.syncStatusScrim.layoutParams.apply { height = bars.top }
            binding.syncStatusScrim.requestLayout()
            binding.syncTabBar.setPadding(0, 0, 0, bars.bottom)
            insets
        }

        buildTabBar()
        val host = supportFragmentManager.findFragmentById(R.id.sync_nav_host) as NavHostFragment
        nav = host.navController
        nav.addOnDestinationChangedListener { _, destination, args ->
            setActiveTab(
                destination.id == R.id.sync_placeholder &&
                    args?.getString(SyncPlaceholderFragment.ARG_TITLE) == YOUR_DATA,
            )
        }
        applyDebugExtras()
    }

    override fun onResume() {
        super.onResume()
        // The evening layout starts at 17:00 local, so re-read the clock on returning to the app.
        vm.refresh()
    }

    /** Vitalio's rounded bottom bar. A soft pill slides between the two tabs. */
    private fun buildTabBar() = with(ui) {
        val bar = binding.syncTabBar
        bar.background = GradientDrawable().apply {
            setColor(surface)
            val r = dp(20).toFloat()
            cornerRadii = floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f)
            setStroke(dp(1).coerceAtLeast(1), rule)
        }
        bar.elevation = dp(8).toFloat()

        val frame = FrameLayout(this@SyncActivity).apply { setPadding(dp(12), dp(8), dp(12), dp(8)) }
        pill = View(this@SyncActivity).apply { background = rounded(tint, 22f) }
        frame.addView(pill, FrameLayout.LayoutParams(0, dp(44)))

        val line = row()
        val home = tab(R.drawable.home, copy("tab.home")) { goHome() }
        val data = tab(R.drawable.ic_graph, copy("tab.your_data")) { goYourData() }
        line.addWithMargins(home.first, width = 0, height = dp(44), weight = 1f)
        line.addWithMargins(data.first, width = 0, height = dp(44), weight = 1f)
        frame.addView(line, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(44)))
        tabs = listOf(home.second, data.second)
        bar.addView(frame)

        // The pill's width is half the bar, known only once it has been laid out.
        line.addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
            val width = (right - left) / 2
            if (pill.layoutParams.width != width) {
                pill.layoutParams = (pill.layoutParams as FrameLayout.LayoutParams).apply { this.width = width }
                pill.translationX = if (yourDataActive) width.toFloat() else 0f
            }
        }
        paintTabs()
    }

    private fun tab(icon: Int, title: String, onClick: () -> Unit): Pair<View, Tab> = with(ui) {
        val image = ImageView(this@SyncActivity)
        val label = text(title, 14f, muted, bold).apply { gravity = Gravity.CENTER }
        val holder = LinearLayout(this@SyncActivity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            addWithMargins(image.apply { setImageResource(icon) }, end = 8, width = dp(20), height = dp(20))
            addWithMargins(label, width = SyncUi.WRAP)
            pressable(this, scale = 0.96f, onClick = onClick)
        }
        holder to Tab(image, label)
    }

    private fun setActiveTab(active: Boolean) {
        if (active == yourDataActive) return
        yourDataActive = active
        pill.animate()
            .translationX(if (active) pill.width.toFloat() else 0f)
            .setDuration(240)
            .setInterpolator(DecelerateInterpolator(1.6f))
            .start()
        paintTabs()
    }

    private fun paintTabs() {
        tabs.forEachIndexed { i, tab ->
            val on = (i == 1) == yourDataActive
            val tint = if (on) ui.accent else ui.muted
            tab.icon.setColorFilter(tint)
            tab.label.setTextColor(tint)
        }
    }

    private fun goHome() {
        nav.popBackStack(R.id.sync_home, false)
    }

    private fun goYourData() {
        if (nav.currentBackStackEntry?.arguments?.getString(SyncPlaceholderFragment.ARG_TITLE) == YOUR_DATA) return
        nav.navigate(
            R.id.sync_placeholder,
            bundleOf(SyncPlaceholderFragment.ARG_TITLE to YOUR_DATA),
            NavOptions.Builder()
                .setPopUpTo(R.id.sync_home, false)
                .setEnterAnim(R.anim.sync_tab_in)
                .setExitAnim(R.anim.sync_tab_out)
                .setPopEnterAnim(R.anim.sync_tab_in)
                .setPopExitAnim(R.anim.sync_tab_out)
                .build(),
        )
    }

    /** Lets QA and screenshots open any state, time and screen with adb. Debug builds only. */
    private fun applyDebugExtras() {
        if (!BuildConfig.DEBUG) return
        intent.getStringExtra(EXTRA_FIXTURE)?.let { name ->
            SyncFixtures.Fixture.values().firstOrNull { it.name == name }?.let(vm::setPreview)
        }
        intent.getStringExtra(EXTRA_TIME)?.let { name ->
            PreviewTime.values().firstOrNull { it.name == name }?.let(vm::setPreview)
        }
        when (intent.getStringExtra(EXTRA_SCREEN)) {
            "your_day" -> nav.navigate(R.id.sync_your_day)
            "read_back" -> nav.navigate(R.id.sync_read_back)
            "patterns" -> nav.navigate(R.id.sync_patterns)
            "experiments" -> nav.navigate(R.id.sync_experiments)
            "states" -> nav.navigate(R.id.sync_states)
            "account" -> nav.navigate(R.id.sync_account)
        }
    }

    private companion object {
        const val YOUR_DATA = "placeholder.your_data"
        const val EXTRA_FIXTURE = "sync_fixture"
        const val EXTRA_TIME = "sync_time"
        const val EXTRA_SCREEN = "sync_screen"
    }
}
