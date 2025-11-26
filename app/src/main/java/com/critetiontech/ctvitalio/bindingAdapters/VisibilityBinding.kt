package com.critetiontech.ctvitalio.bindingAdapters

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("visibleIf")
fun visibleIf(view: View, condition: Boolean) {
    view.visibility = if (condition) View.VISIBLE else View.GONE
}


@BindingAdapter("isVisible")
fun View.bindIsVisible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}