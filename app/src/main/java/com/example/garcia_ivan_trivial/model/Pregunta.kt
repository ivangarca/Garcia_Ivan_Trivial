package com.example.garcia_ivan_trivial.model

data class Pregunta(
    val enunciat: String,
    val opcions: Map<Int, String>,
    val respostaCorrecta: Int
)