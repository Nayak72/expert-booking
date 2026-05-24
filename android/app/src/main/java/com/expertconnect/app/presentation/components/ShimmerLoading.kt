package com.expertconnect.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.expertconnect.app.ui.theme.ShimmerBase
import com.expertconnect.app.ui.theme.ShimmerHighlight

/**
 * Shimmer loading animation effect.
 * Use ShimmerBox as a placeholder while content is loading.
 */

@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    return Brush.linearGradient(
        colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush())
    )
}

@Composable
fun ShimmerExpertCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            ShimmerBox(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp))
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(12.dp))
        Spacer(modifier = Modifier.height(6.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.8f).height(12.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Row {
            ShimmerBox(modifier = Modifier.width(80.dp).height(28.dp), shape = RoundedCornerShape(14.dp))
            Spacer(modifier = Modifier.width(8.dp))
            ShimmerBox(modifier = Modifier.width(80.dp).height(28.dp), shape = RoundedCornerShape(14.dp))
        }
    }
}

@Composable
fun ShimmerHomeSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(3) {
            ShimmerExpertCard()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
