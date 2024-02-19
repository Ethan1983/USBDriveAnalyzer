package com.sample.usbdriveanalyzer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.storage.StorageManager
import android.os.storage.StorageManager.StorageVolumeCallback
import android.os.storage.StorageVolume
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var listFilesRecursivleyButton: Button
    private lateinit var clear: Button
    private lateinit var output: TextView
    private lateinit var usbMountPathSelector: Spinner
    private lateinit var currentUsbMountPath: String
    private var listJob: Job? = null

    private val usbFileObserver = UsbFileObserver()
    private val storageManager by lazy { getSystemService(Context.STORAGE_SERVICE) as StorageManager }
    private val usbStorageVolumeCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        UsbStorageVolumeCallback()
    } else {
        null
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private class UsbStorageVolumeCallback: StorageVolumeCallback() {
        override fun onStateChanged(volume: StorageVolume) {
            Log.d(TAG, "onStateChanged state: ${volume.state} directory: ${volume.directory} mediaStoreVolumeName: ${volume.mediaStoreVolumeName} isRemovable: ${volume.isRemovable}")
        }
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    displayOutput("\n")
                    displayOutput("ACTION_USB_DEVICE_ATTACHED")
                    Log.d(TAG, "ACTION_USB_DEVICE_ATTACHED")
                    listJob?.cancel()
                    listJob = lifecycleScope.launch(Dispatchers.IO) {
                        handleUsbAttach(currentUsbMountPath, this@MainActivity::displayOutput)
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    displayOutput("\n")
                    displayOutput("ACTION_USB_DEVICE_DETACHED")
                    Log.d(TAG, "ACTION_USB_DEVICE_DETACHED")
                    listJob?.cancel()
                    listJob = null
                }
                else -> error("Unexpected intent action ${intent.action}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listFilesRecursivleyButton = findViewById(R.id.list_files)
        clear = findViewById(R.id.clear)
        output = findViewById(R.id.output)
        usbMountPathSelector = findViewById(R.id.usb_mount_path_selector)

        usbMountPathSelector.adapter = ArrayAdapter.createFromResource(
            this, R.array.spinner_items, android.R.layout.simple_spinner_item)

        usbMountPathSelector.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                currentUsbMountPath = parent.getItemAtPosition(position).toString()
                displayOutput("Current USB Mount Path -> $currentUsbMountPath")
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }

        listFilesRecursivleyButton.setOnClickListener {
            displayOutput("\n")
            listJob?.cancel()
            listJob = lifecycleScope.launch {
                listFilesRecursively(currentUsbMountPath, ::displayOutput)
            }
        }

        clear.setOnClickListener { output.text = "" }

        output.movementMethod = ScrollingMovementMethod()

        registerReceiver(usbReceiver, IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        })

        usbFileObserver.startWatching()

        if (usbStorageVolumeCallback != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            storageManager.registerStorageVolumeCallback(mainExecutor, usbStorageVolumeCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
        usbFileObserver.stopWatching()
        if (usbStorageVolumeCallback != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            storageManager.unregisterStorageVolumeCallback(usbStorageVolumeCallback)
        }
    }

    private fun displayOutput(text: String) {
        runOnUiThread {
            output.text = "${output.text} \n $text"
        }
    }
}
