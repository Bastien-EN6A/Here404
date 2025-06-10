package com.ironmind.here.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import java.io.FileOutputStream
import com.ironmind.here.model.Seance
import com.ironmind.here.sftpManager.ClearCache
import com.ironmind.here.sftpManager.DataDeleter
import com.ironmind.here.sftpManager.DataDownloader
import com.ironmind.here.sftpManager.DataUploader
import androidx.work.WorkManager


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
                Log.d("DatabaseHelper", "Base de données par defaut copiée avec succès.")
            } catch (e: Exception) {
                Log.e("DatabaseHelper", "Erreur lors de la copie de la base : ${e.message}")
            }
        }
    }

    fun UpdateLocal(context: Context){
        val downloadRequest = OneTimeWorkRequestBuilder<DataDownloader>().build()
        val clearCache = OneTimeWorkRequestBuilder<ClearCache>().build()
        WorkManager.getInstance(context).enqueue(clearCache) //pour nettoyer la bdd locale
        WorkManager.getInstance(context).enqueue(downloadRequest) //pour telecharger
    }
    fun UpdateRasp(context: Context){
        val uploadRequest = OneTimeWorkRequestBuilder<DataUploader>().build()
        val deleteRequest = OneTimeWorkRequestBuilder<DataDeleter>().build()
        WorkManager.getInstance(context).enqueue(deleteRequest) //pour delete les infos obsoletes
        WorkManager.getInstance(context).enqueue(uploadRequest) //pour upload la nouvelle bdd sur le raspberry
    }

    fun verifyLogin(context: Context, email: String, password: String): Triple<String, String, String>? {
        val dbPath = context.getDatabasePath(DB_NAME)
        if (!dbPath.exists()) return null

        val db = SQLiteDatabase.openDatabase(dbPath.absolutePath, null, SQLiteDatabase.OPEN_READONLY)

        try {
            // Étudiant
            val studentCursor = db.rawQuery(
                "SELECT id, nom, prenom FROM etudiants WHERE email = ? AND password = ?",
                arrayOf(email, password)
            )
            if (studentCursor.moveToFirst()) {
                val id = studentCursor.getString(0)
                val nom = studentCursor.getString(1)
                val prenom = studentCursor.getString(2)
                studentCursor.close()
                return Triple(id, "etudiant", "$prenom $nom")
            }
            studentCursor.close()

            // Prof
            val profCursor = db.rawQuery(
                "SELECT id, nom FROM profs WHERE email = ? AND password = ?",
                arrayOf(email, password)
            )
            if (profCursor.moveToFirst()) {
                val id = profCursor.getInt(0).toString()
                val nom = profCursor.getString(1)
                profCursor.close()
                return Triple(id, "prof", nom)
            }
            profCursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return null
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
        val dbPath = context.getDatabasePath(DB_NAME).absolutePath
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
                "SELECT id, nom, debut, fin, location, prof_id, Groupe FROM seances WHERE debut > ? ORDER BY debut ASC",
                arrayOf(now)
            )

            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)           // index 0 : id
                val nom = cursor.getString(1)       // index 1 : nom
                val debut = cursor.getString(2)     // index 2 : debut
                val fin = cursor.getString(3)       // index 3 : fin
                val location = cursor.getString(4)  // index 4 : location
                val prof = cursor.getInt(5)         // index 5 : prof_id
                val groupe = cursor.getString(6)

                if (groupe == "CM" || groupe == groupeTD || groupe == groupeTP) {
                    result.add(Seance(id, nom, debut, fin, location, prof, groupe))
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

    fun getProfNameById(context: Context, profId: Int): String {
        val dbPath = context.getDatabasePath(DB_NAME).absolutePath
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

        return try {
            val cursor = db.rawQuery("SELECT nom FROM profs WHERE id = ?", arrayOf(profId.toString()))
            if (cursor.moveToFirst()) {
                cursor.getString(0)
            } else {
                "Prof inconnu"
            }
        } catch (e: Exception) {
            "Erreur prof"
        } finally {
            db.close()
        }
    }

    fun getSeancesPourProf(context: Context, profId: String): List<Seance> {
        val dbPath = context.getDatabasePath(DB_NAME).absolutePath
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

        return try {
            val result = mutableListOf<Seance>()

            val now = java.time.LocalDateTime.now().toString()

            val cursor = db.rawQuery(
                "SELECT id, nom, debut, fin, location, prof_id, Groupe FROM seances  WHERE debut > ? AND prof_id = ? ORDER BY debut ASC",
                arrayOf(now, profId)
            )

            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val nom = cursor.getString(1)
                val debut = cursor.getString(2)
                val fin = cursor.getString(3)
                val location = cursor.getString(4)
                val prof = cursor.getInt(5)
                val groupe = cursor.getString(6)

                result.add(Seance(id, nom, debut, fin, location, prof, groupe))
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

    fun getEtudiantsParGroupe(context: Context, groupe: String): List<Pair<String, String>> {
        val db = openDatabase(context) ?: return emptyList()
        return try {
            val result = mutableListOf<Pair<String, String>>()
            val query: String
            val args: Array<String>

            if (groupe == "CM") {
                // Tous les étudiants
                query = "SELECT id, nom, prenom FROM etudiants"
                args = emptyArray()
            } else {
                // Groupe TD ou TP
                query = "SELECT id, nom, prenom FROM etudiants WHERE groupe_td = ? OR groupe_tp = ?"
                args = arrayOf(groupe, groupe)
            }

            val cursor = db.rawQuery(query, args)
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val nom = cursor.getString(1)
                val prenom = cursor.getString(2)
                result.add(id to "$prenom $nom")
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
