package com.example.garcia_ivan_trivial.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.garcia_ivan_trivial.model.Pregunta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TrivialUiState(
    val preguntaActual: Pregunta? = null,
    val indexPregunta: Int = 0,
    val puntsP1: Int = 0,
    val puntsP2: Int = 0,
    val esTornP1: Boolean = true,
    val jocFinalitzat: Boolean = false,
    val respostaSeleccionada: Int? = null,
    val mostrantResultat: Boolean = false
)

class TrivialViewModel : ViewModel() {

    private val llistaPreguntes = listOf(
        Pregunta("¿En qué año acabo la primera guerra mundial?", mapOf(1 to "1947", 2 to "1953", 3 to "1943", 4 to "1918"), 4),
        Pregunta("¿En qué año llegó el ser humano a la Luna por primera vez?", mapOf(1 to "1956", 2 to "1969", 3 to "1959", 4 to "1966"), 2),
        Pregunta("¿Cuál es el símbolo químico del hierro en la tabla periódica?", mapOf(1 to "fe", 2 to "au", 3 to "ag", 4 to "h1"), 1)
    ).shuffled()

    private val _uiState = MutableStateFlow(TrivialUiState(preguntaActual = llistaPreguntes[0]))
    val uiState = _uiState.asStateFlow()

    fun comprovarResposta(idResposta: Int) {
        val estatActual = _uiState.value

        if (estatActual.jocFinalitzat || estatActual.mostrantResultat) return

        val esCorrecta = idResposta == estatActual.preguntaActual?.respostaCorrecta

        _uiState.value = estatActual.copy(
            respostaSeleccionada = idResposta,
            mostrantResultat = true
        )

        viewModelScope.launch {
            delay(1000)// pausa 1 segundo

            var nousPuntsP1 = estatActual.puntsP1
            var nousPuntsP2 = estatActual.puntsP2

            if (esCorrecta) {
                if (estatActual.esTornP1) nousPuntsP1++ else nousPuntsP2++
            }

            if (nousPuntsP1 >= 2 || nousPuntsP2 >= 2) {
                _uiState.value = estatActual.copy(
                    puntsP1 = nousPuntsP1,
                    puntsP2 = nousPuntsP2,
                    jocFinalitzat = true,
                    mostrantResultat = false,
                    respostaSeleccionada = null
                )
            } else {
                val seguentIndex = (estatActual.indexPregunta + 1) % llistaPreguntes.size
                _uiState.value = estatActual.copy(
                    preguntaActual = llistaPreguntes[seguentIndex],
                    indexPregunta = seguentIndex,
                    puntsP1 = nousPuntsP1,
                    puntsP2 = nousPuntsP2,
                    esTornP1 = !estatActual.esTornP1,
                    mostrantResultat = false,
                    respostaSeleccionada = null
                )
            }
        }
    }
}