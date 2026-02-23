package com.example.garcia_ivan_trivial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box // <-- Importante: Añadimos Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.garcia_ivan_trivial.ui.theme.Garcia_Ivan_TrivialTheme

// Importamos tu pantalla principal
import com.example.garcia_ivan_trivial.view.ConnectorRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Garcia_Ivan_TrivialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // Envolvemos tu ruta en un Box que utiliza el innerPadding.
                    // ¡Así desaparece el error rojo!
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ConnectorRoute(
                            onCloseApp = { this@MainActivity.finish() }
                        )
                    }

                    // El código original sigue aquí, sano y salvo (comentado)
                    /* Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                    */
                }
            }
        }
    }
}

// TODO EL CÓDIGO DE ABAJO ESTÁ INTACTO

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Garcia_Ivan_TrivialTheme {
        Greeting("Android")
    }
}