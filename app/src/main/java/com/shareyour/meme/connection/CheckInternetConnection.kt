package com.shareyour.meme.connection

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.shareyour.meme.R
import com.shareyour.meme.utils.NetworkUtils


class CheckInternetConnection : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        var result = context?.let { NetworkUtils().getNetworkStatus(it) }

        //Create Alert Dialog
        var builder:AlertDialog.Builder = context?.let { AlertDialog.Builder(it) }!!
        var view:View = LayoutInflater.from(context).inflate(R.layout.internet_alert_dialog,null)
        builder.setView(view)

        var button: Button = view.findViewById(R.id.retry_btn)

        var alertDialog = builder.setCancelable(false).create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = android.R.style.Animation_Toast

        button.setOnClickListener {
            alertDialog.dismiss()
            onReceive(context,intent)
        }

        Log.e(TAG, result.toString())
        if (result == 0){
            alertDialog.show()
        }else if(result == 1 || result == 2){
            alertDialog.dismiss()
        }
    }

}