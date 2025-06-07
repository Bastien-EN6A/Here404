package com.ironmind.here.data

import android.content.Context
import java.io.File
import java.io.FileOutputStream

object PreloadedDatabaseInstaller {

    fun copyDatabaseIfNeeded(context: Context) {
        val dbFile = context.getDatabasePath("appli_presence.db")

        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()

            context.assets.open("appli_presence.db").use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}
