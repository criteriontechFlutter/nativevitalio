package com.critetiontech.ctvitalio.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class FocusShiftViewModel : ViewModel() {

    private val _isPlaying = MutableLiveData<Boolean>(true)
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _timeLeftSeconds = MutableLiveData<Int>(12) // 12 seconds exercise
    val timeLeftSeconds: LiveData<Int> get() = _timeLeftSeconds

    private val _focusGuideText = MutableLiveData<String>("Take Position")
    val focusGuideText: LiveData<String> get() = _focusGuideText

    private val _descriptionText = MutableLiveData<String>(
        "Hold your both hands index finger one after other, 1st at (8-12 inches) distance and other at (16-20 inches)."
    )
    val descriptionText: LiveData<String> get() = _descriptionText

    fun setPlaying(playing: Boolean) {
        if (_isPlaying.value != playing) {
            _isPlaying.value = playing
        }
    }

    fun setTimeLeft(seconds: Int) {
        if (_timeLeftSeconds.value != seconds) {
            _timeLeftSeconds.value = seconds
        }
    }

    fun updateInstructions(guide: String, description: String) {
        if (_focusGuideText.value != guide) {
            _focusGuideText.value = guide
        }
        if (_descriptionText.value != description) {
            _descriptionText.value = description
        }
    }
}