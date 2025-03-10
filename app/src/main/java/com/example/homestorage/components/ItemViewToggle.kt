// ItemViewToggle.kt
package com.example.homestorage.components

import android.content.Context
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// DataStore 扩展属性（可放在一个公共工具类中，这里为了示例直接写在文件中）
private val Context.dataStore by preferencesDataStore(name = "settings")

// 定义视图模式枚举
enum class ItemViewMode {
    LIST, GRID
}

@Composable
fun ItemViewToggle(
    modifier: Modifier = Modifier,
    onViewModeChange: (ItemViewMode) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var viewMode by remember { mutableStateOf(ItemViewMode.LIST) }

    // 首次启动时从 DataStore 读取保存的模式，默认 0 表示列表模式
    LaunchedEffect(Unit) {
        val preferences = context.dataStore.data.first()
        val savedMode = preferences[intPreferencesKey("item_view_mode")] ?: 0
        viewMode = if (savedMode == 1) ItemViewMode.GRID else ItemViewMode.LIST
        onViewModeChange(viewMode)
    }

    IconButton(
        onClick = {
            // 切换模式
            viewMode = if (viewMode == ItemViewMode.LIST) ItemViewMode.GRID else ItemViewMode.LIST
            // 保存切换结果到 DataStore
            scope.launch {
                context.dataStore.edit { settings ->
                    settings[intPreferencesKey("item_view_mode")] = if (viewMode == ItemViewMode.GRID) 1 else 0
                }
            }
            onViewModeChange(viewMode)
        },
        modifier = modifier
    ) {
        // 当处于列表模式时显示“网格”图标，反之显示“列表”图标
        if (viewMode == ItemViewMode.LIST) {
            Icon(
                imageVector = Icons.Filled.ViewModule,
                contentDescription = "切换为网格视图"
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ViewList,
                contentDescription = "切换为列表视图"
            )
        }
    }
}
