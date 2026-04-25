package com.example.garcia_ivan_trivial.dao

import com.example.garcia_ivan_trivial.model.TrivialConverters
import com.example.garcia_ivan_trivial.model.Pregunta
import com.example.garcia_ivan_trivial.model.User
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [User::class, Pregunta::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(TrivialConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun preguntaDao(): PreguntaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trivial_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}