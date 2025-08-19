package com.fallen.wildstats.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.fallen.wildstats.ui.model.Campeao
import java.io.File

object CampeoesManager {

    private val campeoesMap = mutableMapOf<String, Bitmap>()
    private val campeoesList = mutableListOf<Campeao>()

    fun preparar(context: Context) {
        val imagesDir = File(context.filesDir, "images")
        campeoesList.clear()
        campeoesList.addAll(CampeoesLoader.campeoesList)

        campeoesList.forEach { campeao ->
            val imageFile = File(imagesDir, "${campeao.Nome}.png")
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                campeoesMap[campeao.Nome] = bitmap
            }
        }
    }

    fun getBitmap(nome: String): Bitmap? {
        return campeoesMap[nome]
    }

    fun getAllCampeoes(): List<Campeao> = campeoesList

    fun getCampeoesByRota(rota: String): List<Campeao> =
        campeoesList.filter { it.Rota.contains(rota) }

    fun getCampeaoById(id: Int): Campeao? {
        return campeoesList.find { it.Id == id }
    }
}
