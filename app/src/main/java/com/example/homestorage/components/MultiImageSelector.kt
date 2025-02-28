// MultiImageSelector.kt
package com.example.homestorage.components

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

// MultiImageSelector.kt
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MultiImageSelector(
    photoUris: List<Uri>,
    isEditing: Boolean,
    onAddImage: () -> Unit,
    onRemoveImage: (Int) -> Unit,
    onPreviewImage: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxCount: Int = 3
) {
    val remaining = maxCount - photoUris.size
    val itemSize = 120.dp
    val spacing = 8.dp

    BoxWithConstraints(modifier) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(photoUris) { index, uri ->
                ImageItem(
                    uri = uri,
                    index = index,
                    isEditing = isEditing,
                    onRemove = { onRemoveImage(index) },
                    onPreview = { onPreviewImage(index) },
                    size = itemSize
                )
            }

            if (isEditing && remaining > 0) {
                item {
                    AddImageButton(
                        remaining = remaining,
                        onAddImage = onAddImage,
                        size = itemSize
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageItem(
    uri: Uri,
    index: Int,
    isEditing: Boolean,
    onRemove: () -> Unit,
    onPreview: () -> Unit,
    size: Dp
) {
    Card(
        modifier = Modifier
            .size(size)
            .clickable { onPreview() },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            AsyncImage(
                model = uri,
                contentDescription = "Item image ${index + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (isEditing) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun AddImageButton(
    remaining: Int,
    onAddImage: () -> Unit,
    size: Dp
) {
    Surface(
        modifier = Modifier
            .size(size)
            .clickable(onClick = onAddImage),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add image",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$remaining left",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        }
    }
}
