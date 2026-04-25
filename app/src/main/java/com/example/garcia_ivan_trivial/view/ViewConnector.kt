package com.example.garcia_ivan_trivial.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.example.garcia_ivan_trivial.dao.AppDatabase
import com.example.garcia_ivan_trivial.viewModel.AppScreens
import com.example.garcia_ivan_trivial.viewModel.LoginEvent
import com.example.garcia_ivan_trivial.viewModel.LoginViewModel
import com.example.garcia_ivan_trivial.viewModel.TrivialViewModel

@Composable
fun ConnectorRoute(
    onCloseApp: () -> Unit,
    loginViewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    trivialViewModel: TrivialViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
){
    val uiStateCompose = loginViewModel.uiState.collectAsState()
    val trivialState = trivialViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val userDao = db.userDao()

    LaunchedEffect(Unit) {
        loginViewModel.eventFlow.collect { event ->
            if (event is LoginEvent.CloseApp) {
                onCloseApp()
            }
        }
    }

    when (uiStateCompose.value.screenState){
        AppScreens.LOGIN -> {
            ScreenLogin(
                state = uiStateCompose.value,
                onUsernameChange = loginViewModel::onUsernameChange,
                onPasswordChange = loginViewModel::onPasswordChange,
                onRegisterClick = { loginViewModel.onRegisterClick(userDao) },
                onLoginClick = { loginViewModel.onLoginClick(userDao) },
                onCloseClick = loginViewModel::onCloseClick,
                // Añadimos el "cable" para ir a Settings
                onSettingsClick = loginViewModel::onSettingsClick
            )
        }
        AppScreens.WELCOME -> {
            ScreenWelcome(
                state = uiStateCompose.value,
                onPartidaClick = loginViewModel::onStartTrivialClick,
                onCloseClick = loginViewModel::onCloseClick,
                msgWelcome = "Hola ${uiStateCompose.value.player1} i hola ${uiStateCompose.value.player2}!!",
                msgPartidesGuanyades = "Victòries -> ${uiStateCompose.value.player1}: ${uiStateCompose.value.player1Wins} | ${uiStateCompose.value.player2}: ${uiStateCompose.value.player2Wins}"
            )
        }
        AppScreens.TRIVIAL -> {
            com.example.garcia_ivan_trivial.view.trivial.ViewTrivial(
                loginState = uiStateCompose.value,
                trivialState = trivialState.value,
                onRespostaClick = trivialViewModel::comprovarResposta,
                onComodinClick = trivialViewModel::usarComodin5050,
                onPauseApp = trivialViewModel::pausarJoc,
                onResumeApp = trivialViewModel::reprendreJoc
            )
        }
        AppScreens.SETTINGS -> {
            // Esta es la única versión de SETTINGS que queda
            ScreenSettings(
                state = uiStateCompose.value,
                onTestApiClick = loginViewModel::probarConexionApi,
                onVolverClick = loginViewModel::onTornarLoginClick
            )
        }
        else -> {
            ScreenError(
                state = uiStateCompose.value,
                onCloseClick = loginViewModel::onCloseClick
            )
        }
    }
}