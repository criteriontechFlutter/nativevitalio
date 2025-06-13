package com.critetiontech.ctvitalio.UI.ui.signupFragment

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

    private val _otherChronical = MutableLiveData<Int>()
    val otherChronical: LiveData<Int> get() = _otherChronical

    init {
        _otherChronical.value = 0 // Start at 0%
    }

    fun updateotherChronical(value: Int) {
        _otherChronical.value = value
    }

    private val _NottoshowSkipButton = MutableLiveData<Boolean>()
    val  NottoshowSkipButton: LiveData<Boolean> get() = _NottoshowSkipButton

    init {
        _NottoshowSkipButton.value = false // or false, based on your logic
    }

    fun setNottoSkipButtonVisibility(show: Boolean) {
        _NottoshowSkipButton.value = show
    }

    private val _isNotBack = MutableLiveData<Boolean>()
    val  isNotBack: LiveData<Boolean> get() = _isNotBack

    init {
        _isNotBack.value = false // Start at 0%
    }

    fun updateisNotBack(value: Boolean) {
        _isNotBack.value = value
    }

    private val _isHideProgressBar = MutableLiveData<Boolean>()
    val isHideProgressBar: LiveData<Boolean> get() = _isHideProgressBar

    init {
        _isHideProgressBar.value = false // false means progress bar is visible by default
    }
    fun updateIsHideProgressBar(value: Boolean) {
        _isHideProgressBar.value = value
    }

    private val _isDashboard = MutableLiveData<Boolean>()
    val  isDashboard: LiveData<Boolean> get() = _isDashboard

    init {
        _isDashboard.value = false // false means progress bar is visible by default
    }
    fun updateIsDashboard(value: Boolean) {
        _isDashboard.value = value
    }

    private val _pageNo = MutableLiveData<Int>()
    val  pageNo: LiveData<Int> get() = _pageNo

    init {
        _pageNo.value = 0 // Start at 0%
    }

    fun updatepageNo(value: Int) {
        _pageNo.value = value
    }

}
