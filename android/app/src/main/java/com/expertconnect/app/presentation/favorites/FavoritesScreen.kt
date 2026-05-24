package com.expertconnect.app.presentation.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expertconnect.app.domain.model.Favorite
import com.expertconnect.app.presentation.components.*
import com.expertconnect.app.ui.theme.*
import com.expertconnect.app.utils.UiState

/**
 * Favorites Screen — shows all bookmarked experts with remove option.
 */
@Composable
fun FavoritesScreen(
    onExpertClick: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.cachedFavorites.collectAsState(initial = emptyList())
    val uiState by viewModel.favoritesState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundDark)
    ) {
        // Header
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(PrimaryPurple.copy(alpha = 0.2f), BackgroundDark)))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "My Favorites ❤️",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text("Experts you've bookmarked", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        if (uiState is UiState.Loading && favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else if (uiState is UiState.Error && favorites.isEmpty()) {
            ErrorState(message = (uiState as UiState.Error).message, onRetry = { viewModel.loadFavorites() })
        } else {
            if (favorites.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = "❤️",
                        title = "No favorites yet",
                        subtitle = "Tap the heart icon on any expert to bookmark them"
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(favorites) { favorite ->
                        FavoriteCard(
                            favorite = favorite,
                            onClick = { onExpertClick(favorite.expertId) },
                            onRemove = { viewModel.removeFavorite(favorite.expertId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteCard(
    favorite: Favorite,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PrimaryPurple, SecondaryTeal)))
            ) {
                Text(
                    text = favorite.expertName?.firstOrNull()?.uppercase() ?: "E",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = favorite.expertName ?: "Expert",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = favorite.expertExpertise ?: "Expert",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryPurpleLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    favorite.expertAverageRating?.let { rating ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = StarColor, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("%.1f".format(rating), style = MaterialTheme.typography.labelSmall, color = StarColor)
                        }
                    }
                    favorite.expertPricing?.let { price ->
                        Text("$${"%.0f".format(price)}/hr", style = MaterialTheme.typography.labelSmall, color = AccentAmber)
                    }
                }
            }

            // Remove button
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Remove favorite",
                    tint = Color(0xFFE91E63)
                )
            }
        }
    }
}
