package com.example.garcia_ivan_trivial.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class User (
    @PrimaryKey
    var username: String,
    var password: String,
    var victorias: Int = 0
)
