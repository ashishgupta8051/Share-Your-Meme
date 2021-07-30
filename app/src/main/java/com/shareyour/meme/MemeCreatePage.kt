package com.shareyour.meme

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
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
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.shareyour.meme.connection.CheckInternetConnection
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class MemeCreatePage : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var progressBar:ProgressBar
    private lateinit var url:String
    private lateinit var name:String
    private lateinit var id:String
    private var width by Delegates.notNull<Int>()
    private var height by Delegates.notNull<Int>()
    private var box_count by Delegates.notNull<Int>()
    private val PERMISSION = 123
    private var broadcastReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meme_create_page)

        supportActionBar?.title = "Download Template"

        broadcastReceiver = CheckInternetConnection()

        imageView = findViewById(R.id.get_meme_image)
        progressBar = findViewById(R.id.memeCreateProgress)

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

        Glide.with(this).load(url).listener(object : RequestListener<Drawable> {
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
    }

    override fun onStart() {
        super.onStart()
        var intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastReceiver);
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