package com.shareyour.meme

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.shareyour.meme.adapter.MemeTemplatesAdapter
import com.shareyour.meme.connection.CheckInternetConnection
import com.shareyour.meme.model.Meme
import org.json.JSONArray
import org.json.JSONObject

class MemeTemplates : AppCompatActivity() {

    private var list:MutableList<Meme> = mutableListOf()
    private lateinit var memeTemplatesAdapter: MemeTemplatesAdapter
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var checkInternetConnection: CheckInternetConnection
    private lateinit var alertDialog: AlertDialog
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meme_templates)

        supportActionBar?.title = "Meme Templates"

        checkInternetConnection = CheckInternetConnection(application)

        progressBar = findViewById(R.id.meme_progressBar)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        gridLayoutManager = GridLayoutManager(this,2)
        recyclerView.layoutManager = gridLayoutManager

        //Create Alert Dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val view:View = layoutInflater.inflate(R.layout.internet_alert_dialog,null)

        builder.setView(view)
        alertDialog = builder.setCancelable(false).create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = android.R.style.Animation_Toast

    }

    override fun onStart() {
        super.onStart()
        //Check internet connection
        checkConnection()
    }

    override fun onBackPressed() {
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    private fun checkConnection() {
        checkInternetConnection.observe(this, { isConnected ->
            if (isConnected){
                alertDialog.dismiss()
                loadMemeTemplates()
                showAdsAgain()
            }else{
                alertDialog.show()
            }
        })
    }

    private fun showAdsAgain() {
        Handler(Looper.myLooper()!!).postDelayed(
            {
            loadAds()
            }, 1000 * 60 * 5);
    }

    private fun loadAds() {
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this, AD_UNIT_ID, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError?.message)
                    mInterstitialAd = null
                    val error = "domain: ${adError.domain}, code: ${adError.code}, " +
                            "message: ${adError.message}"
                    Toast.makeText(this@MemeTemplates, "onAdFailedToLoad() with error $error",Toast.LENGTH_SHORT).show()
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd

                    mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.e(TAG, "Ad was dismissed.")
                            mInterstitialAd = null
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                            Log.e(TAG, "Ad failed to show.")
                            mInterstitialAd = null
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.e(TAG, "Ad showed fullscreen content.")
                        }
                    }
                    mInterstitialAd?.show(this@MemeTemplates)
                }
            }
        )

    }

    private fun loadMemeTemplates() {
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
}