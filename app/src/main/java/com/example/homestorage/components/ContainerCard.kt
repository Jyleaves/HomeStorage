package com.example.homestorage.components

import androidx.compose.foundation.layout.*
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.homestorage.data.ContainerEntity

@Composable
fun ContainerCard(
    container: ContainerEntity,
    onClick: () -> Unit
) {
    // Card 可点击，点击后进入 ContainerScreen
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 这里你也可以换成你想要的图标/图片
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = container.name,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
