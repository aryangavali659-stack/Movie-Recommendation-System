package com.example.myapplication.data.local

import androidx.room.Entity

@Entity(
    tableName = "movies",
    primaryKeys = ["id", "userId"]
)
data class MovieEntity(
    val id: Int,
    val userId: String,
    val title: String,
    val overview: String,
    val posterPath: String,
    val rating: Double,
    val releaseDate: String,
    val category: String = "watchlist",
    val genresCsv: String = ""
)
