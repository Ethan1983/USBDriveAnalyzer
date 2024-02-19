package com.sample.usbdriveanalyzer

import android.os.FileObserver
import android.util.Log

class UsbFileObserver : FileObserver(USB_MOUNT_PATH, MASK) {

    override fun onEvent(event: Int, path: String?) {
        Log.d(TAG, "onEvent: $event invoked for $path")
        if (path != null) {
            if (event and CREATE != 0) {
                Log.d(TAG, "Created: $path")
            } else if (event and DELETE != 0) {
                Log.d(TAG, "Deleted: $path")
            }
        }
    }

    companion object {
        private const val TAG = "UsbFileObserver"
        private const val USB_MOUNT_PATH = "/mnt/media_rw/EF33-121C"
        //private const val MASK = ALL_EVENTS
        private const val MASK = CREATE or DELETE
    }
}
