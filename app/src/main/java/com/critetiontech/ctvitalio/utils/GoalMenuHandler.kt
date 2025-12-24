package com.critetiontech.ctvitalio.utils

class GoalMenuHandler(
    private val onEditClick: () -> Unit,
    private val onRemoveClick: () -> Unit
) {
    fun onEdit() = onEditClick()
    fun onRemove() = onRemoveClick()
}
