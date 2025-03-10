// HomeScreen.kt
package com.example.homestorage.screens

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.homestorage.components.*
import com.example.homestorage.components.ItemViewMode
import com.example.homestorage.data.ContainerEntity
import com.example.homestorage.data.Item
import com.example.homestorage.navigation.Screen
import com.example.homestorage.viewmodel.ContainerViewModel
import com.example.homestorage.viewmodel.ItemViewModel
import com.example.homestorage.viewmodel.RoomViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    itemViewModel: ItemViewModel = viewModel(),
    roomViewModel: RoomViewModel = viewModel(),
    containerViewModel: ContainerViewModel = viewModel()
) {
    val rooms = roomViewModel.allRooms.collectAsState(initial = emptyList()).value
    var selectedRoom by rememberSaveable { mutableStateOf("全部") }
    var displayMode by rememberSaveable { mutableStateOf("所有物品") }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current

    // 容器数据
    var containerList by remember { mutableStateOf(emptyList<ContainerEntity>()) }
    LaunchedEffect(selectedRoom) {
        if (selectedRoom != "全部") {
            containerViewModel.getContainersByRoom(selectedRoom).collect { containerList = it }
        } else {
            containerList = emptyList()
        }
    }

    // 多选逻辑
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<Item>() }
    var selectedLocation by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    val onClick: (Item) -> Unit = { item ->
        if (isSelectionMode) {
            if (item in selectedItems) {
                selectedItems.remove(item)
                isSelectionMode = selectedItems.isNotEmpty()
            } else {
                selectedItems.add(item)
            }
        } else {
            navController.navigate("item_form/${item.id}") {
                launchSingleTop = true
                popUpTo("home") { saveState = false }
            }
            Log.d("ItemFormScreen", "Editing item with id: ${item.id}")
        }
    }

    fun handleBatchEdit() {
        if (selectedItems.isNotEmpty()) {
            val allSameLocation = selectedItems.all { it.getFullLocation() == selectedLocation }
            if (allSameLocation) {
                navController.navigate("batch_edit/${selectedLocation}/${selectedItems.joinToString(",") { it.id.toString() }}")
                selectedItems.clear()
            } else {
                Toast.makeText(context, "只能编辑相同位置的物品", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 过滤物品
    val filteredItems by itemViewModel.getFilteredItemsForContainer(
        room = selectedRoom,
        container = "", // HomeScreen 不区分容器，可传空或特殊标记
        filterMode = "category", // 固定一种模式
        selectedCategory = "全部",  // 或传 "全部"
        selectedSubContainer = "全部",
        selectedThirdContainer = "全部",
        searchQuery = searchQuery
    ).collectAsState(initial = emptyList())

    var itemViewMode by remember { mutableStateOf(ItemViewMode.LIST) }

    Scaffold(
        topBar = {
            // 默认导航栏
            TopAppBar(
                title = { Text("家庭物品归类整理", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate("export_import") }) {
                        Icon(Icons.Default.ImportExport, contentDescription = "导入/导出")
                    }
                    ItemViewToggle(onViewModeChange = { itemViewMode = it })
                }
            )
            // 多选状态下的导航栏
            if (isSelectionMode) {
                SelectionTopBar(
                    selectedCount = selectedItems.size,
                    onBatchEdit = { handleBatchEdit() },
                    onBatchDelete = { if (selectedItems.isNotEmpty()) { showDeleteConfirm = true } },
                    onCancelSelection = {
                        selectedItems.clear()
                        isSelectionMode = false
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        }
    ) { innerPadding ->
        DismissKeyboardBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 搜索栏
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClear = { searchQuery = "" }
                )
                // 房间选择
                FilterChipRow(
                    options = rooms.map { it.name },
                    selectedOption = selectedRoom,
                    onOptionSelected = {
                        selectedRoom = it
                        displayMode = "所有物品"
                    },
                    allLabel = "全部"
                )
                // 仅在选中具体房间时显示模式切换
                if (selectedRoom != "全部") {
                    DisplayModeToggle(
                        currentMode = displayMode,
                        onModeChange = { displayMode = it }
                    )
                }
                // 内容显示区域
                if (displayMode == "所有物品") {
                    if (filteredItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无物品，点击右下角添加")
                        }
                    } else {
                        if (itemViewMode == ItemViewMode.LIST) {
                            ItemListView(
                                items = filteredItems,
                                selectedItems = selectedItems,
                                isSelectionMode = isSelectionMode,
                                onItemClick = onClick,
                                onItemLongClick = { item ->
                                    if (!isSelectionMode) {
                                        selectedLocation = item.getFullLocation()
                                        isSelectionMode = true
                                    }
                                    if (item.getFullLocation() == selectedLocation) {
                                        if (selectedItems.contains(item)) {
                                            selectedItems.remove(item)
                                        } else {
                                            selectedItems.add(item)
                                        }
                                        isSelectionMode = selectedItems.isNotEmpty()
                                    }
                                },
                                onDelete = { itemViewModel.delete(it) }
                            )
                        } else {
                            ItemGridView(
                                items = filteredItems,
                                isSelectionMode = isSelectionMode,             // 父级维护的多选状态
                                selectedItems = selectedItems,                 // 父级保存的已选中物品列表
                                onItemClick = onClick,
                                onSelectToggle = { item ->
                                    // 切换当前物品的选中状态
                                    if (selectedItems.contains(item)) {
                                        selectedItems.remove(item)
                                    } else {
                                        selectedItems.add(item)
                                    }
                                },
                                onDelete = { itemViewModel.delete(it) },
                                onLongPress = { item ->
                                    if (!isSelectionMode) {
                                        selectedLocation = item.getFullLocation()
                                        isSelectionMode = true
                                    }
                                    if (item.getFullLocation() == selectedLocation) {
                                        if (selectedItems.contains(item)) {
                                            selectedItems.remove(item)
                                        } else {
                                            selectedItems.add(item)
                                        }
                                        isSelectionMode = selectedItems.isNotEmpty()
                                    }
                                }
                            )
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
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 150.dp),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(containerList.size) { index ->
                                val container = containerList[index]
                                ContainerCard(
                                    container = container,
                                    onClick = {
                                        navController.navigate("container_screen/${selectedRoom}/${container.name}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 添加操作对话框
    if (showAddDialog) {
        AddActionDialog(
            selectedRoom = selectedRoom,
            onDismiss = { showAddDialog = false },
            onAddItem = {
                showAddDialog = false
                val defaultRoom = if (selectedRoom != "全部") selectedRoom else "全部"
                navController.navigate(
                    Screen.ItemForm.createRoute(
                        itemId = 0,
                        defaultRoom = defaultRoom,
                        defaultContainer = "",
                        defaultCategory = "",
                        defaultSubContainer = "",
                        defaultThirdContainer = ""
                    )
                )
            },
            onAddContainer = {
                showAddDialog = false
                navController.navigate(Screen.AddContainer.createRoute(selectedRoom, ""))
            },
            onManageRoom = {
                showAddDialog = false
                navController.navigate("room_management")
            },
            onManageItemCategory = {
                showAddDialog = false
                navController.navigate("item_category_management")
            }
        )
    }

    // 删除确认对话框
    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            itemsToDelete = selectedItems.toList(),
            onConfirm = {
                showDeleteConfirm = false
                val itemsToDelete = selectedItems.toList()
                selectedItems.clear()
                isSelectionMode = false
                itemViewModel.deleteItems(itemsToDelete)
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
