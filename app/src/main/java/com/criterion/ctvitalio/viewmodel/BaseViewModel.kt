package com.critetiontech.ctvitalio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.critetiontech.ctvitalio.utils.NetworkConnectionLiveData

open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    val isConnected = NetworkConnectionLiveData(application)
}
