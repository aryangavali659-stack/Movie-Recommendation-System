package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.UserPreferencesManager
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.UserPreferencesRepository
import com.example.myapplication.ui.MovieViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

    private val _loginSuccess = MutableSharedFlow<Boolean>()
    val loginSuccess: SharedFlow<Boolean> = _loginSuccess.asSharedFlow()

    private val _logoutSuccess = MutableSharedFlow<Boolean>()
    val logoutSuccess: SharedFlow<Boolean> = _logoutSuccess.asSharedFlow()

    fun onEmailChange(newEmail: String) { _email.value = newEmail }
    fun onPasswordChange(newPass: String) { _password.value = newPass }

    fun loginOrSignUp() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            val msg = "Please enter valid credentials"
            _errorMessage.value = msg
            viewModelScope.launch { _uiEvent.emit(msg) }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.loginWithEmail(_email.value, _password.value)

            if (result.isSuccess) {
                _isLoading.value = false
                _errorMessage.value = null
                _loginSuccess.emit(true)
            } else {
                val signUpResult = authRepository.signUpWithEmail(_email.value, _password.value)
                _isLoading.value = false
                if (signUpResult.isSuccess) {
                    _errorMessage.value = null
                    _loginSuccess.emit(true)
                } else {
                    val err = signUpResult.exceptionOrNull()?.localizedMessage ?: "Auth Failed"
                    _errorMessage.value = err
                    _uiEvent.emit(err)
                }
            }
        }
    }

    fun switchOrSignOutAccount(
        preferencesManager: UserPreferencesManager? = null,
        preferencesRepository: UserPreferencesRepository? = null,
        movieViewModel: MovieViewModel? = null,
        onSignedOut: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            authRepository.logout()
            preferencesManager?.clearPreferences()
            preferencesRepository?.clearUserPreferences()
            movieViewModel?.resetStateOnLogout()
            _email.value = ""
            _password.value = ""
            _errorMessage.value = null
            _logoutSuccess.emit(true)
            onSignedOut?.invoke()
        }
    }

    fun logout(
        preferencesManager: UserPreferencesManager? = null,
        preferencesRepository: UserPreferencesRepository? = null,
        movieViewModel: MovieViewModel? = null,
        onSignedOut: (() -> Unit)? = null
    ) {
        switchOrSignOutAccount(preferencesManager, preferencesRepository, movieViewModel, onSignedOut)
    }
}
