package com.ironmind.here.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log

object DatabaseHelper {

    // Vérifie si un étudiant existe avec ce nom + prénom
    fun verifyLogin(context: Context, nom: String, prenom: String): Boolean {
        val dbPath = context.getDatabasePath("appli_presence.db")
        if (!dbPath.exists()) {
            Log.e("DatabaseHelper", "Base de données introuvable à : ${dbPath.absolutePath}")
            return false
        }

        val db = SQLiteDatabase.openDatabase(dbPath.absolutePath, null, SQLiteDatabase.OPEN_READONLY)

        return try {
            val cursor = db.rawQuery(
                "SELECT * FROM etudiants WHERE nom = ? AND prenom = ?",
                arrayOf(nom, prenom)
            )
            val isValid = cursor.moveToFirst()
            cursor.close()
            isValid
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erreur d'accès à la base : ${e.message}")
            false
        } finally {
            db.close()
        }
    }

    // Récupère nom + prénom depuis un ID
    fun getEtudiantById(context: Context, id: String): Pair<String, String> {
        val dbPath = context.getDatabasePath("appli_presence.db").absolutePath
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

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
            e.printStackTrace()
            Pair("Erreur", "Erreur")
        } finally {
            db.close()
        }
    }


    // Récupère l'ID de l'étudiant à partir de son nom et prénom
    fun getEtudiantId(context: Context, nom: String, prenom: String): String? {
        val dbPath = context.getDatabasePath("appli_presence.db")
        if (!dbPath.exists()) return null

        val db = SQLiteDatabase.openDatabase(dbPath.absolutePath, null, SQLiteDatabase.OPEN_READONLY)

        return try {
            val cursor = db.rawQuery(
                "SELECT id FROM etudiants WHERE nom = ? AND prenom = ?",
                arrayOf(nom, prenom)
            )
            val id = if (cursor.moveToFirst()) cursor.getString(0) else null
            cursor.close()
            id
        } catch (e: Exception) {
            null
        } finally {
            db.close()
        }
    }


}
