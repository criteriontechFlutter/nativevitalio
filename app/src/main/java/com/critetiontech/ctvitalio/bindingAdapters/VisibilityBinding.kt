package com.critetiontech.ctvitalio.bindingAdapters

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.critetiontech.ctvitalio.R

@BindingAdapter("visibleIf")
fun visibleIf(view: View, condition: Boolean) {
    view.visibility = if (condition) View.VISIBLE else View.GONE
}


@BindingAdapter("isVisible")
fun View.bindIsVisible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

@BindingAdapter("isGoal", "isActive")
fun setGoalIcon(view: ImageView, isGoal: Boolean?, isActive: Int?) {

    if (isGoal == false) {
        // SHOW other image when NOT a goal
        view.setImageResource(R.drawable.step_progress)
        return
    }

    // If isGoal == true → check isActive
    when (isActive) {
        1 -> view.setImageResource(R.drawable.is_goal_active)
        0 -> view.setImageResource(R.drawable.is_goal_inactive)
        else -> view.setImageResource(R.drawable.step_progress) // fallback
    }
}

@BindingAdapter("visibleIfNotEmpty")
fun visibleIfNotEmpty(view: View, value: String?) {
    view.visibility =
        if (!value.isNullOrBlank()) View.VISIBLE else View.GONE
}
