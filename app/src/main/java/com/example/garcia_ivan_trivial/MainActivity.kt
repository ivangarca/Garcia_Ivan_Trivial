package com.example.garcia_ivan_trivial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.garcia_ivan_trivial.repositori.UserRepository
// ---------------------------------------

import com.example.garcia_ivan_trivial.ui.theme.Garcia_Ivan_TrivialTheme
import com.example.garcia_ivan_trivial.view.ConnectorRoute
import com.example.garcia_ivan_trivial.dao.AppDatabase
import com.example.garcia_ivan_trivial.viewModel.LoginViewModel
import com.example.garcia_ivan_trivial.viewModel.TrivialViewModel



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Instanciamos la Base de Datos al arrancar la App
        val database = AppDatabase.getDatabase(this)

        // 2. Creamos los "Fabricantes" para inyectar los DAOs en los ViewModels
        val trivialViewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TrivialViewModel(application, database.preguntaDao()) as T // <-- AQUÍ
            }
        }

        val loginViewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(database.userDao()) as T
            }
        }

        enableEdgeToEdge()

        // --- PASO 6 PDF: INICIALIZAR DATOS DE PRUEBA EN SEGUNDO PLANO ---
        lifecycleScope.launch(Dispatchers.IO) {
            UserRepository.prepararDadesDeProva(database.userDao())
        }
        // -------------------------------------------------------------

        setContent {
            Garcia_Ivan_TrivialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // 3. Obtenemos los ViewModels usando los fabricantes que creamos arriba
                    val trivialViewModel: TrivialViewModel = viewModel(factory = trivialViewModelFactory)
                    val loginViewModel: LoginViewModel = viewModel(factory = loginViewModelFactory)

                    Box(modifier = Modifier.padding(innerPadding)) {
                        // 4. Le pasamos los ViewModels ya listos a la ruta
                        ConnectorRoute(
                            onCloseApp = { this@MainActivity.finish() },
                            loginViewModel = loginViewModel,
                            trivialViewModel = trivialViewModel
                        )
                    }
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