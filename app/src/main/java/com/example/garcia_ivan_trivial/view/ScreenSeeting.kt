package com.example.garcia_ivan_trivial.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.garcia_ivan_trivial.viewModel.LoginUiState

@Composable
fun ScreenSettings(
    state: LoginUiState,
    onTestApiClick: () -> Unit,
    onVolverClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Panell de Configuració i API",
            fontSize = 26.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(onClick = { onTestApiClick() }) {
            Text("Provar Connexió API (GitHub)", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Aquí mostramos el mensaje (verde si va bien, rojo si hay error)
        Text(
            text = state.apiMessage,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = if (state.apiMessage.contains("Error")) Color.Red else Color(0xFF006400)
        )

        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = { onVolverClick() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("Tornar al Menú", fontSize = 16.sp)
        }
    }
}