package com.example.garcia_ivan_trivial.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garcia_ivan_trivial.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppScreens{
    LOGIN,
    WELCOME,
    SETTINGS,

    TRIVIAL
}
// Estat inicial per la UI de Login.
// Els tres continguts en blanc.
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

// sealed: només pot ser un objecte definit a la mateixa llibreria que LoginEvent.
// A dins de la interfície li diem que només accepti CloseApp com a LoginEvent vàlid.
sealed interface LoginEvent {
    data object  CloseApp : LoginEvent
}

// ViewModel és una classe de Kotling per aplicacions.
// Aquí estem creant una extensió d'aquesta classe.
class LoginViewModel : ViewModel() {
    // La lògica de la nostra App ha de tenir el Hashmap de clients:
    private val users = mutableMapOf<String, User>()

    private val loggedInUsers = mutableListOf<String>()

    // L'estat de l'aplicació és privat, només el viewmodel el pot canviar
    // Però la vista l'ha de poder veure.
    // Per assegurar que la vista veu però no canvia, creem _uiState i uiState.

    //creo dos para no tener que registralos
    init {
        users["ivan"] = User("ivan", "1234", 0)
        users["mario"] = User("mario", "1234", 0)
    }
    private val _uiState = MutableStateFlow(LoginUiState())

    // Iniciem tot en blanc amb el LoginUiState, però serà mutable.
    val uiState = _uiState.asStateFlow() // Aquesta, agafará sempre el valor de la privada

    // Configuració dels events que ens poden canviar la App IMPERATIVAMENT
    // Igual que els estats pero Shared en lloc de States.
    private val _eventFlow = MutableSharedFlow<LoginEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onUsernameChange(input: String) {
        _uiState.value = _uiState.value.copy(input, message = "", errorMsg = "")
    }

    fun onPasswordChange(input: String) {
        _uiState.value = _uiState.value.copy(password = input, message = "", errorMsg = "")
    }

    fun onRegisterClick() {
        val current = _uiState.value
        if (users.containsKey(current.username)) {
            _uiState.value = current.copy(errorMsg = "ERROR: L'usuari ja existeix !!", message = "")
        } else {
            users[current.username] = User(current.username, current.password, 0)
            _uiState.value = current.copy(
                message = "Usuari registrat correctament !!",
                username = "",
                password = "",
                errorMsg = ""
            )
        }
    }

    fun onLoginClick() {
        val current = _uiState.value
        val storedUser = users[current.username] //Això pot ser null !!
        if (storedUser == null) {
            _uiState.value = current.copy(errorMsg = "ERROR: L'usuari no existeix !!", message = "")
        } else if (storedUser.password != current.password) {
            _uiState.value = current.copy(message = "", errorMsg = "ERROR: Credencials invàlides !!")
        } else {

            // 1. Comprobamos si este usuario ya había entrado antes
            if (loggedInUsers.contains(current.username)) {
                _uiState.value = current.copy(
                    errorMsg = "Aquest usuari ja està connectat! Posa'n un altre.",
                    message = ""
                )
            }else {
                // 2. SI NO ESTÁ CONECTADO, ENTRA POR ESTE ELSE (sin necesidad de returns)
                loggedInUsers.add(current.username)

                // 3. Comprobamos cuántos jugadores tenemos listos
                if (loggedInUsers.size == 1) {
                    _uiState.value = current.copy(
                        message = "Jugador 1 (${current.username}) connectat! Esperant al Jugador 2...",
                        errorMsg = "",
                        username = "",
                        password = "",
                        player1 = loggedInUsers[0] // Guardamos de forma segura
                    )
                } else if (loggedInUsers.size == 2) {
                    // ¡Ya tenemos 2 jugadores! Cambiamos de pantalla.
                    _uiState.value = current.copy(
                        message = "Iniciant partida!!",
                        errorMsg = "",
                        username = "",
                        password = "",
                        screenState = AppScreens.WELCOME,
                        player1 = loggedInUsers[0],
                        player2 = loggedInUsers[1]
                    )
                }
            }
        }
    }
    fun onLogoutClick() {
        loggedInUsers.clear()
        _uiState.value = _uiState.value.copy(
            message = "",
            errorMsg = "",
            username = "",
            password = "",
            screenState = AppScreens.LOGIN
        )
    }

    fun onCloseClick() {
        viewModelScope.launch { _eventFlow.emit(LoginEvent.CloseApp) }
    }
    fun onStartTrivialClick() {
        _uiState.value = _uiState.value.copy(screenState = AppScreens.TRIVIAL)
    }
}