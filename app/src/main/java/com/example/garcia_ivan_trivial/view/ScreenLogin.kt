package com.example.garcia_ivan_trivial.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults // <-- Añadido para el color del botón
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.garcia_ivan_trivial.viewModel.LoginUiState

@Composable
fun ScreenLogin(
    state: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onCloseClick: () -> Unit,
    onSettingsClick: () -> Unit // <--- 1. AÑADE ESTO AQUÍ
){
    Column (
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = state.message,
            modifier = Modifier.background(Color.Cyan).padding(16.dp).fillMaxWidth()
        )

        OutlinedTextField( value = state.username, onValueChange = onUsernameChange, label = { Text("User") })
        OutlinedTextField( value = state.password, onValueChange = onPasswordChange, label = { Text("Pass") })

        Spacer(modifier = Modifier.height(80.dp))

        Row {
            Button(onClick = onRegisterClick) { Text("Crear usuari") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onLoginClick) { Text("Entrar") }
        }

        Button(onClick = onCloseClick) {Text("Tancar")}

        Spacer(modifier = Modifier.height(24.dp)) // Un poco de espacio

        // <--- 2. AÑADE EL BOTÓN DE CONFIGURACIÓN AQUÍ
        Button(
            onClick = { onSettingsClick() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("⚙️ Configuració i API")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = state.errorMsg,
            modifier = Modifier.background(Color(255,200,200)).padding(16.dp).fillMaxWidth()
        )
    }
}