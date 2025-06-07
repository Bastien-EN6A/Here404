package com.ironmind.here.data

import android.content.Context
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.File
import java.io.FileOutputStream

object SftpDownloader {

    fun downloadDbToLocal(context: Context): Boolean {
        val host = "10.74.252.206"
        val username = "groupe5"
        val password = "Hilbert23"
        val remotePath = "/data/apli_presence.db"

        val tempFile = File(context.cacheDir, "apli_presence_temp.db")
        val finalFile = context.getDatabasePath("apli_presence.db")

        return try {
            val jsch = JSch()
            val session: Session = jsch.getSession(username, host, 22)
            session.setPassword(password)

            session.setConfig("StrictHostKeyChecking", "no")
            session.connect(30000)

            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()

            val inputStream = channel.get(remotePath)
            val outputStream = FileOutputStream(tempFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            channel.disconnect()
            session.disconnect()

            // Supprimer l'ancien fichier si présent
            if (finalFile.exists()) {
                finalFile.delete()
            }

            // Copier le fichier téléchargé vers l'emplacement final
            tempFile.copyTo(finalFile, overwrite = true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
