package com.shareyour.meme

import APIClient
import android.Manifest
import android.app.DownloadManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*

const val AD_UNIT_ID = "ca-app-pub-7419751706380309/3642459166"

class MainActivity : AppCompatActivity(){
    lateinit var shareBtn:Button
    lateinit var nextBtn:Button
    lateinit var imageView:ImageView
    lateinit var progressBar:ProgressBar
    var url:String? = null
    private val PERMISSION:Int = 123;
    lateinit var fileOutputStream:FileOutputStream
    lateinit var bitmap: Bitmap
    lateinit var builder:StrictMode.VmPolicy.Builder
    lateinit var drawable: BitmapDrawable
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        shareBtn = findViewById(R.id.shareBtn)
        nextBtn = findViewById(R.id.nextBtn)

        imageView = findViewById(R.id.imageView)

        progressBar = findViewById(R.id.progressBar)

        //Initialize MobileAds
        MobileAds.initialize(this) {}

        loadMeme()

        shareBtn.setOnClickListener {
           share()
        }

        nextBtn.setOnClickListener {
           loadMeme()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.downloadmenu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.download){
            if (Build.VERSION.SDK_INT >= 23){
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                    savePostImage();
                }else {
                    requestPermission();
                }
            }else {
                savePostImage();
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Enable Permission", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun share() {
        builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        drawable = imageView.drawable as BitmapDrawable
        bitmap = drawable.bitmap

        val file = File(externalCacheDir,"meme"+".png")
        val intent:Intent

        try {
            fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream)

            fileOutputStream.flush()
            fileOutputStream.close()

            intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(file))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }catch (e:Exception){
            throw RuntimeException(e)
        }
        startActivity(Intent.createChooser(intent,"share image"))
    }

    override fun onStart() {
        super.onStart()
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.i(TAG, adError?.message)
                    mInterstitialAd = null
                    val error = "domain: ${adError.domain}, code: ${adError.code}, " +
                            "message: ${adError.message}"
                    Toast.makeText(this@MainActivity,"onAdFailedToLoad() with error $error", Toast.LENGTH_SHORT).show()
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.i(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.i(TAG, "Ad was dismissed.")
                            mInterstitialAd = null
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                            Log.i(TAG, "Ad failed to show.")
                            mInterstitialAd = null
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.i(TAG, "Ad showed fullscreen content.")
                        }
                    }

                    if (mInterstitialAd != null) {
                        mInterstitialAd?.show(this@MainActivity)
                    } else {
                        Toast.makeText(this@MainActivity, "Ad wasn't loaded.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("This permission is needed because of this and that")
                .setPositiveButton("Ok") { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION
                    )
                }
                .setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }
                .create().show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION)
        }
    }

    private fun savePostImage() {
        var title = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle(title)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        try{
            request.setDestinationInExternalPublicDir("/Meme", "$title.jpg")
        }catch (e:Exception){
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,title+".jpg");
        }

        request.setMimeType("image/*")
        var downloadManager:DownloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
        downloadManager.enqueue(request)
        Toast.makeText(this,"Download Successful",Toast.LENGTH_SHORT).show()
    }

    private fun loadMeme() {
        val url = "https://meme-api.herokuapp.com/gimme"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                progressBar.visibility = View.VISIBLE
                this.url = response.getString("url")
                Glide.with(this).load(this.url).listener(object : RequestListener<Drawable>{
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }

                }).into(imageView)
            },
            { error ->
                progressBar.visibility = View.GONE
                Toast.makeText(this,error.message.toString(),Toast.LENGTH_SHORT).show()
            })

        APIClient.getInstance(this).addToRequestQueue(jsonObjectRequest)

    }
}
