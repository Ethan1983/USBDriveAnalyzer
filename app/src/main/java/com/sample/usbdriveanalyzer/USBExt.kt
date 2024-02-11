package com.sample.usbdriveanalyzer

import kotlin.system.measureTimeMillis

internal fun isMountPathAvailable(usbMountPath: String): Boolean {
    val exitCode = executeCommand("ls $usbMountPath", {})
    return exitCode == 0
}

internal fun listFilesRecursively(usbMountPath: String, statusCallback: (String) -> Unit) {
    if (isMountPathAvailable(usbMountPath)) {
        statusCallback("List files recursively in $usbMountPath")
        val time = measureTimeMillis {
            executeCommand("ls -R $usbMountPath", statusCallback)
        }
        statusCallback("ls -R $usbMountPath took $time milliseconds")
    } else {
        statusCallback("$usbMountPath is unavailable")
    }
}

internal fun handleUsbAttach(usbMountPath: String, statusCallback: (String) -> Unit) {
    val time = measureTimeMillis {
        while (!isMountPathAvailable(usbMountPath)) { }
    }

    statusCallback("USB Mount Path $usbMountPath is available only after $time milliseconds")

    listFilesRecursively(usbMountPath, statusCallback)
}
