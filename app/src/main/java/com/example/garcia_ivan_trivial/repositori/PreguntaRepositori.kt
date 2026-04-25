package com.example.garcia_ivan_trivial.repositori


import com.example.garcia_ivan_trivial.dao.PreguntaDao
import com.example.garcia_ivan_trivial.model.Pregunta

object PreguntaRepository {

    // 1. Obté totes les preguntes de la base de dades
    suspend fun getAllPreguntas(dao: PreguntaDao): List<Pregunta> {
        return dao.getAllPreguntas()
    }

    // 2. Compta quantes preguntes hi ha guardades
    suspend fun getCount(dao: PreguntaDao): Int {
        return dao.getCount()
    }

    // 3. Insereix la llista gegant de preguntes de cop
    suspend fun insertAll(preguntas: List<Pregunta>, dao: PreguntaDao) {
        dao.insertAll(preguntas)
    }
}