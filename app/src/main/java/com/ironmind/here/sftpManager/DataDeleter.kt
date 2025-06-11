package com.ironmind.here.sftpManager
import android.util.Log

class DataDeleter {
    fun deleteOnRaspberry(): Boolean {
        val TAG = "DataDeleter"
        val sftpManager = SftpManager()

        return try {
            if (!sftpManager.connectSftp()) {
                Log.e(TAG, "Connexion SFTP échouée")
                return false
            }

            val remotePath = "/data/${sftpManager.DB_NAME}"
            val success = sftpManager.deleteOnRaspberry(remotePath)
            sftpManager.disconnectSftp()
            success
        } catch (e: Exception) {
            Log.e(TAG, "Erreur Delete", e)
            false
        }
    }
}
