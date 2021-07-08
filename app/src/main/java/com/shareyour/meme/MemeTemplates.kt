package com.shareyour.meme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.shareyour.meme.adapter.MemeTemplatesAdapter
import com.shareyour.meme.model.Meme
import org.json.JSONArray
import org.json.JSONObject

class MemeTemplates : AppCompatActivity() {

    private var list:MutableList<Meme> = mutableListOf()
    private lateinit var memeTemplatesAdapter: MemeTemplatesAdapter
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meme_templates)

        supportActionBar?.title = "Meme Templates"

        progressBar = findViewById(R.id.meme_progressBar)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        gridLayoutManager = GridLayoutManager(this,2)
        recyclerView.layoutManager = gridLayoutManager
    }

    override fun onStart() {
        super.onStart()

        val url = "https://api.imgflip.com/get_memes"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET,url,null,
        {
            response ->
            progressBar.visibility = View.GONE
            var jsonObjects:JSONObject = response.getJSONObject("data")
            var jsonArray:JSONArray = jsonObjects.getJSONArray("memes")

            var size = jsonArray.length()

            for (item in 0 until size){
                var jsonObject:JSONObject=jsonArray.getJSONObject(item)
                var id = jsonObject.getString("id")
                var name = jsonObject.getString("name")
                var url = jsonObject.getString("url")
                var width = jsonObject.getInt("width")
                var height = jsonObject.getInt("height")
                var boxCount = jsonObject.getInt("box_count")

                var meme = Meme(boxCount,height,id,name, url, width)
                list.add(meme)

                runOnUiThread{
                    memeTemplatesAdapter = MemeTemplatesAdapter(list,this)
                    recyclerView.adapter = memeTemplatesAdapter
                }
            }

        },{
            error ->
                progressBar.visibility = View.GONE
                Toast.makeText(this,error.message.toString(), Toast.LENGTH_SHORT).show()
        })

        APIClient.getInstance(this).addToRequestQueue(jsonObjectRequest)

    }

    override fun onBackPressed() {
        finish()
    }
}