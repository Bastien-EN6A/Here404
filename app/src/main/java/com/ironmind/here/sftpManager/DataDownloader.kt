package com.ironmind.here.sftpManager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File

class DataDownloader(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val TAG = "Downloader"

        val sftpManager = SftpManager(
            username = "groupe5",
            password = "Hilbert23",
            host = "10.74.252.206"
        )

        return try {
            if (!sftpManager.connectSftp()) {
                Log.e(TAG, "Échec de la connexion SFTP")
                return Result.failure()
            }

            val remotePath = "/data/appli_presence.db"
            val localFile = File(applicationContext.getDatabasePath("appli_presence.db").absolutePath)

            val success = sftpManager.downloadFromRaspberry(remotePath, localFile.absolutePath) //on download du raspberry

            sftpManager.disconnectSftp()

            if (success) {
                Log.i(TAG, "Téléchargement réussi : ${localFile.absolutePath}")
                Result.success()
            } else {
                Log.e(TAG, "Échec du téléchargement")
                Result.failure()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception dans le Downloader", e)
            Result.failure()
        }
    }
}