package com.fallen.wildstats

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class DrawerSelector {

    private var selectedItem: LinearLayout? = null

    fun atualizarSelecao(
        selecionado: LinearLayout,
        barras: Map<LinearLayout, View>,
        icones: Map<LinearLayout, ImageView>,
        textos: Map<LinearLayout, TextView>
    ) {
        val corSelecionado = Color.parseColor("#C4A878")
        val corNormal = Color.parseColor("#999999")

        if (selectedItem == selecionado) {

            return
        }

        selectedItem = selecionado

        barras.forEach { (item, barra) ->
            if (item == selecionado) {
                barra.setBackgroundColor(corSelecionado)
            } else {
                barra.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        icones.forEach { (item, icone) ->
            if (item == selecionado) {
                icone.setColorFilter(corSelecionado)
            } else {
                icone.setColorFilter(corNormal)
            }
        }

        textos.forEach { (item, texto) ->
            if (item == selecionado) {
                texto.setTextColor(corSelecionado)
            } else {
                texto.setTextColor(corNormal)
            }
        }
    }
}
