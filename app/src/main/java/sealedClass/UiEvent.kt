package sealedClass

import com.critetiontech.ctvitalio.model.HapticType

sealed class UiEvent {
    data class ShowBottomSheet(
        val icon: Int,
        val title: String,
        val message: String,
        val buttonText: String = "OK",
        val hapticType: HapticType
    ) : UiEvent()
}