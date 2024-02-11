package com.sample.usbdriveanalyzer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
    private lateinit var listFilesRecursivleyButton: Button
    private lateinit var clear: Button
    private lateinit var output: TextView
    private lateinit var usbMountPathSelector: Spinner
    private lateinit var currentUsbMountPath: String
    private var listJob: Job? = null

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    displayOutput("\n")
                    displayOutput("ACTION_USB_DEVICE_ATTACHED")
                    listJob?.cancel()
                    listJob = lifecycleScope.launch(Dispatchers.IO) {
                        handleUsbAttach(currentUsbMountPath, this@MainActivity::displayOutput)
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    displayOutput("\n")
                    displayOutput("ACTION_USB_DEVICE_DETACHED")
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
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    private fun displayOutput(text: String) {
        runOnUiThread {
            output.text = "${output.text} \n $text"
        }
    }
}
