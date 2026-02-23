package com.example.garcia_ivan_trivial.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.example.garcia_ivan_trivial.viewModel.LoginUiState

// COM PINTO LA PANTALLA?
@Composable
fun ScreenError(
    state: LoginUiState,
    onCloseClick: () -> Unit
){
    Column (
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error a la App",
            modifier = Modifier
                .background(Color.Cyan)
                .padding(16.dp)
                .fillMaxWidth()
        )

        Button(onClick = onCloseClick) {Text("Tancar")}
    }
}


