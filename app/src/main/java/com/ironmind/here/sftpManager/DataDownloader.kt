package com.ironmind.here.sftpManager

import android.content.Context
import android.util.Log
import java.io.File

object DataDownloader {
    fun download(context: Context): Boolean {
        val sftpManager = SftpManager()
        val dbFile = context.getDatabasePath(sftpManager.DB_NAME)

        return try {
            if (!sftpManager.connectSftp()) {
                Log.e("Downloader", "Connexion SFTP échouée")
                false
            } else {
                val remote = "/data/${sftpManager.DB_NAME}"
                val ok = sftpManager.downloadFromRaspberry(remote, dbFile.absolutePath)
                sftpManager.disconnectSftp()
                Log.i("Downloader", if (ok) "Téléchargement réussi" else "Échec du téléchargement")
                ok
            }
        } catch (e: Exception) {
            Log.e("Downloader", "Exception : ${e.message}")
            false
        }
    }
}
