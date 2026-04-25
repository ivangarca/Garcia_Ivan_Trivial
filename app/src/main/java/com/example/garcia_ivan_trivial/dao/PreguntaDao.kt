package com.example.garcia_ivan_trivial.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.garcia_ivan_trivial.model.Pregunta

@Dao
interface PreguntaDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(preguntas: List<Pregunta>)

    @Query("SELECT * FROM preguntas")
    suspend fun getAllPreguntas(): List<Pregunta>

    @Query("SELECT COUNT(*) FROM preguntas")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pregunta: Pregunta): Long
}