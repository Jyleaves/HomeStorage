// FilterChipRow.kt
package com.example.homestorage.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FilterChipRow(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    allLabel: String = "全部",
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = (selectedOption == allLabel),
                onClick = { onOptionSelected(allLabel) },
                label = { Text(allLabel) }
            )
        }
        items(options) { option ->
            FilterChip(
                selected = (selectedOption == option),
                onClick = { onOptionSelected(option) },
                label = { Text(option) }
            )
        }
    }
}
