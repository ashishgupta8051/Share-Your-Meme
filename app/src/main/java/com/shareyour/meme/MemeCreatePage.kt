package com.shareyour.meme

import android.app.DownloadManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.shareyour.meme.connection.CheckInternetConnection
import org.json.JSONObject
import java.lang.Exception
import java.security.Permission
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest
import kotlin.properties.Delegates

class MemeCreatePage : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var url:String
    private lateinit var name:String
    private lateinit var id:String
    private var width by Delegates.notNull<Int>()
    private var height by Delegates.notNull<Int>()
    private var box_count by Delegates.notNull<Int>()
    private val PERMISSION = 123
    private lateinit var checkInternetConnection: CheckInternetConnection
    private lateinit var alertDialog: AlertDialog
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meme_create_page)

        supportActionBar?.title = "Download Template"

        checkInternetConnection = CheckInternetConnection(application)

        //Initialize MobileAds
        MobileAds.initialize(this) {}

        imageView = findViewById(R.id.get_meme_image)

        var intent = intent
        var extras = intent.extras

        if (extras != null){
            url = extras.getString("url").toString()
            name = extras.getString("name").toString()
            id = extras.getString("id").toString()
            width = extras.getInt("width")
            height = extras.getInt("height")
            box_count = extras.getInt("box_count")
        }else{
            Log.e("Error","Value in null")
        }

        //Create Alert Dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val view: View = layoutInflater.inflate(R.layout.internet_alert_dialog,null)

        builder.setView(view)
        alertDialog = builder.setCancelable(false).create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = android.R.style.Animation_Toast

        checkInternetConnection.observe(this, { isConnected ->
            if (isConnected){
                alertDialog.dismiss()
                Glide.with(this).load(url).into(imageView)
                loadAds()
            }else{
                Glide.with(this).load("").into(imageView)
                alertDialog.show()
            }
        })
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
                    Toast.makeText(this@MemeCreatePage, "onAdFailedToLoad() with error $error",Toast.LENGTH_SHORT).show()
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
                    mInterstitialAd?.show(this@MemeCreatePage)
                }
            }
        )

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.downloadmenu,menu)
        //Hide menu item
        var menuItem:MenuItem = menu!!.findItem(R.id.generate_meme)
        menuItem.isVisible = false

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.download -> {
                if (Build.VERSION.SDK_INT >= 23){
                    if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED){
                        downloadMemeTemplate()
                    }else{
                        requestPermission()
                    }
                }else{
                    downloadMemeTemplate()
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

    override fun onBackPressed() {
        startActivity(Intent(this,MemeTemplates::class.java))
        finish()
    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("This permission is needed because of this and that")
                .setPositiveButton("Ok") { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION
                    )
                }
                .setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }
                .create().show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION)
        }
    }

    private fun downloadMemeTemplate() {
        var title = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle(title)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        try{
            request.setDestinationInExternalPublicDir("/Share Your Meme/Meme Template", "$title.jpg")
        }catch (e: Exception){
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,title+".jpg");
        }

        request.setMimeType("image/*")
        var downloadManager: DownloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
        downloadManager.enqueue(request)
        Toast.makeText(this,"Download Successful",Toast.LENGTH_SHORT).show()
    }
}