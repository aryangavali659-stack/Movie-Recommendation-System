package com.example.myapplication.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(
    context: Context,
    private val authRepository: AuthRepository = AuthRepository(context)
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    fun getActiveUserId(): String {
        val email = authRepository.getCurrentEmail()
        return if (!email.isNullOrBlank()) email else "local_user"
    }

    private fun getGenreKey(userId: String): String = "pref_genre_filter_$userId"
    private fun getRatingKey(userId: String): String = "pref_rating_filter_$userId"

    private val _selectedGenres = MutableStateFlow(loadGenres())
    val selectedGenres: Flow<Set<String>> = _selectedGenres.asStateFlow()

    private val _minRating = MutableStateFlow(loadMinRating())
    val minRating: Flow<Float> = _minRating.asStateFlow()

    fun updateActiveUser() {
        _selectedGenres.value = loadGenres()
        _minRating.value = loadMinRating()
    }

    fun loadGenres(userId: String = getActiveUserId()): Set<String> {
        return prefs.getStringSet(getGenreKey(userId), emptySet()) ?: emptySet()
    }

    fun loadMinRating(userId: String = getActiveUserId()): Float {
        return prefs.getFloat(getRatingKey(userId), 0.0f)
    }

    fun getRatingFilter(userId: String = getActiveUserId()): Float = loadMinRating(userId)

    fun getGenresFilter(userId: String = getActiveUserId()): Set<String> = loadGenres(userId)

    suspend fun saveSelectedGenres(genres: Set<String>, userId: String = getActiveUserId()) {
        saveGenresFilter(userId, genres)
    }

    fun saveGenresFilter(userId: String, genres: Set<String>) {
        prefs.edit {
            putStringSet(getGenreKey(userId), genres)
        }
        _selectedGenres.value = genres
    }

    suspend fun saveMinRating(rating: Float, userId: String = getActiveUserId()) {
        saveRatingFilter(userId, rating)
    }

    fun saveRatingFilter(userId: String, rating: Float) {
        prefs.edit {
            putFloat(getRatingKey(userId), rating)
        }
        _minRating.value = rating
    }

    fun clearUserPreferences() {
        _selectedGenres.value = emptySet()
        _minRating.value = 0.0f
    }
}
