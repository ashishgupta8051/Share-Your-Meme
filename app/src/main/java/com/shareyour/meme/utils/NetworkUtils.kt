package com.shareyour.meme.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class NetworkUtils {

    fun getNetworkStatus(context: Context): Int {
        var result = 0
        var connectivityManager:ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (connectivityManager != null) {
                var capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork);
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = 2;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = 1;
                    }
                }
            }
        }
        return result
    }
}