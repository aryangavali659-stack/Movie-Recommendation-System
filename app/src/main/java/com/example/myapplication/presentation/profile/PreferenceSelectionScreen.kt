package com.example.myapplication.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class GenreItem(val id: Int, val name: String)

// Standard TMDb Genre Mapping
val AVAILABLE_GENRES = listOf(
    GenreItem(28, "Action"),
    GenreItem(12, "Adventure"),
    GenreItem(16, "Animation"),
    GenreItem(35, "Comedy"),
    GenreItem(80, "Crime"),
    GenreItem(99, "Documentary"),
    GenreItem(18, "Drama"),
    GenreItem(10751, "Family"),
    GenreItem(14, "Fantasy"),
    GenreItem(27, "Horror"),
    GenreItem(9648, "Mystery"),
    GenreItem(10749, "Romance"),
    GenreItem(878, "Sci-Fi"),
    GenreItem(53, "Thriller")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceSelectionScreen(
    currentSelectedIds: List<Int>?,
    onSavePreferences: (List<Int>) -> Unit
) {
    // Safely fallback to empty list if state is null during initial load
    val safeInitialIds = remember(currentSelectedIds) { currentSelectedIds ?: emptyList() }
    val selectedGenreIds = remember { mutableStateListOf<Int>().apply { addAll(safeInitialIds) } }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Select Your Preferences") })
        },
        bottomBar = {
            Button(
                onClick = { onSavePreferences(selectedGenreIds.toList()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                enabled = selectedGenreIds.isNotEmpty()
            ) {
                Text("Save Preferences")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Choose genres you like to personalize your feed:",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AVAILABLE_GENRES) { genre ->
                    val isSelected = selectedGenreIds.contains(genre.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                selectedGenreIds.remove(genre.id)
                            } else {
                                selectedGenreIds.add(genre.id)
                            }
                        },
                        label = { Text(genre.name) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
