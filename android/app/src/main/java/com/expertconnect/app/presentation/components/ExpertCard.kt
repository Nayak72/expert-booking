package com.expertconnect.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.expertconnect.app.domain.model.Expert
import com.expertconnect.app.ui.theme.*
import com.expertconnect.app.utils.toSkillsList

/**
 * Premium expert card composable with gradient background, profile image,
 * rating, skills chips, and favorite button.
 */
@Composable
fun ExpertCard(
    expert: Expert,
    onClick: () -> Unit,
    onFavoriteClick: (() -> Unit)? = null,
    isFavorited: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PrimaryPurple.copy(alpha = 0.08f),
                            CardDark
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // ── Header: Avatar + Name + Favorite ──────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile image
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryPurple, SecondaryTeal)
                                )
                            )
                    ) {
                        if (!expert.profileImage.isNullOrBlank()) {
                            AsyncImage(
                                model = expert.profileImage,
                                contentDescription = "${expert.userName} profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.align(Alignment.Center).size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = expert.userName ?: "Expert",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = expert.expertise,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryPurpleLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Favorite button
                    if (onFavoriteClick != null) {
                        IconButton(onClick = onFavoriteClick) {
                            Icon(
                                imageVector = if (isFavorited) Icons.Default.Favorite
                                else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isFavorited) "Remove favorite" else "Add favorite",
                                tint = if (isFavorited) Color(0xFFE91E63) else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Stats Row: Rating | Experience | Price ─────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatChip(
                        icon = Icons.Default.Star,
                        label = "%.1f".format(expert.averageRating),
                        tint = StarColor
                    )
                    StatChip(
                        icon = Icons.Default.WorkHistory,
                        label = "${expert.experience}yr",
                        tint = SecondaryTeal
                    )
                    StatChip(
                        icon = Icons.Default.AttachMoney,
                        label = "$${"%.0f".format(expert.pricing)}/hr",
                        tint = AccentAmber
                    )
                    StatChip(
                        icon = Icons.Default.People,
                        label = "${expert.totalBookings}",
                        tint = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ── Skills Chips ───────────────────────────────────────────────
                val skills = expert.skills.toSkillsList().take(3)
                if (skills.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        skills.forEach { skill ->
                            SkillChip(skill = skill)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ── Availability Badge ─────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (expert.isAvailable == 1) StatusConfirmed else StatusCancelled)
                    )
                    Text(
                        text = if (expert.isAvailable == 1) "Available" else "Unavailable",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (expert.isAvailable == 1) StatusConfirmed else StatusCancelled
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = tint, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SkillChip(skill: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PrimaryPurple.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = skill,
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryPurpleLight,
            fontSize = 11.sp
        )
    }
}
