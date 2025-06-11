package com.ironmind.here.model

data class SeanceStats(
    val seance: Seance,
    val absents: Int,
    val presents: Int
)
