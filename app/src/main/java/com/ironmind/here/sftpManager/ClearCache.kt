package com.ironmind.here.sftpManager

import android.content.Context
import android.util.Log

object ClearCache {
    fun clear(context: Context): Boolean {
        val dbPath = context.getDatabasePath(SftpManager().DB_NAME)
        return if (dbPath.exists()) {
            val deleted = dbPath.delete()
            Log.i("ClearCache", "Base locale supprimée : ${dbPath.absolutePath}")
            deleted
        } else {
            Log.w("ClearCache", "Base locale introuvable, rien à supprimer")
            true
        }
    }
}
