package com.fallen.wildstats.ui.model

data class Campeao(
    val Nome: String,
    val Id: Int,
    val Dif: Int,
    val Dano: Int,
    val Resistencia: Int,
    val Utilidade: Int,
    val Titulo: String,
    val Historia: String,
    val Classe: List<String>,
    val Rota: List<String>,
    val imgurl: String,
    var imgHash: String = "",
    var capaHash: String = "",
    val Dda: String,
    val Armadura: String,
    val Vida: String,
    val VidaRegen: String,
    val ManaRegen: String,
    val RM: String,
    val VDM: String,
    val Mana: String,
    val VDA: String
)
