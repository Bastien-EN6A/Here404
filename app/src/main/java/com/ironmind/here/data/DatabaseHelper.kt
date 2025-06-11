package com.ironmind.here.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.FileOutputStream
import com.ironmind.here.model.Seance
import com.ironmind.here.sftpManager.ClearCache
import com.ironmind.here.sftpManager.DataDeleter
import com.ironmind.here.sftpManager.DataDownloader
import com.ironmind.here.sftpManager.DataUploader

object DatabaseHelper {

    const val DB_NAME = "bdd_fictive_avec_absences.db"

    // Copie la base de données depuis les assets si elle n'existe pas encore
    fun copyDatabaseIfNeeded(context: Context) {
        val dbPath = context.getDatabasePath(DB_NAME)

        Log.d("DatabaseHelper", "→ copyDatabaseIfNeeded() appelée, DB = $DB_NAME, existe = ${dbPath.exists()}")

        if (!dbPath.exists()) {
            dbPath.parentFile?.mkdirs()

            try {
                context.assets.open(DB_NAME).use { input ->
                    FileOutputStream(dbPath).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("DatabaseHelper", "Base de données $DB_NAME copiée avec succès.")
            } catch (e: Exception) {
                Log.e("DatabaseHelper", "Erreur lors de la copie de $DB_NAME : ${e.message}")
            }
        }
    }

    fun UpdateLocal(context: Context) {
        val TAG = "DatabaseHelper"
        ClearCache.clear(context)
        val success = DataDownloader.download(context)

        if (!success) {
            Log.e(TAG, "Téléchargement échoué")
            try {
                copyDatabaseIfNeeded(context)
            }catch (e:Exception){
                Log.e("DatabaseHelper", "Erreur lors de la copie de $DB_NAME : ${e.message}")
            }
        } else {
            Log.i(TAG, "Téléchargement réussi")
        }
    }





    fun UpdateRasp(context: Context) {
        val TAG = "DatabaseHelper"
        Thread {
            try {
                val deleter = DataDeleter()
                val deleted = deleter.deleteOnRaspberry()

                if (!deleted) {
                    Log.e(TAG, "Suppression distante échouée")
                }

                val uploader = DataUploader()
                val uploaded = uploader.upload(context)

                if (!uploaded) {
                    Log.e(TAG, "Upload échoué")
                } else {
                    Log.i(TAG, "Upload réussi vers le Raspberry")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Erreur UpdateRasp() : ${e.message}")
            }
        }.start()
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

    fun getEtudiantById(context: Context, id: String): Pair<String, String> {
        val db = openDatabase(context) ?: return Pair("Erreur", "Erreur")
        return try {
            val cursor = db.rawQuery(
                "SELECT nom, prenom FROM etudiants WHERE id = ?",
                arrayOf(id)
            )
            val result = if (cursor.moveToFirst()) {
                val nom = cursor.getString(0)
                val prenom = cursor.getString(1)
                Pair(nom, prenom)
            } else {
                Pair("Inconnu", "Inconnu")
            }
            cursor.close()
            result
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
            val cursor = db.rawQuery("SELECT nom FROM profs WHERE id = ?", arrayOf(profId.toString()))  //là y a un tostring
            val result = if (cursor.moveToFirst()) {
                cursor.getString(0)
            } else {
                "Prof inconnu"
            }
            cursor.close()
            result
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erreur dans getProfNameById : ${e.message}")
            "Erreur prof"
        } finally {
            db.close()
        }
    }

    fun getProfById(context: Context, id: String): Pair<String, String> {
        val db = openDatabase(context) ?: return Pair("Erreur", "Erreur")

        return try {
            val cursor = db.rawQuery(
                "SELECT nom, prenom FROM profs WHERE id = ?",
                arrayOf(id)
            )
            val result = if (cursor.moveToFirst()) {
                val nom = cursor.getString(0) ?: "Inconnu"
                val prenom = cursor.getString(1) ?: "Inconnu"
                Pair(nom, prenom)
            } else {
                Pair("Inconnu", "Inconnu")
            }
            cursor.close()
            result
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erreur getProfById: ${e.message}")
            Pair("Erreur", "Erreur")
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

    fun addEtudiantsAbs_seances(context: Context, etudiantId: String, seancesId: Int){
        val dbPath = context.getDatabasePath(DB_NAME).absolutePath
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)

        try {
            db.execSQL("INSERT INTO absences VALUES(?, ?)", arrayOf(etudiantId, seancesId.toString()))
        } catch (e: Exception) {
            Log.e("DB", "Erreur insertion absence", e)
        } finally {
            db.close()
        }
    }

    fun getNextSeanceForProf(context: Context, profId: String): Seance? {
        val dbPath = context.getDatabasePath(DB_NAME).absolutePath
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

        return try {
            val now = java.time.LocalDateTime.now().toString()
            val cursor = db.rawQuery(
                "SELECT id, nom, debut, fin, location, prof_id, Groupe FROM seances WHERE debut > ? AND prof_id = ? ORDER BY debut ASC LIMIT 1",
                arrayOf(now, profId)
            )
            if (cursor.moveToFirst()) {
                Seance(
                    id = cursor.getInt(0),
                    nom = cursor.getString(1),
                    debut = cursor.getString(2),
                    fin = cursor.getString(3),
                    location = cursor.getString(4),
                    prof_id = cursor.getInt(5),
                    groupe = cursor.getString(6)
                )
            } else null
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erreur getNextSeanceForProf: ${e.message}")
            null
        } finally {
            db.close()
        }
    }
    
    fun getNextSeanceForEtudiant(context: Context, etudiantId: String): Seance? {
        val dbPath = context.getDatabasePath(DB_NAME).absolutePath
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

        return try {
            // Récupérer les groupes de l'étudiant
            val etuCursor = db.rawQuery("SELECT groupe_td, groupe_tp FROM etudiants WHERE id = ?", arrayOf(etudiantId))
            if (!etuCursor.moveToFirst()) return null
            val groupeTD = etuCursor.getString(0)
            val groupeTP = etuCursor.getString(1)
            etuCursor.close()
            
            val now = java.time.LocalDateTime.now().toString()
            
            // Récupérer toutes les séances futures
            val cursor = db.rawQuery(
                "SELECT id, nom, debut, fin, location, prof_id, Groupe FROM seances WHERE debut > ? ORDER BY debut ASC",
                arrayOf(now)
            )
            
            var nextSeance: Seance? = null
            
            // Parcourir les résultats pour trouver la première séance applicable à l'étudiant
            while (cursor.moveToNext()) {
                val groupe = cursor.getString(6)
                
                // Vérifier si la séance s'applique à l'étudiant (CM ou son groupe TD/TP)
                if (groupe == "CM" || groupe == groupeTD || groupe == groupeTP) {
                    nextSeance = Seance(
                        id = cursor.getInt(0),
                        nom = cursor.getString(1),
                        debut = cursor.getString(2),
                        fin = cursor.getString(3),
                        location = cursor.getString(4),
                        prof_id = cursor.getInt(5),
                        groupe = groupe
                    )
                    break // On s'arrête à la première séance trouvée
                }
            }
            
            cursor.close()
            nextSeance
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erreur getNextSeanceForEtudiant: ${e.message}")
            null
        } finally {
            db.close()
        }
    }


    fun getAbsenceByEtudiantId(context: Context, etudiantId: String): Int {
        val dbPath = context.getDatabasePath(DB_NAME).absolutePath
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

        return try {
            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM absences WHERE etudiant_Id = ?",
                arrayOf(etudiantId)
            )
            var count = 0
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }
            cursor.close()
            count
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erreur getAbsenceByEtudiantId: ${e.message}")
            0
        } finally {
            db.close()
        }
    }

    fun getNombreSeancesPasseesPourEtudiant(context: Context, etudiantId: String): Int {
        val dbPath = context.getDatabasePath(DB_NAME).absolutePath
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

        return try {
            val now = java.time.LocalDateTime.now().toString()

            val etuCursor = db.rawQuery("SELECT groupe_td, groupe_tp FROM etudiants WHERE id = ?", arrayOf(etudiantId))
            if (!etuCursor.moveToFirst()) return 0
            val groupeTD = etuCursor.getString(0)
            val groupeTP = etuCursor.getString(1)
            etuCursor.close()

            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM seances WHERE debut < ? AND (Groupe = 'CM' OR Groupe = ? OR Groupe = ?)",
                arrayOf(now, groupeTD, groupeTP)
            )

            val total = if (cursor.moveToFirst()) cursor.getInt(0) else 0
            cursor.close()
            total
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erreur getNombreSeancesPasseesPourEtudiant: ${e.message}")
            0
        } finally {
            db.close()
        }
    }

    fun getTotalSeancesPourEtudiant(context: Context, etudiantId: String): Int {
        return getSeancesPourEtudiant(context, etudiantId).size
    }

    fun getPastSeancesGroupedByDebut(context: Context, profId: String): Map<String, List<Seance>> {
        val dbPath = context.getDatabasePath(DB_NAME).absolutePath
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

        return try {
            val now = java.time.LocalDateTime.now().toString()
            val cursor = db.rawQuery(
                "SELECT id, nom, debut, fin, location, prof_id, Groupe FROM seances WHERE debut < ? AND prof_id = ? ORDER BY debut DESC",
                arrayOf(now, profId)
            )

            val seanceList = mutableListOf<Seance>()
            while (cursor.moveToNext()) {
                val seance = Seance(
                    id = cursor.getInt(0),
                    nom = cursor.getString(1),
                    debut = cursor.getString(2),
                    fin = cursor.getString(3),
                    location = cursor.getString(4),
                    prof_id = cursor.getInt(5),
                    groupe = cursor.getString(6)
                )
                seanceList.add(seance)
            }

            // Regroupement par date+heure exacte de début (clé = début)
            seanceList.groupBy { seance ->
                seance.debut // format complet : "yyyy-MM-ddTHH:mm:ss"
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Erreur getPastSeancesGroupedByDebut: ${e.message}")
            emptyMap()
        } finally {
            db.close()
        }
    }

}
