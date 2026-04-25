package com.example.garcia_ivan_trivial.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class Tematica {
    VIDEOJOC,
    CULTURA_GENERAL,
    SERIE_PELI,
    ANIME,
    MUSICA
}

@Entity(tableName = "preguntas")
data class Pregunta(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val enunciat: String,
    val opcions: Map<Int, String>,
    val respostaCorrecta: Int,
    val tematica: Tematica,
    val imatgeResId: Int? = null
)

class TrivialConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromMap(value: Map<Int, String>): String = gson.toJson(value)

    @TypeConverter
    fun toMap(value: String): Map<Int, String> {
        val type = object : TypeToken<Map<Int, String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromTematica(value: Tematica): String = value.name

    @TypeConverter
    fun toTematica(value: String): Tematica = Tematica.valueOf(value)
}

