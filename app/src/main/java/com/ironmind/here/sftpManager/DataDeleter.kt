package com.ironmind.here.sftpManager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class DataDeleter(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val TAG = "DataDeleter"

        val sftpManager = SftpManager()

        return try {
            if (!sftpManager.connectSftp()) {
                Log.e(TAG, "Connexion SFTP échouée")
                return Result.failure()
            }

            val remotePath = "/data/"+sftpManager.DB_NAME
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