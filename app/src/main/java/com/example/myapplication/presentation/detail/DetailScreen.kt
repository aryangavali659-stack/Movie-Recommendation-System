package com.example.myapplication.presentation.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.domain.model.Movie
import com.example.myapplication.presentation.viewmodel.MovieDetailViewModel
import com.example.myapplication.ui.MovieViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    movie: Movie,
    viewModel: MovieViewModel,
    movieDetailViewModel: MovieDetailViewModel? = null,
    onBackClick: () -> Unit,
    onBookmarkClick: (Movie) -> Unit = {}
) {
    val context = LocalContext.current
    val details by viewModel.selectedMovieDetails.collectAsState()
    val isWatchlisted by movieDetailViewModel?.isWatchlisted?.collectAsState() ?: remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(movie.id) {
        viewModel.loadMovieDetails(movie.id)
        movieDetailViewModel?.checkWatchlistStatus(movie.id)
    }

    LaunchedEffect(Unit) {
        movieDetailViewModel?.userMessage?.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(movie.title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        movieDetailViewModel?.toggleWatchlist(movie) ?: onBookmarkClick(movie)
                    }) {
                        Icon(
                            imageVector = if (isWatchlisted) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save to Watchlist",
                            tint = if (isWatchlisted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                movieDetailViewModel?.toggleWatchlist(movie) ?: onBookmarkClick(movie)
            }) {
                Icon(
                    imageVector = if (isWatchlisted) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save to Watchlist"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(movie.posterPath),
                contentDescription = movie.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Text(text = movie.title, style = MaterialTheme.typography.headlineSmall)
            Text(text = "Rating: ${movie.rating} ★ | Release: ${movie.releaseDate}", style = MaterialTheme.typography.bodyMedium)

            // Watch Trailer Button
            details.trailerKey?.let { key ->
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$key"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Watch Official Trailer")
                }
            }

            // Watchlist Toggle Button
            Button(
                onClick = { movieDetailViewModel?.toggleWatchlist(movie) ?: onBookmarkClick(movie) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isWatchlisted) "Remove from Watchlist" else "Save to Watchlist")
            }

            Text(text = "Overview", style = MaterialTheme.typography.titleMedium)
            Text(text = movie.overview, style = MaterialTheme.typography.bodyMedium)

            // Cast Section
            if (details.cast.isNotEmpty()) {
                Text(text = "Cast", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(details.cast) { member ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(80.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = member.profileUrl.ifEmpty { "https://via.placeholder.com/150" }
                                ),
                                contentDescription = member.name,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = member.name, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            Text(text = member.character, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
