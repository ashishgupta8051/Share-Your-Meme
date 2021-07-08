package com.shareyour.meme.holder

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.shareyour.meme.R

class MemeTemplatesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var imageView: ImageView = itemView.findViewById(R.id.meme_image)

    var progressBar:ProgressBar = itemView.findViewById(R.id.meme_image_progressBar)

}