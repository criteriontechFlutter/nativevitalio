package com.critetiontech.ctvitalio.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.model.WatchModel

class ConnectSmartWatchViewModel (application: Application) : BaseViewModel(application){

    private val _watchList = MutableLiveData<List<WatchModel>>()
    val watchList: LiveData<List<WatchModel>> = _watchList

    init {
        // Initial dummy data
        _watchList.value = listOf(
            WatchModel("Samsung Galaxy 7 Watch", "91%", R.drawable.watch),
            WatchModel("Apple Watch Series 10", "55%", R.drawable.watch)
        )
    }

    fun removeWatch(item: WatchModel) {
        _watchList.value = _watchList.value?.filter { it != item }
    }

    fun addWatch(item: WatchModel) {
        _watchList.value = _watchList.value?.toMutableList()?.apply { add(item) }
    }


}