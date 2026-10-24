package com.example.myapplication.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.UserProfile
import com.example.myapplication.ui.MovieViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: MovieViewModel,
    userProfile: UserProfile = UserProfile(),
    onChangePreferencesClick: () -> Unit = {},
    onSwitchAccountClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onBackClick: () -> Unit
) {
    val profileState by viewModel.userProfile.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val effectiveProfile = if (userProfile.email.isNotBlank() || userProfile.favoriteGenres.isNotEmpty()) userProfile else profileState
    var showLogoutDialog by remember { mutableStateOf(false) }

    val displayEmail = if (currentUserId.isNotBlank()) currentUserId else "No User Logged In"

    val availableGenres = listOf(
        "Action", "Adventure", "Animation", "Comedy", "Crime",
        "Drama", "Fantasy", "Horror", "Romance", "Sci-Fi", "Thriller"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account & Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = displayEmail,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Connected Profile (${effectiveProfile.favoriteGenres.size} Genres Selected)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Text(
                text = "Favorite Genres",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                availableGenres.forEach { genre ->
                    val isSelected = effectiveProfile.favoriteGenres.contains(genre)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleFavoriteGenre(genre) },
                        label = { Text(genre) }
                    )
                }
            }

            HorizontalDivider()

            Text(
                text = "Minimum Rating Filter: ${"%.1f".format(effectiveProfile.minRatingFilter)} ★",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Slider(
                value = effectiveProfile.minRatingFilter.toFloat(),
                onValueChange = { viewModel.setMinRating(it.toDouble()) },
                valueRange = 0f..9f,
                steps = 8
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action 1: Edit Preferences
            OutlinedButton(
                onClick = onChangePreferencesClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Favorite Genres")
            }

            // Action 2: Switch Account
            Button(
                onClick = {
                    viewModel.resetStateOnLogout()
                    onSwitchAccountClick()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Switch / Add Another Account")
            }

            // Action 3: Sign Out
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
    }

    // Confirmation Alert Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out of your account?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.resetStateOnLogout()
                        onSignOutClick()
                    }
                ) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
