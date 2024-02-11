package com.sample.usbdriveanalyzer

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

internal inline fun executeCommand(command: String, statusCallback: (String) -> Unit): Int {
    var exitCode = 0
    var process: Process? = null
    try {
        process = Runtime.getRuntime().exec("su")

        val outputStream = DataOutputStream(process.outputStream)
        outputStream.writeBytes(command + "\n")
        outputStream.flush()
        outputStream.close()

        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            //statusCallback("Command output: $line")
        }
        reader.close()

        exitCode = process.waitFor()
        statusCallback("$command exited with code: $exitCode")
    } catch (e: IOException) {
        statusCallback("Error executing command $e")
    } catch (e: InterruptedException) {
        statusCallback("Command execution interrupted $e")
    } finally {
        process?.destroy()
    }

    return exitCode
}
