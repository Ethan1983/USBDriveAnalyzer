package com.sample.usbdriveanalyzer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MountReceiver: BroadcastReceiver() {

    private val TAG = "MountReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "MountReceiver action: ${intent.action}")
        when (intent.action) {
            Intent.ACTION_MEDIA_MOUNTED -> {
                Log.d(TAG, "Mounted: ${intent.data?.path}")
            }
            Intent.ACTION_MEDIA_UNMOUNTED -> {
                Log.d(TAG, "Unmounted: ${intent.data?.path}")
            }
        }
    }
}
