package com.example.myapplication.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    private val _isOnboardedFlow = MutableStateFlow(prefs.getBoolean("is_onboarded", false))
    val isOnboardedFlow: Flow<Boolean> = _isOnboardedFlow.asStateFlow()

    private val _preferredGenreIdsFlow = MutableStateFlow(
        prefs.getStringSet("preferred_genres", emptySet())
            ?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    )
    val preferredGenreIdsFlow: Flow<List<Int>> = _preferredGenreIdsFlow.asStateFlow()

    fun saveUserPreferences(genreIds: List<Int>, languages: List<String>) {
        val stringSet = genreIds.map { it.toString() }.toSet()
        val langSet = languages.toSet()
        prefs.edit {
            putStringSet("preferred_genres", stringSet)
            putStringSet("preferred_languages", langSet)
            putBoolean("is_onboarded", true)
        }

        _isOnboardedFlow.value = true
        _preferredGenreIdsFlow.value = genreIds
    }

    fun clearPreferences() {
        prefs.edit {
            clear()
        }
        _isOnboardedFlow.value = false
        _preferredGenreIdsFlow.value = emptyList()
    }
}
