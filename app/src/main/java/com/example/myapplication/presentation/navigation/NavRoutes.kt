package com.example.myapplication.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object PreferenceSelection : Screen("preference_selection")
    object Home : Screen("home")
    object MovieDetail : Screen("movie_detail")
    object Watchlist : Screen("watchlist")
    object Profile : Screen("profile")
}
