package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.MovieRepository
import com.example.myapplication.data.repository.UserPreferencesRepository
import com.example.myapplication.domain.model.Movie
import com.example.myapplication.domain.model.MovieDetailInfo
import com.example.myapplication.domain.model.UserProfile
import com.example.myapplication.domain.usecase.CalculateRecommendationUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MovieViewModel(
    private val repository: MovieRepository,
    private val preferencesRepository: UserPreferencesRepository? = null,
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val calculateRecommendationUseCase = CalculateRecommendationUseCase()

    val currentUserId: StateFlow<String> = authRepository.authStateFlow
        .map { email -> email ?: authRepository.getCurrentUserId() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authRepository.getCurrentUserId()
        )

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _hollywoodMovies = MutableStateFlow<List<Movie>>(emptyList())
    val hollywoodMovies: StateFlow<List<Movie>> = _hollywoodMovies

    private val _bollywoodMovies = MutableStateFlow<List<Movie>>(emptyList())
    val bollywoodMovies: StateFlow<List<Movie>> = _bollywoodMovies

    private val _tollywoodMovies = MutableStateFlow<List<Movie>>(emptyList())
    val tollywoodMovies: StateFlow<List<Movie>> = _tollywoodMovies

    private val _selectedMovieDetails = MutableStateFlow(MovieDetailInfo())
    val selectedMovieDetails: StateFlow<MovieDetailInfo> = _selectedMovieDetails.asStateFlow()

    // Watchlist re-collects automatically whenever currentUserId updates
    val watchlist: StateFlow<List<Movie>> = currentUserId
        .flatMapLatest { userId ->
            if (userId.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.getWatchlist(userId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recommendedMovies: StateFlow<List<Movie>> = combine(
        watchlist,
        hollywoodMovies,
        bollywoodMovies,
        tollywoodMovies,
        userProfile
    ) { watchlistMovies, hollywood, bollywood, tollywood, profile ->
        val allCandidates = (hollywood + bollywood + tollywood).distinctBy { it.id }
        calculateRecommendationUseCase.execute(watchlistMovies, allCandidates, profile)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Movie>>(emptyList())
    val searchResults: StateFlow<List<Movie>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        loadAllIndustries()

        // React immediately whenever user changes
        viewModelScope.launch {
            currentUserId.collect { userId ->
                preferencesRepository?.updateActiveUser()
                if (userId.isNotBlank()) {
                    val genres = preferencesRepository?.loadGenres(userId) ?: emptySet()
                    val rating = preferencesRepository?.loadMinRating(userId) ?: 0.0f
                    _userProfile.value = UserProfile(
                        email = userId,
                        favoriteGenres = genres,
                        minRatingFilter = rating.toDouble()
                    )
                } else {
                    _userProfile.value = UserProfile()
                }
            }
        }

        // Listen for live user preference updates
        preferencesRepository?.let { prefsRepo ->
            viewModelScope.launch {
                prefsRepo.selectedGenres.collect { savedGenres ->
                    _userProfile.value = _userProfile.value.copy(favoriteGenres = savedGenres)
                }
            }
            viewModelScope.launch {
                prefsRepo.minRating.collect { savedRating ->
                    _userProfile.value = _userProfile.value.copy(minRatingFilter = savedRating.toDouble())
                }
            }
        }
    }

    fun resetStateOnLogout() {
        authRepository.logout()
        _userProfile.value = UserProfile()
        _selectedMovieDetails.value = MovieDetailInfo()
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _isSearching.value = false
        preferencesRepository?.updateActiveUser()
    }

    fun loadAllIndustries() {
        viewModelScope.launch {
            _hollywoodMovies.value = repository.getMoviesByIndustry(language = "en")
            _bollywoodMovies.value = repository.getMoviesByIndustry(language = "hi")
            _tollywoodMovies.value = repository.getMoviesByIndustry(language = "te")
        }
    }

    fun loadMovieDetails(movieId: Int) {
        viewModelScope.launch {
            _selectedMovieDetails.value = MovieDetailInfo()
            _selectedMovieDetails.value = repository.getMovieDetails(movieId)
        }
    }

    fun onGenreSelected(genres: Set<String>) {
        _userProfile.value = _userProfile.value.copy(favoriteGenres = genres)
        preferencesRepository?.let { prefsRepo ->
            viewModelScope.launch {
                prefsRepo.saveSelectedGenres(genres)
            }
        }
    }

    fun onRatingFilterChanged(rating: Float) {
        _userProfile.value = _userProfile.value.copy(minRatingFilter = rating.toDouble())
        preferencesRepository?.let { prefsRepo ->
            viewModelScope.launch {
                prefsRepo.saveMinRating(rating)
            }
        }
    }

    fun toggleFavoriteGenre(genre: String) {
        val currentGenres = _userProfile.value.favoriteGenres.toMutableSet()
        if (currentGenres.contains(genre)) {
            currentGenres.remove(genre)
        } else {
            currentGenres.add(genre)
        }
        onGenreSelected(currentGenres)
    }

    fun setMinRating(rating: Double) {
        onRatingFilterChanged(rating.toFloat())
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        if (newQuery.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
        } else {
            _isSearching.value = true
            viewModelScope.launch {
                _searchResults.value = repository.searchMovies(newQuery)
                _isSearching.value = false
            }
        }
    }
}
