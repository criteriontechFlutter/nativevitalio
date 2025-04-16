package com.criterion.nativevitalio.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

object BindingAdapters {
    @BindingAdapter("imageUrl")
    fun loadImage(view: ImageView, imageUrl: String?) {

            Glide.with(view.context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)

    }
}
