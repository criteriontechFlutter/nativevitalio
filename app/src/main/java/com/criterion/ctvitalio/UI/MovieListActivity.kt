package com.critetiontech.ctvitalio.UI

import NetworkUtils
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.critetiontech.ctvitalio.adapter.MovieAdapter
import com.critetiontech.ctvitalio.databinding.ActivityMovieListBinding
import com.critetiontech.ctvitalio.viewmodel.MovieViewModel

class MovieListActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMovieListBinding
    private lateinit var viewModel: MovieViewModel
    private lateinit var movieAdapter : MovieAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prepareRecyclerView()
        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]
        if (NetworkUtils.checkAndShowToast()) {
//            viewModel.getPopularMovies()
        }
        viewModel.observeMovieLiveData().observe(this, fun(movieLiveData: List<Any>) {
            movieAdapter.setMovieList(movieLiveData)

        })
    }

    private fun prepareRecyclerView() {
        movieAdapter = MovieAdapter()
        binding.rvMovies.apply {
            layoutManager = GridLayoutManager(applicationContext,2)
            adapter = movieAdapter
        }
    }
}