package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.UserPreferencesManager
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val preferencesManager: UserPreferencesManager,
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                // Load local preference state
                preferencesManager.preferredGenreIdsFlow.collect { genres ->
                    _userProfile.value = _userProfile.value.copy(
                        preferredGenreIds = genres ?: emptyList()
                    )
                }

                val cloudProfile = userRepository.fetchUserProfileFromCloud()
                if (cloudProfile != null) {
                    _userProfile.value = cloudProfile
                    preferencesManager.saveUserPreferences(
                        cloudProfile.preferredGenreIds ?: emptyList(),
                        cloudProfile.preferredLanguages ?: emptyList()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updatePreferences(selectedGenreIds: List<Int>?, selectedLanguages: List<String>?) {
        viewModelScope.launch {
            try {
                val safeGenreIds = selectedGenreIds ?: emptyList()
                val safeLanguages = selectedLanguages ?: listOf("en", "hi", "te")

                val updatedProfile = _userProfile.value.copy(
                    preferredGenreIds = safeGenreIds,
                    preferredLanguages = safeLanguages,
                    isOnboardingCompleted = true
                )
                _userProfile.value = updatedProfile

                // Save locally
                preferencesManager.saveUserPreferences(safeGenreIds, safeLanguages)

                // Save to cloud repository
                userRepository.syncUserProfileToCloud(updatedProfile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
