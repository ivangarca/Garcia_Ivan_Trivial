package com.example.garcia_ivan_trivial.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garcia_ivan_trivial.dao.UserDao
import com.example.garcia_ivan_trivial.model.User
import com.example.garcia_ivan_trivial.repositori.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppScreens {
    LOGIN, WELCOME, SETTINGS, TRIVIAL
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val message: String = "",
    val errorMsg: String = "",
    val player1: String = "",
    val player2: String = "",
    val player1Wins: Int = 0,
    val player2Wins: Int = 0,
    val screenState: AppScreens = AppScreens.LOGIN,
    val respostaSeleccionada: Int? = null,
    val mostrantResultat: Boolean = false
)

sealed interface LoginEvent {
    data object CloseApp : LoginEvent
}

class LoginViewModel(private val userDao: UserDao) : ViewModel() {

    private val loggedInUsers = mutableListOf<String>()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<LoginEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // L'usuari prova
    init {
        viewModelScope.launch(Dispatchers.IO) {
            UserRepository.prepararDadesDeProva(userDao)
        }
    }

    fun onUsernameChange(input: String) {
        _uiState.value = _uiState.value.copy(username = input, message = "", errorMsg = "")
    }

    fun onPasswordChange(input: String) {
        _uiState.value = _uiState.value.copy(password = input, message = "", errorMsg = "")
    }

    fun onRegisterClick(dao: UserDao) {
        val current = _uiState.value

        viewModelScope.launch(Dispatchers.IO) {
            val newUser = User(username = current.username, password = current.password, victorias = 0)
            val isAdded = UserRepository.addUser(newUser, dao)

            withContext(Dispatchers.Main) {
                if (isAdded) {
                    _uiState.value = current.copy(
                        message = "Usuari registrat correctament !!",
                        username = "",
                        password = "",
                        errorMsg = ""
                    )
                } else {
                    _uiState.value = current.copy(errorMsg = "ERROR: L'usuari ja existeix !!", message = "")
                }
            }
        }
    }

    fun onLoginClick(dao: UserDao) {
        val current = _uiState.value

        // 1. Anem al fil secundari (trastienda) per llegir de la BD
        viewModelScope.launch(Dispatchers.IO) {
            val storedUser = UserRepository.getUser(current.username, dao)

            // 2. Tornem al fil principal (mostrador) per actualitzar la UI
            withContext(Dispatchers.Main) {
                if (storedUser == null) {
                    _uiState.value = current.copy(errorMsg = "ERROR: L'usuari no existeix !!", message = "")
                } else if (storedUser.password != current.password) {
                    _uiState.value = current.copy(message = "", errorMsg = "ERROR: Credencials invàlides !!")
                } else {
                    if (loggedInUsers.contains(current.username)) {
                        _uiState.value = current.copy(
                            errorMsg = "Aquest usuari ja està connectat! Posa'n un altre.",
                            message = ""
                        )
                    } else {
                        loggedInUsers.add(current.username)

                        if (loggedInUsers.size == 1) {
                            _uiState.value = current.copy(
                                message = "Jugador 1 (${current.username}) connectat! Esperant al Jugador 2...",
                                errorMsg = "",
                                username = "",
                                password = "",
                                player1 = loggedInUsers[0],
                                player1Wins = storedUser.victorias
                            )
                        } else if (loggedInUsers.size == 2) {
                            _uiState.value = current.copy(
                                message = "Iniciant partida!!",
                                errorMsg = "",
                                username = "",
                                password = "",
                                screenState = AppScreens.WELCOME,
                                player1 = loggedInUsers[0],
                                player2 = loggedInUsers[1],
                                player2Wins = storedUser.victorias // Extraer les victòries de la BD
                            )
                        }
                    }
                }
            }
        }
    }

    fun onLogoutClick() {
        loggedInUsers.clear()
        _uiState.value = _uiState.value.copy(
            message = "", errorMsg = "", username = "", password = "", screenState = AppScreens.LOGIN
        )
    }

    fun onCloseClick() {
        viewModelScope.launch { _eventFlow.emit(LoginEvent.CloseApp) }
    }

    fun onStartTrivialClick() {
        _uiState.value = _uiState.value.copy(screenState = AppScreens.TRIVIAL)
    }
}