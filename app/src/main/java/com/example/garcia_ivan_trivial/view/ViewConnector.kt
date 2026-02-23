package com.example.garcia_ivan_trivial.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.garcia_ivan_trivial.viewModel.AppScreens
import com.example.garcia_ivan_trivial.viewModel.LoginEvent
import com.example.garcia_ivan_trivial.viewModel.LoginViewModel
import com.example.garcia_ivan_trivial.viewModel.TrivialViewModel

@Composable
fun ConnectorRoute(
    onCloseApp: () -> Unit,
    loginViewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    // 2. AÑADE ESTE PARÁMETRO
    trivialViewModel: TrivialViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
){
    val uiStateCompose = loginViewModel.uiState.collectAsState()
    val trivialState = trivialViewModel.uiState.collectAsState()

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
                onRegisterClick = loginViewModel::onRegisterClick,
                onLoginClick = loginViewModel::onLoginClick,
                onCloseClick = loginViewModel::onCloseClick
            )
        }
        AppScreens.WELCOME -> {
            ScreenWelcome(
                state = uiStateCompose.value,
                onPartidaClick = loginViewModel::onStartTrivialClick,
                onCloseClick = loginViewModel::onCloseClick,
                msgWelcome = "Hola ${uiStateCompose.value.player1} i hola ${uiStateCompose.value.player2}!!",
                msgPartidesGuanyades = "Victòries -> ${uiStateCompose.value.player1}: ${uiStateCompose.value.player1Wins} | ${uiStateCompose.value.player2}: ${uiStateCompose.value.player2Wins}")
        }
        AppScreens.SETTINGS -> {
            ScreenWelcome(
                state = uiStateCompose.value,
                onPartidaClick = loginViewModel::onLogoutClick,
                onCloseClick = loginViewModel::onCloseClick,
                msgWelcome = "Funcionalitat en construcció",
                msgPartidesGuanyades = ""
            )
        }
        AppScreens.TRIVIAL -> {
            com.example.garcia_ivan_trivial.view.trivial.ViewTrivial(
                loginState = uiStateCompose.value,
                trivialState = trivialState.value,
                onRespostaClick = trivialViewModel::comprovarResposta
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