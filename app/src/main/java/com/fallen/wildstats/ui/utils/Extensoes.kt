package com.fallen.wildstats.ui.utils

import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.exibirMensagem(mensagem: String) {
    Toast.makeText(
        requireContext(), // pega o context do fragmento
        mensagem,
        Toast.LENGTH_LONG
    ).show()
}
