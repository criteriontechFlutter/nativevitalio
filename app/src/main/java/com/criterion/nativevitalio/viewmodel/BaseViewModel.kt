package com.criterion.nativevitalio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.criterion.nativevitalio.utils.NetworkConnectionLiveData

open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    val isConnected = NetworkConnectionLiveData(application)
}
