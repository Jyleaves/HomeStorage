// DisplayModeToggle.kt
package com.example.homestorage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DisplayModeToggle(
    currentMode: String,
    onModeChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentMode == "所有物品",
            onClick = { onModeChange("所有物品") },
            label = { Text("所有物品") }
        )
        FilterChip(
            selected = currentMode == "容器",
            onClick = { onModeChange("容器") },
            label = { Text("容器") }
        )
    }
}
