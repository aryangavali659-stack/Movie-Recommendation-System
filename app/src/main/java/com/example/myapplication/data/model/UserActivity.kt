package com.example.myapplication.data.model

data class UserActivity(
    val userId: String = "",
    val userEmail: String = "",
    val activityType: String = "",     // e.g. "LOGIN", "ADDED_TO_WATCHLIST"
    val movieTitle: String = "",       // Movie title when applicable
    val timestamp: Long = System.currentTimeMillis()
)
