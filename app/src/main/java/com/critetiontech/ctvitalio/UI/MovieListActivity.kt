package com.critetiontech.vitalioCorporate.UI

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.critetiontech.vitalioCorporate.adapter.MovieAdapter
import com.critetiontech.vitalioCorporate.databinding.ActivityMovieListBinding
import com.critetiontech.vitalioCorporate.viewmodel.MovieViewModel

class MovieListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMovieListBinding
    private lateinit var viewModel: MovieViewModel
    private lateinit var movieAdapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prepareRecyclerView()
        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]
        // viewModel.getPopularMovies() // commented out - requires network check
        viewModel.observeMovieLiveData().observe(this) { movieLiveData ->
            movieAdapter.setMovieList(movieLiveData)
        }
    }

    private fun prepareRecyclerView() {
        movieAdapter = MovieAdapter()
        binding.rvMovies.apply {
            layoutManager = GridLayoutManager(applicationContext, 2)
            adapter = movieAdapter
        }
    }
}