package com.critetiontech.ctvitalio.ui.events

import com.critetiontech.ctvitalio.model.HapticType

/**
 * UiEvent.kt
 *
 * Sealed class representing one-shot UI events emitted from ViewModels
 * and consumed by their corresponding Activities/Fragments.
 *
 * Previously located in the incorrect top-level package `sealedClass`.
 * Now lives under `com.critetiontech.ctvitalio.ui.events`.
 *
 * Usage in ViewModel:
 *   private val _uiEvent = MutableSharedFlow<UiEvent>()
 *   val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()
 *
 * Usage in Fragment:
 *   lifecycleScope.launch {
 *       viewModel.uiEvent.collect { event ->
 *           when (event) {
 *               is UiEvent.ShowBottomSheet -> showGlobalMessage(event)
 *           }
 *       }
 *   }
 */
sealed class UiEvent {
    data class ShowBottomSheet(
        val icon: Int,
        val title: String,
        val message: String,
        val buttonText: String = "OK",
        val hapticType: HapticType
    ) : UiEvent()
}
