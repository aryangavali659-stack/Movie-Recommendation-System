package com.example.myapplication.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.MovieRepository
import com.example.myapplication.domain.model.Movie
import com.example.myapplication.domain.usecase.CalculateRecommendationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: MovieRepository,
    private val calculateUseCase: CalculateRecommendationUseCase
) : ViewModel() {

    private val _trendingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val trendingMovies: StateFlow<List<Movie>> = _trendingMovies

    private val _recommendedMovies = MutableStateFlow<List<Movie>>(emptyList())
    val recommendedMovies: StateFlow<List<Movie>> = _recommendedMovies

    init {
        fetchMovies()
    }

    private fun fetchMovies() {
        viewModelScope.launch {
            val movies = repository.getPopularMovies()
            _trendingMovies.value = movies

            val target = movies.firstOrNull()
            if (target != null) {
                _recommendedMovies.value = calculateUseCase.execute(listOf(target), movies)
            } else {
                _recommendedMovies.value = emptyList()
            }
        }
    }
}