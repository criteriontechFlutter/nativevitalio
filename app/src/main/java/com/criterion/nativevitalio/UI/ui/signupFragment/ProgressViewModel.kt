package com.criterion.nativevitalio.UI.ui.signupFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProgressViewModel : ViewModel() {
    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> get() = _progress

    init {
        _progress.value = 0 // Start at 0%
    }

    fun updateProgress(value: Int) {
        _progress.value = value
    }
}
