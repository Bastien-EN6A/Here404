package com.ironmind.here.sftpManager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File

class DataUploader(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val TAG = "Uploader"

        val sftpManager = SftpManager()

        return try {
            if (!sftpManager.connectSftp()) {
                Log.e(TAG, "Échec de la connexion SFTP")
                return Result.failure()
            }

            val remotePath = "/data/"+sftpManager.DB_NAME
            val localFile = File(applicationContext.getDatabasePath(sftpManager.DB_NAME).absolutePath)

            val success = sftpManager.uploadToRaspberry(localFile.absolutePath, remotePath) //on upload vers le raspberry
            sftpManager.disconnectSftp()

            if (success) {
                Log.i(TAG, "Upload réussi : ${localFile.absolutePath}")
                Result.success()
            } else {
                Log.e(TAG, "Échec de l'Upload")
                Result.failure()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception dans le Uploader", e)
            Result.failure()
        }
    }
}
