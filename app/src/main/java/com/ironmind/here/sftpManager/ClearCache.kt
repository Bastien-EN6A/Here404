package com.ironmind.here.sftpManager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class ClearCache(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val TAG = "ClearCache"
        return try {
            val dbFile = applicationContext.getDatabasePath("appli_presence.db")
            if (dbFile.exists()) {
                val deleted = dbFile.delete()
                if (deleted) {
                    Log.i(TAG, "Base locale supprimée : ${dbFile.absolutePath}")
                    Result.success()
                } else {
                    Log.e(TAG, "Échec de suppression de la base locale")
                    Result.failure()
                }
            } else {
                Log.w(TAG, "Base locale introuvable, rien à supprimer")
                Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la suppression de la base locale", e)
            Result.failure()
        }
    }
}