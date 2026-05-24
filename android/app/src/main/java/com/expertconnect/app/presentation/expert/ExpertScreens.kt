@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.expertconnect.app.presentation.expert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.expertconnect.app.domain.model.Expert
import com.expertconnect.app.presentation.components.*
import com.expertconnect.app.ui.theme.*
import com.expertconnect.app.utils.UiState
import com.expertconnect.app.utils.toSkillsList

/**
 * Expert List Screen with search, category filters, and paginated expert cards.
 */
@Composable
fun ExpertListScreen(
    onExpertClick: (String) -> Unit,
    viewModel: ExpertViewModel = hiltViewModel()
) {
    val expertsState by viewModel.expertsState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("Tech", "AI/ML", "Business", "Health", "Finance", "Design", "Law")
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(PrimaryPurple.copy(alpha = 0.2f), BackgroundDark)))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Discover Experts",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text("Find the perfect expert for your needs", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { viewModel.search(searchQuery) }
                )
            }
        }

        // Category filter chips
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null; viewModel.filterByCategory(null) },
                    label = { Text("All", color = if (selectedCategory == null) Color.White else TextSecondary) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryPurple,
                        containerColor = SurfaceVariantDark
                    )
                )
            }
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        selectedCategory = if (selectedCategory == category) null else category
                        viewModel.filterByCategory(selectedCategory)
                    },
                    label = { Text(category, color = if (selectedCategory == category) Color.White else TextSecondary) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryPurple,
                        containerColor = SurfaceVariantDark
                    )
                )
            }
        }

        // Expert list
        when (val state = expertsState) {
            is UiState.Loading -> {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(5) { ShimmerExpertCard(modifier = Modifier.padding(horizontal = 16.dp)) }
                }
            }
            is UiState.Success -> {
                if (state.data.items.isEmpty()) {
                    EmptyState(emoji = "🔍", title = "No experts found", subtitle = "Try different search terms")
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                        // Result count
                        item {
                            Text(
                                text = "${state.data.total} experts found",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        items(state.data.items) { expert ->
                            ExpertCard(
                                expert = expert,
                                onClick = { onExpertClick(expert.id) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
            is UiState.Error -> {
                ErrorState(message = state.message, onRetry = { viewModel.loadExperts() })
            }
            else -> {}
        }
    }
}

/**
 * Expert Detail Screen — full profile with bio, skills, reviews, and book button.
 */
@Composable
fun ExpertDetailScreen(
    expertId: String,
    onBookClick: (String, String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: ExpertViewModel = hiltViewModel()
) {
    val detailState by viewModel.expertDetailState.collectAsState()
    val reviewsState by viewModel.reviewsState.collectAsState()
    val ratingSummary by viewModel.ratingSummary.collectAsState()
    val isFavorited by viewModel.isFavorited.collectAsState()

    LaunchedEffect(expertId) {
        viewModel.loadExpertDetail(expertId)
        viewModel.loadReviews(expertId)
        viewModel.checkFavoriteStatus(expertId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expert Profile", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(expertId) }) {
                        Icon(
                            imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorited) Color(0xFFE91E63) else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        bottomBar = {
            if (detailState is UiState.Success) {
                val expert = (detailState as UiState.Success<Expert>).data
                Box(modifier = Modifier.fillMaxWidth().background(SurfaceDark).padding(16.dp)) {
                    GradientButton(
                        text = "Book Session — ${"$%.0f".format(expert.pricing)}/hr",
                        onClick = { onBookClick(expertId, expert.userName ?: "Expert") },
                        enabled = expert.isAvailable == 1
                    )
                }
            }
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        when (val state = detailState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }
            is UiState.Success -> {
                val expert = state.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Profile header
                    item {
                        ExpertProfileHeader(expert = expert)
                    }

                    // Stats
                    item {
                        ExpertStatsRow(expert = expert, ratingSummary = ratingSummary)
                    }

                    // Bio
                    if (!expert.bio.isNullOrBlank()) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text("About", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(expert.bio, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                        }
                    }

                    // Skills
                    val skills = expert.skills.toSkillsList()
                    if (skills.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text("Skills", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    skills.forEach { skill -> SkillChip(skill = skill) }
                                }
                            }
                        }
                    }

                    // Reviews header
                    item {
                        SectionHeader(title = "Reviews (${expert.totalReviews})")
                    }

                    // Reviews list
                    when (val rvState = reviewsState) {
                        is UiState.Success -> {
                            if (rvState.data.isEmpty()) {
                                item { EmptyState(emoji = "📝", title = "No reviews yet") }
                            } else {
                                items(rvState.data) { review ->
                                    ReviewCard(review = review)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
            is UiState.Error -> {
                ErrorState(message = state.message, onRetry = { viewModel.loadExpertDetail(expertId) })
            }
            else -> {}
        }
    }
}

@Composable
private fun ExpertProfileHeader(expert: Expert) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(PrimaryPurple.copy(alpha = 0.25f), BackgroundDark)
                )
            )
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Brush.linearGradient(listOf(PrimaryPurple, SecondaryTeal)))) {
                if (!expert.profileImage.isNullOrBlank()) {
                    AsyncImage(model = expert.profileImage, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                } else {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(44.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expert.userName ?: "Expert", style = MaterialTheme.typography.headlineSmall, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
                Text(expert.expertise, style = MaterialTheme.typography.bodyLarge, color = PrimaryPurpleLight)
                Spacer(modifier = Modifier.height(6.dp))
                RatingBar(rating = expert.averageRating)
            }
        }
    }
}

@Composable
private fun ExpertStatsRow(expert: Expert, ratingSummary: com.expertconnect.app.domain.model.RatingSummary?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ExpertStatItem(label = "Experience", value = "${expert.experience} yrs", icon = "💼")
        ExpertStatItem(label = "Sessions", value = "${expert.totalBookings}", icon = "📅")
        ExpertStatItem(label = "Rating", value = "%.1f".format(expert.averageRating), icon = "⭐")
        ExpertStatItem(label = "Price", value = "$${"%.0f".format(expert.pricing)}/hr", icon = "💰")
    }
}

@Composable
private fun ExpertStatItem(label: String, value: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 20.sp)
        Text(value, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun ReviewCard(review: com.expertconnect.app.domain.model.Review) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(PrimaryPurple.copy(alpha = 0.2f))) {
                    Text(
                        text = review.reviewerName?.firstOrNull()?.uppercase() ?: "?",
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(review.reviewerName ?: "User", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    RatingBar(rating = review.rating)
                }
            }
            if (!review.reviewText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(review.reviewText, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}
