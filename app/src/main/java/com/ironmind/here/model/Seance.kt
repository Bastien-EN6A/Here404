package com.ironmind.here.model

data class Seance(
    val id: Int,
    val nom: String,
    val debut: String,
    val fin: String,
    val location: String,
    val prof_id: Int,
    val groupe: String
)

