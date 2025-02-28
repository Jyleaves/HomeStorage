// PageIndicator.kt
package com.example.homestorage.components

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun PageIndicator(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    val dotSize = 8.dp
    val spacing = 4.dp

    Row(modifier) {
        repeat(pageCount) { index ->
            val transition = updateTransition(
                targetState = (index == currentPage),
                label = "pageIndicatorAnimation"
            )

            val size by transition.animateDp(
                transitionSpec = { spring(stiffness = 150f) },
                label = "sizeAnimation"
            ) { isSelected ->
                if (isSelected) 12.dp else dotSize
            }

            Box(
                modifier = Modifier
                    .size(size)
                    .padding(spacing)
                    .clip(CircleShape)
                    .background(
                        if (index == currentPage)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
            )
        }
    }
}