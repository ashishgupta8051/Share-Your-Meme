package com.shareyour.meme.adapter

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.shareyour.meme.MemeCreatePage
import com.shareyour.meme.R
import com.shareyour.meme.holder.MemeTemplatesHolder
import com.shareyour.meme.model.Meme

class MemeTemplatesAdapter(private val dataList:MutableList<Meme>,private val activity: Activity)
    : RecyclerView.Adapter<MemeTemplatesHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemeTemplatesHolder {
        return MemeTemplatesHolder(activity.layoutInflater.inflate(R.layout.meme_list,parent,false))
    }

    override fun onBindViewHolder(holder: MemeTemplatesHolder, position: Int) {
        val meme = dataList[position]

        var imageUrl = meme.url

        Glide.with(activity).load(imageUrl).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                holder.progressBar.visibility = View.GONE
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                holder.progressBar.visibility = View.GONE
                return false
            }

        }).into(holder.imageView)

        holder.itemView.setOnClickListener(View.OnClickListener {
            var intent = Intent(activity,MemeCreatePage::class.java)
            intent.putExtra("url",meme.url)
            intent.putExtra("id",meme.id)
            intent.putExtra("name",meme.name)
            intent.putExtra("width",meme.width)
            intent.putExtra("height",meme.height)
            intent.putExtra("box_count",meme.box_count)
            activity.startActivity(intent)
            activity.finish()
        })
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}