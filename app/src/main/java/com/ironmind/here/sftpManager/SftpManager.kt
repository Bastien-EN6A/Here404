package com.ironmind.here.sftpManager

import android.util.Log
import com.jcraft.jsch.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileNotFoundException

class SftpManager(
    private val username: String= "groupe5",
    private val password: String = "Hilbert23",
    private val host: String = "10.74.251.68",
    private val port: Int = 22,
    val DB_NAME: String = "will_emploi_temps_final.db" //doit etre le meme que dans DatabaseHelper.kt
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
            if (!file.exists()) throw FileNotFoundException("Fichier introuvable : $localPath")  // ⬅️ force une exception
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
    fun isConnected(): Boolean {
        return session?.isConnected == true && channelSftp?.isConnected == true
    }
    fun disconnectSftp() {
        try {
            channelSftp?.takeIf { it.isConnected }?.disconnect()
            session?.takeIf { it.isConnected }?.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la déconnexion SFTP", e)
        }
    }
}