package com.example.myapplication.domain.model

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val preferredGenreIds: List<Int> = emptyList(),
    val preferredLanguages: List<String> = listOf("en", "hi", "te"), // English, Hindi, Telugu
    val isOnboardingCompleted: Boolean = false,
    val favoriteGenres: Set<String> = emptySet(),
    val minRatingFilter: Double = 0.0
)
