package com.critetiontech.ctvitalio.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.critetiontech.ctvitalio.model.Movies
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MovieViewModel : ViewModel() {
    private var movieLiveData = MutableLiveData<List<com.critetiontech.ctvitalio.model.Result>>()
//    fun getPopularMovies() {
//        RetrofitInstance.api.getPopularMovies(
//            "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJmNjNkZGZjM2UxNTYwMjI4NDIzMmMzYzY5OWRiOTA3YSIsIm5iZiI6MTc0MjU3NjQxNC43ODgsInN1YiI6IjY3ZGQ5YjFlYzcwYWNkZDlkZjY5OWMwNSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.SMADbRO6a7UycFI0lFJ8szk4dMAxL-rLo2tIYFmSgU0").enqueue(object :
//            Callback<Movies> {
//            override fun onResponse(call: Call<Movies>, response: Response<Movies>) {
//                if (response.body()!=null){
//                    Log.d("TAG", "onResponse: "+response.body()!!.results);
//                    movieLiveData.value = response.body()!!.results
//                }
//                else{
//                    return
//                }
//            }
//            override fun onFailure(call: Call<Movies>, t: Throwable) {
//                Log.d("TAG",t.message.toString())
//            }
//        })
//    }



    fun observeMovieLiveData() : LiveData<List<com.critetiontech.ctvitalio.model.Result>> {
        return movieLiveData
    }
}
