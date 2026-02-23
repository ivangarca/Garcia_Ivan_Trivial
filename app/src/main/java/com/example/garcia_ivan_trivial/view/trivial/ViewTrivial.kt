package com.example.garcia_ivan_trivial.view.trivial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.garcia_ivan_trivial.viewModel.LoginUiState
import com.example.garcia_ivan_trivial.viewModel.TrivialUiState

@Composable
fun ViewTrivial(
    loginState: LoginUiState, //coge los nombres de los jugadores
    trivialState: TrivialUiState, // saca la pregunta, de quien es el turno y si la partdia ha terminado
    onRespostaClick: (Int) -> Unit // lama al TrivialViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text( //mensaje jugador 1
            text = "Jugador 1: ${loginState.player1} (${trivialState.puntsP1} pts)",
            fontSize = 20.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(if (trivialState.esTornP1) Color.Green else Color.LightGray) //cambia el color depende el boolean
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        if (!trivialState.jocFinalitzat) { //muestra una pregunta y sus respuestas
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = trivialState.preguntaActual?.enunciat ?: "", fontSize = 24.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))

                trivialState.preguntaActual?.opcions?.forEach { (id, texto) ->
                    Button(onClick = { onRespostaClick(id) }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) {
                        Text(text = texto)
                    }
                }
            }
        } else { // si el juego ha acabado muestre ese mensaje
            Text(text = "PARTIDA FINALITZADA", fontSize = 30.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        Text( // mensaje jugador 2
            text = "Jugador 2: ${loginState.player2} (${trivialState.puntsP2} pts)",
            fontSize = 20.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(if (!trivialState.esTornP1) Color(0xFFFFA500) else Color.LightGray) //cambia color depende el boolean
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}