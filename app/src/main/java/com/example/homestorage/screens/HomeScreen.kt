package com.example.homestorage.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.homestorage.components.ContainerCard
import com.example.homestorage.navigation.Screen
import com.example.homestorage.viewmodel.ItemViewModel
import com.example.homestorage.viewmodel.RoomViewModel
import com.example.homestorage.viewmodel.ContainerViewModel
import com.example.homestorage.data.ContainerEntity
import com.example.homestorage.components.ItemRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    itemViewModel: ItemViewModel = viewModel(),
    roomViewModel: RoomViewModel = viewModel(),
    containerViewModel: ContainerViewModel = viewModel()
) {
    val items = itemViewModel.allItems.collectAsState().value
    val rooms = roomViewModel.allRooms.collectAsState(initial = emptyList()).value

    // 使用 rememberSaveable 保留状态
    var selectedRoom by rememberSaveable { mutableStateOf("全部") }
    var displayMode by rememberSaveable { mutableStateOf("所有物品") }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // 用于管理焦点状态
    val focusManager = LocalFocusManager.current

    // 收集容器数据
    var containerList by remember { mutableStateOf(emptyList<ContainerEntity>()) }
    LaunchedEffect(key1 = selectedRoom) {
        if (selectedRoom != "全部") {
            containerViewModel.getContainersByRoom(selectedRoom).collect { containerList = it }
        } else {
            containerList = emptyList()
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("家庭物品归类整理", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.ExportImport.route) }) {
                        Icon(Icons.Default.ImportExport, contentDescription = "导入/导出")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        }
    ) { innerPadding ->
        // 整个内容区域添加一个点击区域，点击时取消焦点
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ============== 搜索栏 ==============
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { newQuery ->
                        searchQuery = newQuery
                    },
                    label = { Text("搜索物品...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                focusManager.clearFocus()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "清除搜索"
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus() // 搜索确认时收起键盘
                        }
                    )
                )

                // ============== 房间选择行（水平滚动）=============
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedRoom == "全部",
                            onClick = {
                                selectedRoom = "全部"
                                displayMode = "所有物品"
                            },
                            label = { Text("全部") }
                        )
                    }
                    items(rooms) { room ->
                        FilterChip(
                            selected = selectedRoom == room.name,
                            onClick = {
                                selectedRoom = room.name
                                displayMode = "所有物品"
                            },
                            label = { Text(room.name) }
                        )
                    }
                }

                // ============== 模式切换行，仅在选中具体房间时显示 ==============
                if (selectedRoom != "全部") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = displayMode == "所有物品",
                            onClick = { displayMode = "所有物品" },
                            label = { Text("所有物品") }
                        )
                        FilterChip(
                            selected = displayMode == "容器",
                            onClick = { displayMode = "容器" },
                            label = { Text("容器") }
                        )
                    }
                }

                // ============== 显示内容 ==============
                if (displayMode == "所有物品") {
                    // 根据房间过滤，再按搜索关键字过滤
                    val baseList = if (selectedRoom == "全部") items else items.filter { it.room == selectedRoom }
                    val displayedItems = if (searchQuery.trim() == "到期") {
                        val currentTime = System.currentTimeMillis()
                        baseList.filter { item ->
                            item.expirationDate != null && item.reminderDays != null &&
                                    item.expirationDate > currentTime &&
                                    (item.expirationDate - currentTime) <= item.reminderDays * 24 * 60 * 60 * 1000
                        }
                    } else {
                        baseList.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    }

                    if (displayedItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无物品，点击右下角添加")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(displayedItems) { item ->
                                ItemRow(
                                    item = item,
                                    onClick = {
                                        navController.navigate(Screen.ItemForm.createRoute(item.id))
                                    },
                                    onDelete = {
                                        itemViewModel.delete(item)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // 显示容器
                    if (containerList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无容器，点击右下角添加")
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(containerList.size) { index ->
                                val container = containerList[index]
                                ContainerCard(
                                    container = container,
                                    onClick = {
                                        navController.navigate(
                                            Screen.ContainerScreen.createRoute(
                                                selectedRoom,
                                                container.name
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("请选择操作") },
            text = {
                Column {
                    TextButton(onClick = {
                        showAddDialog = false
                        val defaultRoom = if (selectedRoom != "全部") selectedRoom else ""
                        navController.navigate(Screen.ItemForm.createRoute(0, defaultRoom, ""))
                    }) { Text("添加物品") }
                    if (selectedRoom != "全部") {
                        TextButton(onClick = {
                            showAddDialog = false
                            navController.navigate(Screen.AddContainer.createRoute(selectedRoom))
                        }) { Text("添加容器") }
                    }
                    TextButton(onClick = {
                        showAddDialog = false
                        navController.navigate("room_management")
                    }) {
                        Text("管理房间")
                    }
                    TextButton(onClick = {
                        showAddDialog = false
                        navController.navigate("item_category_management")
                    }) { Text("管理物品类别") }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
