package com.critetiontech.ctvitalio.UI.customviews
import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView

class SyncedHorizontalScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : HorizontalScrollView(context, attrs) {

    var linkedScrollViews: MutableList<SyncedHorizontalScrollView> = mutableListOf()
    private var isSyncing = false

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (!isSyncing) {
            linkedScrollViews.forEach { scrollView ->
                if (scrollView !== this) {
                    scrollView.isSyncing = true
                    scrollView.scrollTo(l, t)
                    scrollView.isSyncing = false
                }
            }
        }
    }
}