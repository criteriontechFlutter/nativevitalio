package com.criterion.nativevitalio.UI.customviews

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView

class SyncedHorizontalScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : HorizontalScrollView(context, attrs) {

    var linkedScrollViews: List<SyncedHorizontalScrollView>? = null

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        linkedScrollViews?.forEach {
            if (it != this) {
                it.scrollTo(l, t)
            }
        }
    }
}