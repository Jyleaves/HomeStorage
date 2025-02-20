package com.example.homestorage.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.homestorage.data.ItemCategory
import com.example.homestorage.viewmodel.ItemCategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCategoryManagementScreen(
    navController: NavController,
    itemCategoryViewModel: ItemCategoryViewModel = viewModel()
) {
    val categories = itemCategoryViewModel.allCategories.collectAsState(initial = emptyList()).value

    // 用于确认删除弹窗
    var categoryToDelete by remember { mutableStateOf<ItemCategory?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("管理物品类别") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            // +号，用于新增物品类别
            FloatingActionButton(onClick = {
                // 进入“添加物品类别”表单（编辑模式= false）
                navController.navigate("item_category_form?categoryNameToEdit=")
            }) {
                Icon(Icons.Default.Add, contentDescription = "添加物品类别")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无类别")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat.categoryName, style = MaterialTheme.typography.titleMedium)
                                Row {
                                    // 编辑按钮
                                    IconButton(onClick = {
                                        // 进入编辑模式
                                        navController.navigate("item_category_form?categoryNameToEdit=${cat.categoryName}")
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "编辑类别"
                                        )
                                    }
                                    // 删除按钮
                                    IconButton(onClick = {
                                        categoryToDelete = cat
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "删除类别",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 确认删除弹窗
    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("确认删除物品类别") },
            text = { Text("删除后，该类别可能无法恢复，确定要删除吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemCategoryViewModel.delete(categoryToDelete!!)
                        categoryToDelete = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}
