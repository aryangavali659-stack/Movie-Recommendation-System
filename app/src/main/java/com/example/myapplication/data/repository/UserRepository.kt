package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.UserProfile

class UserRepository {

    fun getCurrentUserId(): String? = "local_user"

    suspend fun syncUserProfileToCloud(profile: UserProfile) {
        // Cloud sync repository layer
    }

    suspend fun fetchUserProfileFromCloud(): UserProfile? {
        return null
    }
}
