package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CinemaGold

@Composable
fun HalfStarRatingBar(
    rating: Float,
    onRatingChanged: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier,
    starSize: Int = 28,
    showLabel: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..5) {
                val icon = when {
                    rating >= i -> Icons.Filled.Star
                    rating >= i - 0.5f -> Icons.Filled.StarHalf
                    else -> Icons.Outlined.StarOutline
                }

                Icon(
                    imageVector = icon,
                    contentDescription = "Estrella $i",
                    tint = if (rating >= i - 0.5f) CinemaGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(starSize.dp)
                        .testTag("star_$i")
                        .then(
                            if (onRatingChanged != null) {
                                Modifier.clickable {
                                    // Cycling logic for simple tap: if current was full, set half, if half set 0, else full
                                    val newRating = if (rating == i.toFloat()) {
                                        i - 0.5f
                                    } else if (rating == (i - 0.5f)) {
                                        0f
                                    } else {
                                        i.toFloat()
                                    }
                                    onRatingChanged(newRating)
                                }
                            } else Modifier
                        )
                )
            }

            if (showLabel) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (rating > 0f) String.format("%.1f", rating) else "Sin calificar",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (rating > 0f) CinemaGold else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (onRatingChanged != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(1.0f, 2.5f, 3.5f, 4.0f, 4.5f, 5.0f).forEach { preset ->
                    FilterChip(
                        selected = rating == preset,
                        onClick = { onRatingChanged(preset) },
                        label = { Text("★ $preset", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CinemaGold.copy(alpha = 0.25f),
                            selectedLabelColor = CinemaGold
                        ),
                        modifier = Modifier.testTag("rating_chip_$preset")
                    )
                }
            }
        }
    }
}
