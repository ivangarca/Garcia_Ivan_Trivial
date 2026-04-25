package com.example.garcia_ivan_trivial.view.trivial

import androidx.compose.foundation.background
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
import com.example.garcia_ivan_trivial.viewModel.TrivialUiState
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.CircleShape
import com.example.garcia_ivan_trivial.model.Tematica
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale

// --- IMPORTACIONES PARA ANIMACIÓN Y CICLO ---
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

// --- IMPORTACIONES PARA EL SENSOR ---
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

@Composable
fun ViewTrivial(
    loginState: LoginUiState,
    trivialState: TrivialUiState,
    onRespostaClick: (Int) -> Unit,
    onComodinClick: () -> Unit,
    onPauseApp: () -> Unit,
    onResumeApp: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // --- SENSOR DE AGITAR ---
    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            var ultimoAgito: Long = 0

            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val gX = x / SensorManager.GRAVITY_EARTH
                    val gY = y / SensorManager.GRAVITY_EARTH
                    val gZ = z / SensorManager.GRAVITY_EARTH

                    val fuerzaG = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

                    if (fuerzaG > 2.7f) {
                        val tiempoActual = System.currentTimeMillis()
                        if (tiempoActual - ultimoAgito > 1000) {
                            ultimoAgito = tiempoActual
                            onComodinClick()
                        }
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // --- CICLO DE VIDA ---
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                onPauseApp()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- SECCIÓN JUGADOR 1 ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (trivialState.esTornP1) Color.Green else Color.LightGray)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Jugador 1: ${loginState.player1} (${trivialState.tematiquesP1.size}/5)",
                fontSize = 20.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))

            Tematica.values().forEach { tematica ->
                val conseguida = trivialState.tematiquesP1.contains(tematica)
                val icono = if (conseguida) "✅" else "❌"
                Text(
                    text = "${tematica.name}: $icono",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }

        // --- ZONA CENTRAL ---
        if (trivialState.jocPausat) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "JOC EN PAUSA",
                        color = Color.White,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { onResumeApp() }
                    ) {
                        Text(
                            text = "REPRENDRE PARTIDA",
                            fontSize = 18.sp
                        )
                    }
                }
            }
        } else if (!trivialState.jocFinalitzat) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                val comodinDisponible = if (trivialState.esTornP1) !trivialState.comodinP1Usado else !trivialState.comodinP2Usado

                if (comodinDisponible && !trivialState.mostrantResultat) {
                    Button(
                        onClick = { onComodinClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text(
                            text = "COMODÍN 50/50 💡",
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = trivialState.preguntaActual?.enunciat ?: "",
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                if (trivialState.preguntaActual?.imatgeResId != null) {
                    Image(
                        painter = painterResource(id = trivialState.preguntaActual!!.imatgeResId!!),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .weight(1f, fill = false)
                            .padding(vertical = 8.dp)
                    )
                }

                trivialState.preguntaActual?.opcions?.forEach { (id, texto) ->
                    if (!trivialState.opcionesDeshabilitadas.contains(id)) {
                        val seleccionada = trivialState.respostaSeleccionada
                        val correcta = trivialState.preguntaActual.respostaCorrecta

                        val targetColor = if (trivialState.mostrantResultat) {
                            when (id) {
                                seleccionada -> if (seleccionada == correcta) Color.Green else Color.Red
                                correcta -> Color(0xFF81C784)
                                else -> Color.LightGray
                            }
                        } else {
                            Color(0xFF6200EE)
                        }

                        // ANIMACIÓN DE ESTADO
                        val animatedColor by animateColorAsState(
                            targetValue = targetColor,
                            label = "ColorAnim"
                        )

                        Button(
                            onClick = { onRespostaClick(id) },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .padding(2.dp)
                                .shadow(
                                    elevation = if (trivialState.mostrantResultat) 25.dp else 0.dp,
                                    shape = CircleShape,
                                    ambientColor = animatedColor,
                                    spotColor = animatedColor
                                ),
                            colors = ButtonDefaults.buttonColors(containerColor = animatedColor)
                        ) {
                            Text(
                                text = texto,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            // --- PARTIDA FINALIZADA CON CRONÓMETRO ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PARTIDA FINALITZADA",
                        fontSize = 30.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Temps total: ${trivialState.tempsPartida} segons",
                        fontSize = 20.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }

        // --- SECCIÓN JUGADOR 2 ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (!trivialState.esTornP1) Color(0xFFFFA500) else Color.LightGray)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Jugador 2: ${loginState.player2} (${trivialState.tematiquesP2.size}/5)",
                fontSize = 20.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))

            Tematica.values().forEach { tematica ->
                val conseguida = trivialState.tematiquesP2.contains(tematica)
                val icono = if (conseguida) "✅" else "❌"
                Text(
                    text = "${tematica.name}: $icono",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}