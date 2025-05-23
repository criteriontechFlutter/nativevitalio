package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.databinding.MovieLayoutBinding
import com.critetiontech.ctvitalio.model.Result
import java.util.ArrayList

class MovieAdapter : RecyclerView.Adapter<MovieAdapter.ViewHolder>() {
    private var movieList = ArrayList<Result>()
    fun setMovieList(movieList: List<Any>){
        this.movieList = movieList as ArrayList<com.critetiontech.ctvitalio.model.Result>
        notifyDataSetChanged()
    }
    class ViewHolder(val binding : MovieLayoutBinding) : RecyclerView.ViewHolder(binding.root) {}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            MovieLayoutBinding.inflate(
                LayoutInflater.from(
                    parent.context
                )
            )
        )
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView)
            .load("https://image.tmdb.org/t/p/w500"+movieList[position].poster_path)
            .into(holder.binding.imageView)
        holder.binding.textView.text = movieList[position].title
    }
    override fun getItemCount(): Int {
        return movieList.size
    }}
