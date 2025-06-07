package com.ironmind.here.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.FileOutputStream

object DatabaseHelper {

    private const val DB_NAME = "emploi_temps_final.db"

    // Copie la base de données depuis les assets si elle n'existe pas encore
    fun copyDatabaseIfNeeded(context: Context) {
        val dbPath = context.getDatabasePath(DB_NAME)

        if (!dbPath.exists()) {
            dbPath.parentFile?.mkdirs()

            try {
                context.assets.open(DB_NAME).use { input ->
                    FileOutputStream(dbPath).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("DatabaseHelper", "Base de données copiée avec succès.")
            } catch (e: Exception) {
                Log.e("DatabaseHelper", "Erreur lors de la copie de la base : ${e.message}")
            }
        }
    }

    fun verifyLogin(context: Context, email: String, password: String): Boolean {
        val db = openDatabase(context) ?: return false

        return try {
            val cursor = db.rawQuery(
                "SELECT * FROM etudiants WHERE email = ? AND password = ?",
                arrayOf(email, password)
            )
            val isValid = cursor.moveToFirst()
            cursor.close()
            isValid
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erreur login: ${e.message}")
            false
        } finally {
            db.close()
        }
    }

    fun getEtudiantId(context: Context, email: String, password: String): String? {
        val db = openDatabase(context) ?: return null

        return try {
            val cursor = db.rawQuery(
                "SELECT id FROM etudiants WHERE email = ? AND password = ?",
                arrayOf(email, password)
            )
            val userId = if (cursor.moveToFirst()) cursor.getString(0) else null
            cursor.close()
            userId
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erreur getEtudiantId: ${e.message}")
            null
        } finally {
            db.close()
        }
    }

    fun getEtudiantById(context: Context, id: String): Pair<String, String> {
        val db = openDatabase(context) ?: return Pair("Erreur", "Erreur")

        return try {
            val cursor = db.rawQuery(
                "SELECT nom, prenom FROM etudiants WHERE id = ?",
                arrayOf(id)
            )
            if (cursor.moveToFirst()) {
                val nom = cursor.getString(0)
                val prenom = cursor.getString(1)
                Pair(nom, prenom)
            } else {
                Pair("Inconnu", "Inconnu")
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erreur getEtudiantById: ${e.message}")
            Pair("Erreur", "Erreur")
        } finally {
            db.close()
        }
    }

    private fun openDatabase(context: Context): SQLiteDatabase? {
        val dbPath = context.getDatabasePath(DB_NAME)
        return if (dbPath.exists()) {
            SQLiteDatabase.openDatabase(dbPath.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        } else {
            Log.e("DatabaseHelper", "Base introuvable à ${dbPath.absolutePath}")
            null
        }
    }
}
