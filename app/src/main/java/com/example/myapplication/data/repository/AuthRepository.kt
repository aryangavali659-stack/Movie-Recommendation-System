package com.example.myapplication.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Patterns
import androidx.core.content.edit
import com.example.myapplication.utils.ActivityLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private val _authStateFlow = MutableStateFlow<String?>(null)
        val authStateFlowStream: Flow<String?> = _authStateFlow.asStateFlow()
        private var currentLoggedInEmail: String? = null
    }

    val authStateFlow: Flow<String?> get() = authStateFlowStream

    fun isUserLoggedIn(): Boolean {
        return !getCurrentUserId().isBlank()
    }

    fun getCurrentUserId(): String {
        return currentLoggedInEmail ?: prefs?.getString("user_email", "") ?: ""
    }

    fun getCurrentUserEmail(): String {
        return getCurrentUserId()
    }

    fun getCurrentEmail(): String? {
        val email = getCurrentUserId()
        return if (email.isNotBlank()) email else null
    }

    suspend fun loginWithEmail(email: String, pass: String): Result<Unit> {
        return try {
            val trimmedEmail = email.trim()
            val trimmedPass = pass.trim()

            if (trimmedEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() || trimmedPass.length < 4) {
                return Result.failure(Exception("Please enter a valid email address and password (min 4 chars)."))
            }

            // Check if Email is Verified
            val isVerified = prefs?.getBoolean("verified_${trimmedEmail}", true) ?: true
            if (!isVerified) {
                logout()
                return Result.failure(Exception("Email not verified! We have sent a verification link to your Gmail inbox. Please check and verify first."))
            }

            currentLoggedInEmail = trimmedEmail
            prefs?.edit {
                putBoolean("is_logged_in", true)
                putString("user_email", trimmedEmail)
            }
            _authStateFlow.value = trimmedEmail
            ActivityLogger.logUserAction(userId = trimmedEmail, activityType = "LOGIN", email = trimmedEmail)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Authentication failed. Check credentials."))
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String): Result<Unit> {
        return try {
            val trimmedEmail = email.trim()
            val trimmedPass = pass.trim()

            if (trimmedEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() || trimmedPass.length < 4) {
                return Result.failure(Exception("Please enter a valid email address and password."))
            }

            prefs?.edit {
                putBoolean("verified_${trimmedEmail}", true)
                putBoolean("is_logged_in", true)
                putString("user_email", trimmedEmail)
            }
            currentLoggedInEmail = trimmedEmail
            _authStateFlow.value = trimmedEmail
            ActivityLogger.logUserAction(userId = trimmedEmail, activityType = "SIGNUP", email = trimmedEmail)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Sign up failed"))
        }
    }

    fun logout() {
        val email = getCurrentUserId()
        if (email.isNotBlank()) {
            ActivityLogger.logUserAction(userId = email, activityType = "LOGOUT", email = email)
        }
        currentLoggedInEmail = null
        prefs?.edit { clear() }
        _authStateFlow.value = null
    }
}
