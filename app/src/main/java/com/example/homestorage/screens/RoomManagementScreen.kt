package com.example.homestorage.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissDirection
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.homestorage.navigation.Screen
import com.example.homestorage.viewmodel.RoomViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun RoomManagementScreen(
    navController: NavController,
    roomViewModel: RoomViewModel = viewModel()
) {
    val rooms = roomViewModel.allRooms.collectAsState(initial = emptyList()).value

    // 用于显示确认删除弹窗
    var roomToDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("管理房间") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.AddRoom.createRoute())
            }) {
                Icon(Icons.Default.Add, contentDescription = "添加房间")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (rooms.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无房间")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rooms) { room ->
                        val dismissState = rememberDismissState(
                            confirmStateChange = { dismissValue ->
                                if (dismissValue == DismissValue.DismissedToStart) {
                                    roomToDelete = room.name
                                    false
                                } else false
                            }
                        )

                        SwipeToDismiss(
                            state = dismissState,
                            directions = setOf(DismissDirection.EndToStart),
                            background = {
                                val color = if (dismissState.targetValue == DismissValue.Default) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.errorContainer
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(color)
                                        .padding(end = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    val scale by animateFloatAsState(
                                        targetValue = if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除",
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.scale(scale)
                                    )
                                }
                            },
                            dismissContent = {
                                // 卡片中仅保留点击进入编辑，不再显示删除按钮
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // 传递房间名称参数进入编辑界面
                                            navController.navigate(Screen.AddRoom.createRoute(room.name))
                                        },
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(room.name, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // 确认删除弹窗
    if (roomToDelete != null) {
        AlertDialog(
            onDismissRequest = { roomToDelete = null },
            title = { Text("确认删除房间") },
            text = { Text("此操作将会删除房间及其下所有物品。确定要删除吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        roomViewModel.deleteRoom(roomToDelete!!)
                        roomToDelete = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { roomToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}
