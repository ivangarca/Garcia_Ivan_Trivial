package com.example.garcia_ivan_trivial.dao

import com.example.garcia_ivan_trivial.model.User
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUser(username: String): User?

    @Query("SELECT * FROM users ORDER BY victorias DESC LIMIT 5")
    suspend fun getTop5Users(): List<User>

    @Update
    suspend fun update(user: User)
}