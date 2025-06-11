package com.ironmind.here.sftpManager
import android.content.Context
import android.util.Log
import com.ironmind.here.data.DatabaseHelper.DB_NAME

class DataUploader {
    fun upload(context: Context): Boolean {
        val TAG = "DataUploader"
        val sftpManager = SftpManager()

        return try {
            if (!sftpManager.connectSftp()) {
                Log.e(TAG, "Connexion SFTP échouée")
                return false
            }

            val localPath = context.getDatabasePath(DB_NAME).absolutePath
            val remotePath = "/data/${DB_NAME}"

            val success = sftpManager.uploadToRaspberry(localPath, remotePath)
            sftpManager.disconnectSftp()
            success
        } catch (e: Exception) {
            Log.e(TAG, "Erreur Upload", e)
            false
        }
    }
}

