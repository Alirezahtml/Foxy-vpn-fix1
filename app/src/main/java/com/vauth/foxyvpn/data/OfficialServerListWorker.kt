package com.vauth.foxyvpn.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class OfficialServerListWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val settings = SettingsStore(applicationContext)
        if (!settings.serverListAutoRefresh) return Result.success()
        return ServerListClient(applicationContext).refreshFromOfficial()
            .fold(
                onSuccess = { count ->
                    settings.lastServerListRefreshEpochMs = System.currentTimeMillis()
                    AppLogger.i("OfficialServerListWorker", "automatic refresh completed: $count countries")
                    Result.success()
                },
                onFailure = { error ->
                    AppLogger.w("OfficialServerListWorker", "automatic refresh failed", error)
                    if (runAttemptCount < 3) Result.retry() else Result.failure()
                },
            )
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "official_mozilla_server_list_refresh"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<OfficialServerListWorker>(12, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.MINUTES,
                )
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }
    }
}
