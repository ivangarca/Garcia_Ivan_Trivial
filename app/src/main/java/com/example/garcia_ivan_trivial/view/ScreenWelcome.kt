package com.example.garcia_ivan_trivial.view



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.garcia_ivan_trivial.viewModel.LoginUiState


// COM PINTO LA PANTALLA?
@Composable
fun ScreenWelcome(
    state: LoginUiState,
    onPartidaClick: () -> Unit,
    onCloseClick: () -> Unit,
    msgWelcome: String,
    msgPartidesGuanyades: String
){
    Column (
        modifier = Modifier
            .fillMaxSize().padding(16.dp)
            .background(Color.Yellow),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = msgWelcome,
            color = Color.Yellow,
            fontSize = 24.sp,
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp)
                .fillMaxWidth()
        )
        Text(
            text = msgPartidesGuanyades,
            color = Color.Yellow,
            fontSize = 24.sp,
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp)
                .fillMaxWidth()
        )


        Button(onClick = onPartidaClick) {Text("Començar Partida")}


        Button(onClick = onCloseClick) {Text("Sortir")}


    }
}





