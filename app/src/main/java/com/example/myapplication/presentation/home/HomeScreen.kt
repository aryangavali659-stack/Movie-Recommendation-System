package com.example.myapplication.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Movie
import com.example.myapplication.presentation.components.MovieCard
import com.example.myapplication.ui.MovieViewModel

@Composable
fun HomeScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit,
    onOpenWatchlist: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    val hollywoodMovies by viewModel.hollywoodMovies.collectAsState()
    val bollywoodMovies by viewModel.bollywoodMovies.collectAsState()
    val tollywoodMovies by viewModel.tollywoodMovies.collectAsState()
    val recommendedMovies by viewModel.recommendedMovies.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    val isLoading = hollywoodMovies.isEmpty() && bollywoodMovies.isEmpty() && tollywoodMovies.isEmpty()

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Movie Recommender",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Row {
                            IconButton(onClick = onOpenProfile) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = onOpenWatchlist) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Watchlist",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search movies...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear Search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            if (searchQuery.isNotBlank()) {
                item {
                    Text(
                        text = "Search Results",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    } else if (searchResults.isEmpty()) {
                        Text("No movies found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(searchResults) { movie ->
                                Box(modifier = Modifier.clickable { onMovieClick(movie) }) {
                                    MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                                }
                            }
                        }
                    }
                }
            }

            if (recommendedMovies.isNotEmpty()) {
                item {
                    MovieSection(
                        title = "Recommended For You (Cosine Similarity)",
                        movies = recommendedMovies,
                        onMovieClick = onMovieClick
                    )
                }
            }

            item {
                MovieSection(
                    title = "Hollywood Hits (Post-2000)",
                    movies = hollywoodMovies,
                    onMovieClick = onMovieClick
                )
            }

            item {
                MovieSection(
                    title = "Bollywood Cinema (Hindi)",
                    movies = bollywoodMovies,
                    onMovieClick = onMovieClick
                )
            }

            item {
                MovieSection(
                    title = "Tollywood Cinema (Telugu)",
                    movies = tollywoodMovies,
                    onMovieClick = onMovieClick
                )
            }
        }
    }
}

@Composable
fun MovieSection(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        if (movies.isEmpty()) {
            Text(
                text = "Loading titles...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(movies) { movie ->
                    Box(modifier = Modifier.clickable { onMovieClick(movie) }) {
                        MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                    }
                }
            }
        }
    }
}
