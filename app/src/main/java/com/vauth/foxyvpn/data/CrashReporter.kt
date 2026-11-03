package com.vauth.foxyvpn.data

import android.content.Context
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

object CrashReporter {
    private const val TAG = "CrashReporter"
    private const val CRASH_FILE_NAME = "last_crash.txt"

    private val _lastCrashReport = MutableStateFlow<String?>(null)
    val lastCrashReport: StateFlow<String?> = _lastCrashReport

    fun install(context: Context) {
        val appContext = context.applicationContext
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val trace = runCatching { formatCrashTrace(thread, throwable) }.getOrDefault(
                "Thread: ${thread.name}\n${throwable.javaClass.name}: ${throwable.message}"
            )
            runCatching { crashFile(appContext).writeText(trace) }
            runCatching {
                _lastCrashReport.value = trace
                AppLogger.e(TAG, "Uncaught exception on thread '${thread.name}': ${throwable.message}", throwable)
            }

            // If a background worker/coroutine thread threw an uncaught exception,
            // log it without killing the user's foreground session.
            val mainThread = runCatching { Looper.getMainLooper()?.thread }.getOrNull()
            if (mainThread != null && thread !== mainThread) {
                return@setDefaultUncaughtExceptionHandler
            }

            if (previousHandler != null) {
                previousHandler.uncaughtException(thread, throwable)
            } else {
                Runtime.getRuntime().exit(1)
            }
        }
    }

    fun replayLastCrashIfAny(context: Context) {
        val file = runCatching { crashFile(context) }.getOrNull() ?: return
        if (!file.exists()) return
        val text = runCatching { file.readText() }.getOrNull()
        runCatching { file.delete() }
        if (text.isNullOrBlank()) return
        _lastCrashReport.value = text
        AppLogger.e(TAG, "app crashed last run; captured stack trace follows")
        text.lineSequence().forEach { line ->
            if (line.isNotBlank()) AppLogger.e(TAG, line)
        }
    }

    private fun formatCrashTrace(thread: Thread, throwable: Throwable): String {
        val writer = StringWriter()
        PrintWriter(writer).use { pw ->
            pw.println("Thread: ${thread.name}")
            throwable.printStackTrace(pw)
        }
        return writer.toString()
    }

    private fun crashFile(context: Context): File = File(context.filesDir, CRASH_FILE_NAME)
}
