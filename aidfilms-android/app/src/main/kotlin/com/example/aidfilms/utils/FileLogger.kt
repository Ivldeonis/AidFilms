package com.example.aidfilms.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

object FileLogger {
    private const val TAG = "FileLogger"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun logError(context: Context, throwable: Throwable) {
        val timestamp = dateFormat.format(Date())
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val stackTrace = sw.toString()

        val logMessage = """
            --- CRASH REPORT ---
            Timestamp: $timestamp
            Error: ${throwable.message}
            Stacktrace:
            $stackTrace
            ---------------------

        """.trimIndent()

        try {
            // Try to use public Downloads directory
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val logFile = File(downloadsDir, "aidfilms_crash_log.txt")
            FileOutputStream(logFile, true).use {
                it.write(logMessage.toByteArray())
            }
            Log.d(TAG, "Logged to ${logFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log to Downloads, trying app specific dir", e)
            try {
                val fallbackFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "crash_log.txt")
                FileOutputStream(fallbackFile, true).use {
                    it.write(logMessage.toByteArray())
                }
            } catch (e2: Exception) {
                Log.e(TAG, "All logging failed", e2)
            }
        }
    }

    fun setup(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logError(context, throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
