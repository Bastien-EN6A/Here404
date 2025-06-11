package com.ironmind.here.data

import androidx.compose.runtime.mutableStateOf
import java.time.LocalDate

/**
 * Classe singleton pour gérer l'état partagé de la page d'emploi du temps
 * Permet de conserver la date sélectionnée entre les navigations
 */
object ScheduleState {
    // Date sélectionnée dans l'emploi du temps, initialisée à la date du jour
    val selectedDate = mutableStateOf(LocalDate.now())
}
