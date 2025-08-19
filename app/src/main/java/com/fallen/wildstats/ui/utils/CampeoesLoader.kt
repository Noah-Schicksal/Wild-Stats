package com.fallen.wildstats.ui.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.fallen.wildstats.ui.model.Campeao
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object CampeoesLoader {

    private const val PREFS_NAME = "CampeoesPrefs"
    private const val LOCAL_FILE_NAME = "campeoes.json"
    private const val STORAGE_PATH = "json-campeões/campeoes.json"
    private const val IMAGES_DIR = "images"
    private const val CAPAS_DIR = "capas"
    private const val DEBOUNCE_INTERVAL = 24 * 60 * 60 * 1000L // 24h em ms

    var campeoesList: List<Campeao> = emptyList()

    fun loadCampeoes(
        context: Context,
        onComplete: () -> Unit,
        onStartDownload: (() -> Unit)? = null
    ) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val localFile = File(context.filesDir, LOCAL_FILE_NAME)

        // 🔹 Copia campeoes.json dos assets se não existir
        copyAssetIfNotExists(context, LOCAL_FILE_NAME, localFile)

        // 🔹 Copia imagens dos assets se não existirem
        val imagesDir = File(context.filesDir, IMAGES_DIR)
        if (!imagesDir.exists()) imagesDir.mkdirs()
        context.assets.list(IMAGES_DIR)?.forEach { fileName ->
            val destFile = File(imagesDir, fileName)
            copyAssetIfNotExists(context, "$IMAGES_DIR/$fileName", destFile)
        }

        // 🔹 Copia capas dos assets se não existirem
        val capasDir = File(context.filesDir, CAPAS_DIR)
        if (!capasDir.exists()) capasDir.mkdirs()
        context.assets.list(CAPAS_DIR)?.forEach { fileName ->
            val destFile = File(capasDir, fileName)
            copyAssetIfNotExists(context, "$CAPAS_DIR/$fileName", destFile)
        }

        // 🔹 Log resumo dos assets
        Log.d("CampeoesLoader", "📂 Lista de assets copiados/localizados:")
        Log.d("CampeoesLoader", "JSON: $LOCAL_FILE_NAME -> ${localFile.absolutePath}")
        context.assets.list(IMAGES_DIR)?.forEach { fileName ->
            val file = File(imagesDir, fileName)
            Log.d("CampeoesLoader", "Imagem: $fileName -> ${file.absolutePath} (existe=${file.exists()})")
        }
        context.assets.list(CAPAS_DIR)?.forEach { fileName ->
            val file = File(capasDir, fileName)
            Log.d("CampeoesLoader", "Capa: $fileName -> ${file.absolutePath} (existe=${file.exists()})")
        }

        // 🔹 Carrega cache local
        if (localFile.exists()) {
            campeoesList = parseJson(localFile.readText())
        }

        // 🔹 Checa debounce
        val lastCheck = prefs.getLong("last_json_check", 0L)
        val now = System.currentTimeMillis()
        if (now - lastCheck < DEBOUNCE_INTERVAL) {
            onComplete()
            return
        }
        prefs.edit().putLong("last_json_check", now).apply()

        // 🔹 Checa metadados no Firebase
        val storageRef = FirebaseStorage.getInstance().reference.child(STORAGE_PATH)
        storageRef.metadata.addOnSuccessListener { metadata ->
            val remoteSize = metadata.sizeBytes
            val remoteUpdated = metadata.updatedTimeMillis

            val localSize = prefs.getLong("local_size", -1)
            val localUpdated = prefs.getLong("local_updated", -1)

            if (remoteSize != localSize || remoteUpdated != localUpdated) {
                onStartDownload?.invoke()
                storageRef.getFile(localFile)
                    .addOnSuccessListener {
                        prefs.edit()
                            .putLong("local_size", remoteSize)
                            .putLong("local_updated", remoteUpdated)
                            .apply()

                        campeoesList = parseJson(localFile.readText())

                        // Baixa imagens normais e capas
                        downloadImages(context, campeoesList) {
                            onComplete()
                        }
                    }
                    .addOnFailureListener {
                        Log.e("CampeoesLoader", "Erro ao baixar campeoes.json", it)
                        onComplete()
                    }
            } else {
                onComplete()
            }
        }.addOnFailureListener {
            Log.e("CampeoesLoader", "Erro ao obter metadados do arquivo", it)
            onComplete()
        }
    }

    private fun parseJson(json: String): List<Campeao> {
        val mapType = object : TypeToken<Map<String, List<Campeao>>>() {}.type
        val map: Map<String, List<Campeao>> = Gson().fromJson(json, mapType)
        return map["campeoes"]?.map { campeao ->
            campeao.apply {
                if (imgHash == null) imgHash = ""
                if (capaHash == null) capaHash = ""
            }
        } ?: emptyList()
    }

    private fun downloadImages(context: Context, campeoes: List<Campeao>, onComplete: () -> Unit) {
        val imagesDir = File(context.filesDir, IMAGES_DIR)
        if (!imagesDir.exists()) imagesDir.mkdirs()

        val capasDir = File(context.filesDir, CAPAS_DIR)
        if (!capasDir.exists()) capasDir.mkdirs()

        val storage = FirebaseStorage.getInstance().reference
        var pendingDownloads = campeoes.size * 2 // imagens + capas

        if (pendingDownloads == 0) {
            onComplete()
            return
        }

        campeoes.forEach { campeao ->
            // 🔹 Imagem normal
            val imgFile = File(imagesDir, "${campeao.Nome}.png")
            val imgHashStored = getStoredHash(context, campeao.Nome, false)
            val needsDownloadImg = !imgFile.exists() || campeao.imgHash != imgHashStored

            if (needsDownloadImg) {
                storage.child("imagens-campeões/${campeao.Nome}.png").getFile(imgFile)
                    .addOnSuccessListener {
                        saveHash(context, campeao.Nome, campeao.imgHash ?: "", false)
                        pendingDownloads--
                        if (pendingDownloads == 0) onComplete()
                    }
                    .addOnFailureListener {
                        Log.e("CampeoesLoader", "Erro ao baixar imagem: ${campeao.Nome}.png", it)
                        pendingDownloads--
                        if (pendingDownloads == 0) onComplete()
                    }
            } else {
                pendingDownloads--
                if (pendingDownloads == 0) onComplete()
            }

            // 🔹 Capa
            val capaFile = File(capasDir, "${campeao.Nome}_capa.png")
            val capaHashStored = getStoredHash(context, campeao.Nome, true)
            val needsDownloadCapa = !capaFile.exists() || campeao.capaHash != capaHashStored

            if (needsDownloadCapa) {
                storage.child("campeões-capas/${campeao.Nome}_capa.png").getFile(capaFile)
                    .addOnSuccessListener {
                        saveHash(context, campeao.Nome, campeao.capaHash ?: "", true)
                        pendingDownloads--
                        if (pendingDownloads == 0) onComplete()
                    }
                    .addOnFailureListener {
                        Log.e("CampeoesLoader", "Erro ao baixar capa: ${campeao.Nome}_capa.png", it)
                        pendingDownloads--
                        if (pendingDownloads == 0) onComplete()
                    }
            } else {
                pendingDownloads--
                if (pendingDownloads == 0) onComplete()
            }
        }
    }

    private fun getStoredHash(context: Context, campeaoNome: String, isCapa: Boolean): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = if (isCapa) "capaHash_$campeaoNome" else "imgHash_$campeaoNome"
        return prefs.getString(key, "") ?: ""
    }

    private fun saveHash(context: Context, campeaoNome: String, hash: String, isCapa: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = if (isCapa) "capaHash_$campeaoNome" else "imgHash_$campeaoNome"
        prefs.edit().putString(key, hash).apply()
    }

    private fun copyAssetIfNotExists(context: Context, assetPath: String, destFile: File) {
        if (!destFile.exists()) {
            try {
                context.assets.open(assetPath).use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("CampeoesLoader", "Copiado asset: $assetPath -> ${destFile.absolutePath}")
            } catch (e: Exception) {
                Log.e("CampeoesLoader", "Erro ao copiar asset: $assetPath", e)
            }
        } else {
            Log.d("CampeoesLoader", "ℹ️ Asset já existe: $assetPath")
        }
    }
}
