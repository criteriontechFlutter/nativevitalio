package com.critetiontech.vitalioCorporate.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.critetiontech.vitalioCorporate.utils.NetworkConnectionLiveData

open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    val isConnected = NetworkConnectionLiveData(application)
}
