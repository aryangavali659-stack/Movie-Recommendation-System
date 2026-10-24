package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.MovieRepository
import com.example.myapplication.domain.model.Movie
import com.example.myapplication.utils.ApiEmailHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovieDetailViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _isWatchlisted = MutableStateFlow(false)
    val isWatchlisted: StateFlow<Boolean> = _isWatchlisted.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    fun checkWatchlistStatus(movieId: Int) {
        viewModelScope.launch {
            _isWatchlisted.value = repository.isBookmarked(movieId)
        }
    }

    fun toggleWatchlist(movie: Movie) {
        viewModelScope.launch {
            repository.toggleWatchlist(movie)
            val newStatus = repository.isBookmarked(movie.id)
            _isWatchlisted.value = newStatus

            if (newStatus) {
                _userMessage.emit("Saved to Watchlist")

                // AUTOMATED EMAIL TRIGGER
                val currentUserEmail = "aryangavaliofficial271@gmail.com"

                ApiEmailHelper.sendAutomatedEmail(
                    userEmail = currentUserEmail,
                    movieTitle = movie.title,
                    releaseInfo = movie.releaseDate,
                    posterUrl = movie.posterPath
                )
            } else {
                _userMessage.emit("Removed from Watchlist")
            }
        }
    }
}