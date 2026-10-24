package com.example.myapplication.presentation.navigation

import androidx.compose.runtime.*
import com.example.myapplication.data.local.UserPreferencesManager
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.domain.model.Movie
import com.example.myapplication.presentation.auth.LoginScreen
import com.example.myapplication.presentation.detail.DetailScreen
import com.example.myapplication.presentation.home.HomeScreen
import com.example.myapplication.presentation.profile.PreferenceSelectionScreen
import com.example.myapplication.presentation.profile.ProfileScreen
import com.example.myapplication.presentation.viewmodel.AuthViewModel
import com.example.myapplication.presentation.viewmodel.MovieDetailViewModel
import com.example.myapplication.presentation.viewmodel.ProfileViewModel
import com.example.myapplication.presentation.watchlist.WatchlistScreen
import com.example.myapplication.ui.MovieViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    authRepository: AuthRepository,
    profileViewModel: ProfileViewModel,
    movieViewModel: MovieViewModel,
    movieDetailViewModel: MovieDetailViewModel,
    userPreferencesManager: UserPreferencesManager? = null
) {
    val userProfile by profileViewModel.userProfile.collectAsState()
    val isUserLoggedIn = authRepository.isUserLoggedIn()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var selectedMovie by remember { mutableStateOf<Movie?>(null) }

    var isNavigationInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isNavigationInitialized) {
            isNavigationInitialized = true
            when {
                !isUserLoggedIn -> {
                    currentScreen = Screen.Login
                }
                userProfile.preferredGenreIds.isEmpty() -> {
                    currentScreen = Screen.PreferenceSelection
                }
            }
        }
    }

    when (currentScreen) {
        Screen.Login -> {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    movieViewModel.loadAllIndustries()
                    currentScreen = if (userProfile.preferredGenreIds.isEmpty()) {
                        Screen.PreferenceSelection
                    } else {
                        Screen.Home
                    }
                }
            )
        }
        Screen.PreferenceSelection -> {
            PreferenceSelectionScreen(
                currentSelectedIds = userProfile.preferredGenreIds,
                onSavePreferences = { updatedGenreIds ->
                    profileViewModel.updatePreferences(
                        selectedGenreIds = updatedGenreIds,
                        selectedLanguages = userProfile.preferredLanguages
                    )
                    movieViewModel.loadAllIndustries()
                    currentScreen = Screen.Home
                }
            )
        }
        Screen.Home -> {
            HomeScreen(
                viewModel = movieViewModel,
                onMovieClick = { movie ->
                    selectedMovie = movie
                    currentScreen = Screen.MovieDetail
                },
                onOpenWatchlist = {
                    currentScreen = Screen.Watchlist
                },
                onOpenProfile = {
                    currentScreen = Screen.Profile
                }
            )
        }
        Screen.MovieDetail -> {
            selectedMovie?.let { movie ->
                DetailScreen(
                    movie = movie,
                    viewModel = movieViewModel,
                    movieDetailViewModel = movieDetailViewModel,
                    onBackClick = {
                        currentScreen = Screen.Home
                    }
                )
            } ?: run {
                currentScreen = Screen.Home
            }
        }
        Screen.Watchlist -> {
            WatchlistScreen(
                viewModel = movieViewModel,
                onMovieClick = { movie ->
                    selectedMovie = movie
                    currentScreen = Screen.MovieDetail
                },
                onBackClick = {
                    currentScreen = Screen.Home
                }
            )
        }
        Screen.Profile -> {
            ProfileScreen(
                viewModel = movieViewModel,
                userProfile = userProfile,
                onChangePreferencesClick = {
                    currentScreen = Screen.PreferenceSelection
                },
                onSwitchAccountClick = {
                    movieViewModel.resetStateOnLogout()
                    authViewModel.switchOrSignOutAccount(userPreferencesManager)
                    currentScreen = Screen.Login
                },
                onSignOutClick = {
                    movieViewModel.resetStateOnLogout()
                    authViewModel.logout()
                    currentScreen = Screen.Login
                },
                onBackClick = {
                    currentScreen = Screen.Home
                }
            )
        }
    }
}