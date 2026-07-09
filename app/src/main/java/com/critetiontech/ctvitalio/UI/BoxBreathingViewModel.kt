package com.critetiontech.ctvitalio.UI

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BoxBreathingViewModel : ViewModel() {

    private val _countdownSeconds = MutableLiveData(3)
    val countdownSeconds: LiveData<Int> get() = _countdownSeconds

    private val _isCountdownActive = MutableLiveData(true)
    val isCountdownActive: LiveData<Boolean> get() = _isCountdownActive

    private val _elapsedTimeSeconds = MutableLiveData(0)
    val elapsedTimeSeconds: LiveData<Int> get() = _elapsedTimeSeconds

    private val _isPlaying = MutableLiveData(true)
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _breathingProgress = MutableLiveData(0.0f)
    val breathingProgress: LiveData<Float> get() = _breathingProgress

    private val _currentPhaseText = MutableLiveData("Breathe In")
    val currentPhaseText: LiveData<String> get() = _currentPhaseText

    val sideDurationSeconds = 4
    val totalCycleDurationMs: Int get() = sideDurationSeconds * 4 * 1000

    fun setCountdownSeconds(seconds: Int) {
        _countdownSeconds.postValue(seconds)
    }

    fun setCountdownActive(active: Boolean) {
        _isCountdownActive.postValue(active)
    }

    fun incrementElapsedTime() {
        val current = _elapsedTimeSeconds.value
        if (current != null) {
            _elapsedTimeSeconds.postValue(current + 1)
        }
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.postValue(playing)
    }

    fun setBreathingProgress(progress: Float) {
        _breathingProgress.postValue(progress)
        updatePhaseText(progress)
    }

    private fun updatePhaseText(progress: Float) {
        val phase = when {
            progress < 0.25f -> "Breathe In"
            progress < 0.50f -> "Hold"
            progress < 0.75f -> "Breathe Out"
            else -> "Hold"
        }
        if (phase != _currentPhaseText.value) {
            _currentPhaseText.postValue(phase)
        }
    }
}
