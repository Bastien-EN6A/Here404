package com.ironmind.here.sftpManager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class DataDeleter(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val TAG = "DataDeleter"

        val sftpManager = SftpManager(
            username = "groupe5",
            password = "Hilbert23",
            host = "10.74.252.206"
        )

        return try {
            if (!sftpManager.connectSftp()) {
                Log.e(TAG, "Connexion SFTP échouée")
                return Result.failure()
            }

            val remotePath = "/data/appli_presence.db"
            val success = sftpManager.deleteOnRaspberry(remotePath)

            sftpManager.disconnectSftp()

            if (success) {
                Log.i(TAG, "Suppression réussie de $remotePath")
                Result.success()
            } else {
                Log.e(TAG, "Échec de la suppression de $remotePath")
                Result.failure()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception dans DataDeleter", e)
            Result.failure()
        }
    }
}