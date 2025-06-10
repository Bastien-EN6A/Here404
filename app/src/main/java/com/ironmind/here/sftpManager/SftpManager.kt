package com.ironmind.here.sftpManager

import android.util.Log
import com.jcraft.jsch.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SftpManager(
    private val username: String,
    private val password: String,
    private val host: String,
    private val port: Int = 22
) {
    private var session: Session? = null
    private var channelSftp: ChannelSftp? = null

    private val TAG = "SftpManager"

    fun connectSftp(): Boolean {
        return try {
            val jsch = JSch()
            session = jsch.getSession(username, host, port).apply {
                setPassword(password)
                setConfig("StrictHostKeyChecking", "no")
                connect()
            }

            channelSftp = session!!.openChannel("sftp") as ChannelSftp
            channelSftp!!.connect()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur de connexion SFTP", e)
            false
        }
    }

    fun uploadToRaspberry(localPath: String, remotePath: String): Boolean {
        return try {
            val file = File(localPath)
            if (!file.exists()) return false
            channelSftp?.put(FileInputStream(file), remotePath)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'envoi du fichier", e)
            false
        }
    }

    fun downloadFromRaspberry(remotePath: String, localPath: String): Boolean {
        return try {
            channelSftp?.get(remotePath, FileOutputStream(localPath))
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du téléchargement du fichier", e)
            false
        }
    }

    fun deleteOnRaspberry(remotePath: String): Boolean {
        return try {
            channelSftp?.rm(remotePath)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la suppression du fichier", e)
            false
        }
    }

    fun disconnectSftp() {
        try {
            channelSftp?.takeIf { it.isConnected }?.disconnect()
            session?.takeIf { it.isConnected }?.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la déconnexion SFTP", e)
        }
    }

    fun isConnected(): Boolean {
        return session?.isConnected == true && channelSftp?.isConnected == true
    }
}