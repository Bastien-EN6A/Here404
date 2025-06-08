package com.ironmind.here.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.FileOutputStream
import com.ironmind.here.model.Seance


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

    fun getSeancesPourEtudiant(context: Context, etudiantId: String): List<Seance> {
        val dbPath = context.getDatabasePath("emploi_temps_final.db").absolutePath
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

        return try {
            val result = mutableListOf<Seance>()

            val etuCursor = db.rawQuery("SELECT groupe_td, groupe_tp FROM etudiants WHERE id = ?", arrayOf(etudiantId))
            if (!etuCursor.moveToFirst()) return emptyList()
            val groupeTD = etuCursor.getString(0)
            val groupeTP = etuCursor.getString(1)
            etuCursor.close()

            val now = java.time.LocalDateTime.now().toString()

            val cursor = db.rawQuery(
                "SELECT nom, debut, fin, location, groupe FROM seances WHERE debut > ? ORDER BY debut ASC",
                arrayOf(now)
            )

            while (cursor.moveToNext()) {
                val nom = cursor.getString(0)
                val debut = cursor.getString(1)
                val fin = cursor.getString(2)
                val location = cursor.getString(3)
                val groupe = cursor.getString(4)

                if (groupe == "CM" || groupe == groupeTD || groupe == groupeTP) {
                    result.add(Seance(nom, debut, fin, location, groupe))
                }
            }

            cursor.close()
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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
