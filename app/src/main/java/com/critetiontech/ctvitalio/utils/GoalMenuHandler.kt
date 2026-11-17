package com.critetiontech.vitalioCorporate.utils

class GoalMenuHandler(
    private val onEditClick: () -> Unit,
    private val onRemoveClick: () -> Unit
) {
    fun onEdit() = onEditClick()
    fun onRemove() = onRemoveClick()
}
