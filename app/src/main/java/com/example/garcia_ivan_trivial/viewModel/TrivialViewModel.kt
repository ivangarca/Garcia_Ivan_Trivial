package com.example.garcia_ivan_trivial.viewModel

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.garcia_ivan_trivial.R
import com.example.garcia_ivan_trivial.dao.PreguntaDao
import com.example.garcia_ivan_trivial.model.Pregunta
import com.example.garcia_ivan_trivial.model.Tematica
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TrivialUiState(
    val preguntaActual: Pregunta? = null,
    val indexPregunta: Int = 0,
    val tematiquesP1: Set<Tematica> = emptySet(),
    val tematiquesP2: Set<Tematica> = emptySet(),
    val esTornP1: Boolean = true,
    val jocFinalitzat: Boolean = false,
    val respostaSeleccionada: Int? = null,
    val mostrantResultat: Boolean = false,
    val comodinP1Usado: Boolean = false,
    val comodinP2Usado: Boolean = false,
    val opcionesDeshabilitadas: List<Int> = emptyList(),
    val jocPausat: Boolean = false,
    val tempsPartida: Long = 0L
)

class TrivialViewModel(
    application: Application,
    private val preguntaDao: PreguntaDao
) : AndroidViewModel(application) {

    private var llistaPreguntes: List<Pregunta> = emptyList()
    private val _uiState = MutableStateFlow(TrivialUiState())
    val uiState = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<Int, Int>()

    // --- VARIABLES PARA EL AUDIO FOCUS ---
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    // Guardamos el momento exacto en el que se crea el ViewModel (empieza el juego)
    private var startTime = System.currentTimeMillis()

    init {
        cargarJuego()
        inicialitzarAudio()
    }

    override fun onCleared() {
        super.onCleared()
        // Liberamos el foco de audio al destruir el ViewModel
        controlarMusicaFons(false)
        mediaPlayer?.release()
        mediaPlayer = null
        soundPool?.release()
        soundPool = null
    }

    private fun inicialitzarAudio() {
        val context = getApplication<Application>()
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        mediaPlayer = MediaPlayer.create(context, R.raw.inazuma_chill)
        mediaPlayer?.isLooping = true
        mediaPlayer?.setVolume(0.8f, 0.8f)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.let { sp ->
            soundMap[1] = sp.load(context, R.raw.sonido_win, 1)
            soundMap[2] = sp.load(context, R.raw.pou_no_sound_effect, 1)
        }
    }

    private fun cargarJuego() {
        viewModelScope.launch {
            if (preguntaDao.getCount() == 0) {
                // Asumo que 'llistaPreguntesHardcoded' está definida en otro lugar o en el DAO
                // preguntaDao.insertAll(llistaPreguntesHardcoded)
            }
            llistaPreguntes = preguntaDao.getAllPreguntas().shuffled()
            if (llistaPreguntes.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    preguntaActual = llistaPreguntes[0]
                )
                controlarMusicaFons(true)
            }
        }
    }

    fun comprovarResposta(idResposta: Int) {
        val estatActual = _uiState.value
        if (estatActual.jocFinalitzat || estatActual.mostrantResultat) {
            return
        }

        val pregunta = estatActual.preguntaActual ?: return
        val esCorrecta = idResposta == pregunta.respostaCorrecta

        if (esCorrecta) {
            reproduirSo(1)
        } else {
            reproduirSo(2)
        }

        _uiState.value = estatActual.copy(
            respostaSeleccionada = idResposta,
            mostrantResultat = true
        )

        viewModelScope.launch {
            delay(1000)

            var nuevasTematicasP1 = estatActual.tematiquesP1
            var nuevasTematicasP2 = estatActual.tematiquesP2

            if (esCorrecta) {
                if (estatActual.esTornP1) {
                    nuevasTematicasP1 = nuevasTematicasP1 + pregunta.tematica
                } else {
                    nuevasTematicasP2 = nuevasTematicasP2 + pregunta.tematica
                }
            }

            // --- COMPROBACIÓN DE FINAL DE PARTIDA ---
            if (nuevasTematicasP1.size >= 5 || nuevasTematicasP2.size >= 5) {

                val segundosTotales = (System.currentTimeMillis() - startTime) / 1000

                _uiState.value = estatActual.copy(
                    tematiquesP1 = nuevasTematicasP1,
                    tematiquesP2 = nuevasTematicasP2,
                    jocFinalitzat = true,
                    tempsPartida = segundosTotales,
                    mostrantResultat = false,
                    respostaSeleccionada = null
                )

                // Paramos la música al terminar
                controlarMusicaFons(false)

            } else {
                val seguentIndex = (estatActual.indexPregunta + 1) % llistaPreguntes.size
                _uiState.value = estatActual.copy(
                    preguntaActual = llistaPreguntes[seguentIndex],
                    indexPregunta = seguentIndex,
                    tematiquesP1 = nuevasTematicasP1,
                    tematiquesP2 = nuevasTematicasP2,
                    esTornP1 = !estatActual.esTornP1,
                    mostrantResultat = false,
                    respostaSeleccionada = null,
                    opcionesDeshabilitadas = emptyList()
                )
            }
        }
    }

    fun usarComodin5050() {
        val estatActual = _uiState.value
        val pregunta = estatActual.preguntaActual ?: return

        val yaUsado = if (estatActual.esTornP1) {
            estatActual.comodinP1Usado
        } else {
            estatActual.comodinP2Usado
        }

        if (yaUsado || estatActual.mostrantResultat || estatActual.jocFinalitzat) return

        val opcionesIncorrectas = pregunta.opcions.keys.filter {
            it != pregunta.respostaCorrecta
        }.shuffled()

        val aEliminar = opcionesIncorrectas.take(2)

        _uiState.value = estatActual.copy(
            comodinP1Usado = if (estatActual.esTornP1) true else estatActual.comodinP1Usado,
            comodinP2Usado = if (!estatActual.esTornP1) true else estatActual.comodinP2Usado,
            opcionesDeshabilitadas = aEliminar
        )
    }

    private fun reproduirSo(soId: Int) {
        val soundId = soundMap[soId]
        if (soundId != null && soundId != 0) {
            soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
        }
    }

    // --- CONTROL DE MÚSICA CON AUDIO FOCUS ---
    fun controlarMusicaFons(play: Boolean) {
        if (play) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener { focusChange ->
                        when (focusChange) {
                            // Si perdemos el foco (ej. llamada o abren Spotify), pausamos
                            AudioManager.AUDIOFOCUS_LOSS,
                            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                                mediaPlayer?.pause()
                            }
                            // Si recuperamos el foco y el juego no está en pausa, reanudamos
                            AudioManager.AUDIOFOCUS_GAIN -> {
                                if (!_uiState.value.jocPausat) {
                                    mediaPlayer?.start()
                                }
                            }
                        }
                    }
                    .build()

                val res = audioManager?.requestAudioFocus(audioFocusRequest!!)
                if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    if (mediaPlayer?.isPlaying == false) {
                        mediaPlayer?.start()
                    }
                }
            } else {
                // Compatibilidad con versiones más antiguas de Android
                if (mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start()
                }
            }
        } else {
            // Detenemos música y liberamos el foco
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
                audioManager?.abandonAudioFocusRequest(audioFocusRequest!!)
            }
        }
    }

    fun pausarJoc() {
        if (_uiState.value.jocFinalitzat) return
        _uiState.value = _uiState.value.copy(
            jocPausat = true
        )
        controlarMusicaFons(false)
    }

    fun reprendreJoc() {
        _uiState.value = _uiState.value.copy(
            jocPausat = false
        )
        controlarMusicaFons(true)
    }


    private val llistaPreguntesHardcoded = listOf(
        Pregunta(1, "¿En qué año acabo la primera guerra mundial?", mapOf(1 to "1947", 2 to "1953", 3 to "1943", 4 to "1918"), 4, Tematica.CULTURA_GENERAL),
        Pregunta(2, "¿En qué año llegó el ser humano a la Luna por primera vez?", mapOf(1 to "1956", 2 to "1969", 3 to "1959", 4 to "1966"), 2, Tematica.CULTURA_GENERAL),
        Pregunta(3, "¿Cuál es el símbolo químico del hierro en la tabla periódica?", mapOf(1 to "fe", 2 to "au", 3 to "ag", 4 to "h1"), 1, Tematica.CULTURA_GENERAL),
        Pregunta(4, "¿Cuál es el océano más grande del mundo?", mapOf(1 to "Atlántico", 2 to "Índico", 3 to "Ártico", 4 to "Pacífico"), 4, Tematica.CULTURA_GENERAL),
        Pregunta(5, "¿Quién escribió 'Don Quijote de la Mancha'?", mapOf(1 to "Lope de Vega", 2 to "Miguel de Cervantes", 3 to "García Lorca", 4 to "García Márquez"), 2, Tematica.CULTURA_GENERAL, imatgeResId = R.drawable.don_quijote),
        Pregunta(6, "¿Cuál es el planeta más grande del sistema solar?", mapOf(1 to "Tierra", 2 to "Marte", 3 to "Júpiter", 4 to "Saturno"), 3, Tematica.CULTURA_GENERAL),
        Pregunta(7, "¿En qué país se encuentra la famosa Torre de Pisa?", mapOf(1 to "Francia", 2 to "Italia", 3 to "España", 4 to "Grecia"), 2, Tematica.CULTURA_GENERAL),
        Pregunta(8, "¿Cuál es el río más largo y caudaloso del mundo?", mapOf(1 to "Nilo", 2 to "Amazonas", 3 to "Yangtsé", 4 to "Misisipi"), 2, Tematica.CULTURA_GENERAL),
        Pregunta(9, "¿Qué idioma tiene más hablantes nativos en el mundo?", mapOf(1 to "Inglés", 2 to "Español", 3 to "Chino Mandarín", 4 to "Hindi"), 3, Tematica.CULTURA_GENERAL),
        Pregunta(10, "¿Cuál es el hueso más largo del cuerpo humano?", mapOf(1 to "Fémur", 2 to "Húmero", 3 to "Tibia", 4 to "Peroné"), 1, Tematica.CULTURA_GENERAL),
        Pregunta(11, "¿Qué legendaria banda británica compuso la canción 'Bohemian Rhapsody'?", mapOf(1 to "The Beatles", 2 to "Queen", 3 to "Rolling Stones", 4 to "Pink Floyd"), 2, Tematica.MUSICA),
        Pregunta(12, "¿A qué cantante se le conoce mundialmente como el 'Rey del Pop'?", mapOf(1 to "Prince", 2 to "Elvis Presley", 3 to "Michael Jackson", 4 to "Bruno Mars"), 3, Tematica.MUSICA),
        Pregunta(13, "¿De qué ciudad inglesa es originaria la banda The Beatles?", mapOf(1 to "Londres", 2 to "Mánchester", 3 to "Liverpool", 4 to "Birmingham"), 3, Tematica.MUSICA),
        Pregunta(14, "¿Qué cantante española lanzó el exitoso álbum 'Motomami' en 2022?", mapOf(1 to "Aitana", 2 to "Bad Gyal", 3 to "Lola Índigo", 4 to "Rosalía"), 4, Tematica.MUSICA),
        Pregunta(15, "¿Cuál es el nombre del cantante principal de la banda Nirvana, fallecido en 1994?", mapOf(1 to "Axl Rose", 2 to "Kurt Cobain", 3 to "Eddie Vedder", 4 to "Chris Cornell"), 2, Tematica.MUSICA),
        Pregunta(16, "¿Qué cantante colombiana es famosa por canciones como 'Hips Don't Lie' y 'Waka Waka'?", mapOf(1 to "Karol G", 2 to "Shakira", 3 to "Becky G", 4 to "Nathy Peluso"), 2, Tematica.MUSICA),
        Pregunta(17, "¿Qué exitosa cantante estadounidense protagoniza la mastodóntica gira 'The Eras Tour'?", mapOf(1 to "Taylor Swift", 2 to "Beyoncé", 3 to "Ariana Grande", 4 to "Katy Perry"), 1, Tematica.MUSICA),
        Pregunta(18, "¿A quién se le considera indiscutiblemente el 'Rey del Rock and Roll'?", mapOf(1 to "Chuck Berry", 2 to "Johnny Cash", 3 to "Elvis Presley", 4 to "Jerry Lee Lewis"), 3, Tematica.MUSICA),
        Pregunta(19, "¿Qué banda británica liderada por Chris Martin es conocida por el tema 'Viva la Vida'?", mapOf(1 to "U2", 2 to "Maroon 5", 3 to "Imagine Dragons", 4 to "Coldplay"), 4, Tematica.MUSICA),
        Pregunta(20, "¿A qué icónica artista se le atribuye el título de la 'Reina del Pop'?", mapOf(1 to "Madonna", 2 to "Lady Gaga", 3 to "Britney Spears", 4 to "Whitney Houston"), 1, Tematica.MUSICA),
        Pregunta(21, "¿Cuál es el Pokémon número 001 en la Pokédex nacional?", mapOf(1 to "Pikachu", 2 to "Charmander", 3 to "Bulbasaur", 4 to "Squirtle"), 3, Tematica.VIDEOJOC),
        Pregunta(22, "¿Cómo se llama el hermano de Mario, conocido por su característica ropa verde?", mapOf(1 to "Wario", 2 to "Yoshi", 3 to "Toad", 4 to "Luigi"), 4, Tematica.VIDEOJOC),
        Pregunta(23, "¿Qué empresa desarrolla la popular saga de videojuegos de fútbol 'EA Sports FC' (anteriormente conocida como FIFA)?", mapOf(1 to "Konami", 2 to "Electronic Arts", 3 to "Ubisoft", 4 to "2K Games"), 2, Tematica.VIDEOJOC),
        Pregunta(24, "¿Cuál es el videojuego más vendido de todos los tiempos a nivel mundial?", mapOf(1 to "Tetris", 2 to "Grand Theft Auto V", 3 to "Minecraft", 4 to "Wii Sports"), 3, Tematica.VIDEOJOC),
        Pregunta(25, "En la saga 'The Legend of Zelda', ¿cómo se llama el reino principal donde se desarrollan la mayoría de las aventuras?", mapOf(1 to "Hyrule", 2 to "Termina", 3 to "Lorule", 4 to "Kanto"), 1, Tematica.VIDEOJOC, imatgeResId = R.drawable.link),
        Pregunta(26, "¿Quién es el espartano protagonista principal de la saga 'God of War'?", mapOf(1 to "Dante", 2 to "Kratos", 3 to "Master Chief", 4 to "Nathan Drake"), 2, Tematica.VIDEOJOC),
        Pregunta(27, "¿De qué tipo principal es el conocido Pokémon Pikachu?", mapOf(1 to "Fuego", 2 to "Agua", 3 to "Eléctrico", 4 to "Planta"), 3, Tematica.VIDEOJOC),
        Pregunta(28, "¿Qué famosa saga de videojuegos de carreras cuenta con caparazones rojos y azules para atacar a los rivales?", mapOf(1 to "Gran Turismo", 2 to "Need for Speed", 3 to "Forza Horizon", 4 to "Mario Kart"), 4, Tematica.VIDEOJOC),
        Pregunta(29, "¿Qué icónico personaje de los videojuegos de los años 80 es un círculo amarillo que come puntos en un laberinto?", mapOf(1 to "Sonic", 2 to "Pac-Man", 3 to "Mega Man", 4 to "Donkey Kong"), 2, Tematica.VIDEOJOC),
        Pregunta(30, "¿En qué popular juego de PC de la empresa Riot Games los equipos compiten por destruir el 'Nexo' enemigo?", mapOf(1 to "Valorant", 2 to "Overwatch", 3 to "League of Legends", 4 to "Dota 2"), 3, Tematica.VIDEOJOC),
        Pregunta(31, "¿Cuál es la técnica de ataque más famosa de Goku en Dragon Ball?", mapOf(1 to "Rasengan", 2 to "Kamehameha", 3 to "Chidori", 4 to "Genkidama"), 2, Tematica.ANIME),
        Pregunta(32, "¿Cuántas Bolas de Dragón se necesitan reunir para invocar al dragón Shenron?", mapOf(1 to "Cinco", 2 to "Seis", 3 to "Siete", 4 to "Ocho"), 3, Tematica.ANIME),
        Pregunta(33, "¿Cuál es la posición principal en la que juega Mark Evans en el campo de fútbol?", mapOf(1 to "Delantero", 2 to "Defensa", 3 to "Medio", 4 to "Portero"), 4, Tematica.ANIME, imatgeResId = R.drawable.mark_evans),
        Pregunta(34, "¿Cómo se llama la famosa supertécnica de parada de Mark Evans que invoca una mano gigante amarilla?", mapOf(1 to "Mano Celestial", 2 to "Mano Diabólica", 3 to "Muralla Infinita", 4 to "Tornado de Fuego"), 1, Tematica.ANIME),
        Pregunta(35, "¿Cuál es el mayor sueño de Naruto Uzumaki desde el inicio de la serie?", mapOf(1 to "Destruir su aldea", 2 to "Ser el Hokage", 3 to "Atrapar todos los Pokémon", 4 to "Encontrar el One Piece"), 2, Tematica.ANIME),
        Pregunta(36, "¿Cómo se llama el gigantesco zorro de las nueve colas que está sellado dentro de Naruto?", mapOf(1 to "Shukaku", 2 to "Matatabi", 3 to "Kurama", 4 to "Gyuki"), 3, Tematica.ANIME, R.drawable.kyubi),
        Pregunta(37, "¿Cuál es el nombre del protagonista de One Piece que sueña con ser el Rey de los Piratas?", mapOf(1 to "Roronoa Zoro", 2 to "Sanji", 3 to "Naruto", 4 to "Monkey D. Luffy"), 4, Tematica.ANIME),
        Pregunta(38, "¿Qué tipo de alimento da poderes especiales en One Piece a cambio de perder la capacidad de nadar?", mapOf(1 to "Semillas Senzu", 2 to "Frutas del Diablo", 3 to "Manzanas Doradas", 4 to "Caramelos Raros"), 2, Tematica.ANIME),
        Pregunta(39, "¿Cuál fue el primer Pokémon que recibió Ash Ketchum al iniciar su aventura?", mapOf(1 to "Charmander", 2 to "Bulbasaur", 3 to "Squirtle", 4 to "Pikachu"), 4, Tematica.ANIME, R.drawable.bulbasur),
        Pregunta(40, "¿Cómo se llama el grupo de villanos formado por Jessie, James y Meowth que siempre intenta robar a Pikachu?", mapOf(1 to "Team Aqua", 2 to "Team Rocket", 3 to "Team Magma", 4 to "Team Plasma"), 2, Tematica.ANIME),
        Pregunta(41, "¿Cómo se llama la escuela de magia y hechicería a la que asiste Harry Potter?", mapOf(1 to "Beauxbatons", 2 to "Durmstrang", 3 to "Hogwarts", 4 to "Ilvermorny"), 3, Tematica.SERIE_PELI),
        Pregunta(42, "¿A qué casa de Hogwarts pertenecen Harry, Ron y Hermione?", mapOf(1 to "Slytherin", 2 to "Hufflepuff", 3 to "Ravenclaw", 4 to "Gryffindor"), 4, Tematica.SERIE_PELI),
        Pregunta(43, "En 'El Señor de los Anillos', ¿qué hobbit es el portador encargado de destruir el Anillo Único?", mapOf(1 to "Sam", 2 to "Pippin", 3 to "Frodo", 4 to "Bilbo"), 3, Tematica.SERIE_PELI, R.drawable.anillo),
        Pregunta(44, "¿Cómo se llama el sabio mago de barba blanca que guía a la Comunidad del Anillo?", mapOf(1 to "Saruman", 2 to "Elrond", 3 to "Gandalf", 4 to "Radagast"), 3, Tematica.SERIE_PELI, R.drawable.gandalf),
        Pregunta(45, "¿Cuál es el famoso lema de la Casa Stark en 'Juego de Tronos'?", mapOf(1 to "Fuego y Sangre", 2 to "Se acerca el invierno", 3 to "Oye mi rugido", 4 to "Nosotros no sembramos"), 2, Tematica.SERIE_PELI),
        Pregunta(46, "En 'Juego de Tronos', ¿a qué familia pertenece Daenerys, conocida como la 'Madre de Dragones'?", mapOf(1 to "Lannister", 2 to "Baratheon", 3 to "Targaryen", 4 to "Tyrell"), 3, Tematica.SERIE_PELI, R.drawable.dragon_daenerys),
        Pregunta(47, "En 'The Walking Dead', ¿quién es el ex policía protagonista que despierta en un mundo lleno de zombis?", mapOf(1 to "Daryl Dixon", 2 to "Negan", 3 to "Glenn Rhee", 4 to "Rick Grimes"), 4, Tematica.SERIE_PELI),
        Pregunta(48, "¿Cuál es el arma icónica que siempre utiliza el personaje de Daryl Dixon?", mapOf(1 to "Katana", 2 to "Bate de béisbol", 3 to "Ballesta", 4 to "Machete"), 3, Tematica.SERIE_PELI),
        Pregunta(49, "¿Cómo se llama la niña con poderes telequinéticos protagonista de 'Stranger Things'?", mapOf(1 to "Max", 2 to "Nancy", 3 to "Robin", 4 to "Once (Eleven)"), 4, Tematica.SERIE_PELI),
        Pregunta(50, "¿En qué mítica década está ambientada la serie 'Stranger Things'?", mapOf(1 to "Los años 70", 2 to "Los años 80", 3 to "Los años 90", 4 to "Los años 2000"), 2, Tematica.SERIE_PELI)
    )
}