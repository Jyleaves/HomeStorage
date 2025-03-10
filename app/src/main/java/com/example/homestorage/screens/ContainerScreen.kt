package com.example.homestorage.screens

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.homestorage.components.DeleteConfirmDialog
import com.example.homestorage.components.FilterChipRow
import com.example.homestorage.components.ItemGridView
import com.example.homestorage.components.ItemListView
import com.example.homestorage.navigation.Screen
import com.example.homestorage.components.ItemViewMode
import com.example.homestorage.components.ItemViewToggle
import com.example.homestorage.components.SelectionTopBar
import com.example.homestorage.data.Item
import com.example.homestorage.viewmodel.ItemViewModel
import com.example.homestorage.viewmodel.ContainerViewModel
import com.example.homestorage.viewmodel.SubContainerViewModel
import com.example.homestorage.viewmodel.ThirdContainerViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerScreen(
    navController: NavController,
    room: String,
    container: String,
    itemViewModel: ItemViewModel = viewModel(),
    containerViewModel: ContainerViewModel = viewModel(),
    subContainerViewModel: SubContainerViewModel = viewModel(),
    // 1. 注入 ThirdContainerViewModel
    thirdContainerViewModel: ThirdContainerViewModel = viewModel()
) {
    // ========== 获取容器内所有物品 ==========
    val items = itemViewModel.allItems.collectAsState().value.filter {
        it.room == room && it.container == container
    }

    // ========== 1. 定义过滤模式 ==========
    // "category" 表示按物品类别，"subcontainer" 表示按二级容器
    var filterMode by remember { mutableStateOf("category") }
    var itemViewMode by remember { mutableStateOf(ItemViewMode.LIST) }

    // ========== 2. 获取可供筛选的类别和二级容器 ==========
    // 物品类别
    val inferredCategories = items.map { it.category }.distinct().sorted()
    var selectedCategory by remember { mutableStateOf("全部") }

    // 二级容器列表（从数据库获取）
    val subContainers = subContainerViewModel.getSubContainers(room, container)
        .collectAsState(initial = emptyList()).value
    // 提取二级容器名称列表
    val subContainerNames = subContainers.map { it.subContainerName }.distinct().sorted()
    var selectedSubContainer by remember { mutableStateOf("全部") }

    // 3. 当用户切换二级容器时，重置第三层容器选择为“全部”
    var selectedThirdContainer by remember { mutableStateOf("全部") }
    LaunchedEffect(selectedSubContainer) {
        selectedThirdContainer = "全部"
    }

    // 4. 根据 selectedSubContainer，从数据库加载对应的三级容器
    val thirdContainersState = thirdContainerViewModel.getThirdContainers(
        room = room,
        container = container,
        subContainer = selectedSubContainer
    ).collectAsState(initial = emptyList()).value
    // 提取三级容器名称列表
    val thirdContainerNames = thirdContainersState.map { it.thirdContainerName }.distinct().sorted()

    // ========== 3. 根据筛选模式过滤物品 ==========
    val filteredItems by itemViewModel.getFilteredItemsForContainer(
        room = room,
        container = container,
        filterMode = filterMode,
        selectedCategory = selectedCategory,
        selectedSubContainer = selectedSubContainer,
        selectedThirdContainer = selectedThirdContainer,
        searchQuery = ""  // 或者你在 ContainerScreen 中也加入搜索框，此处传入搜索关键词
    ).collectAsState(initial = emptyList())

    // ========== 更多操作菜单（编辑 / 删除容器） ==========
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // ========== 多选模式状态 ==========
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<Item>() }
    var selectedLocation by remember { mutableStateOf("") } // 当前选中物品的统一位置
    var showDeleteConfirm by remember { mutableStateOf(false) }

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

    // 新增上下文获取
    val context = LocalContext.current

    // 将处理函数定义为普通函数
    fun handleBatchEdit() {
        if (selectedItems.isNotEmpty()) {
            val allSameLocation = selectedItems.all {
                it.getFullLocation() == selectedLocation
            }

            if (allSameLocation) {
                navController.navigate(
                    Screen.BatchEdit.createRoute(
                        selectedLocation,
                        selectedItems.joinToString(",") { it.id.toString() }
                    )
                )
                selectedItems.clear()
            } else {
                Toast.makeText(
                    context, // 使用外部获取的context
                    "只能编辑相同位置的物品",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$room - $container", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 添加视图切换组件
                    ItemViewToggle(onViewModeChange = { itemViewMode = it })

                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("编辑容器") },
                            onClick = {
                                menuExpanded = false
                                navController.navigate(
                                    Screen.AddContainer.createRoute(
                                        defaultRoom = room,
                                        containerNameToEdit = container
                                    )
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("删除容器") },
                            onClick = {
                                menuExpanded = false
                                showDeleteDialog = true
                            }
                        )
                        if (subContainerNames.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("管理二级容器") },
                                onClick = {
                                    menuExpanded = false
                                    // 导航到管理二级容器页面
                                    navController.navigate(Screen.ManageSubContainer.createRoute(room, container))
                                }
                            )
                        }
                    }
                }
            )
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
            FloatingActionButton(
                onClick = {
                    // ========== 4. FAB 点击时，根据筛选模式预填信息 ==========
                    when (filterMode) {
                        "category" -> {
                            // 如果用户选中了具体类别，就预填，否则不填
                            val prefillCat = if (selectedCategory != "全部") selectedCategory else ""
                            navController.navigate(
                                Screen.ItemForm.createRoute(
                                    itemId = 0,
                                    defaultRoom = room,
                                    defaultContainer = container,
                                    defaultCategory = prefillCat,
                                    defaultSubContainer = "",
                                    defaultThirdContainer = ""
                                )
                            )
                        }
                        "subcontainer" -> {
                            // 如果用户选中了具体二级容器，就预填，否则不填
                            val prefillSub = if (selectedSubContainer != "全部") selectedSubContainer else ""
                            // 三级容器
                            val prefillThird = if (selectedThirdContainer != "全部") selectedThirdContainer else ""

                            navController.navigate(
                                Screen.ItemForm.createRoute(
                                    itemId = 0,
                                    defaultRoom = room,
                                    defaultContainer = container,
                                    defaultCategory = "",
                                    defaultSubContainer = prefillSub,
                                    defaultThirdContainer = prefillThird
                                )
                            )
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ========== 5. 先放一个切换筛选模式的 Row ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = (filterMode == "category"),
                    onClick = { filterMode = "category" },
                    label = { Text("按类别") }
                )
                FilterChip(
                    selected = (filterMode == "subcontainer"),
                    onClick = { filterMode = "subcontainer" },
                    label = { Text("按二级容器") }
                )
            }

            // ========== 6. 根据 filterMode 显示类别Chip或二级容器Chip ==========
            if (filterMode == "category" && inferredCategories.isNotEmpty()) {
                FilterChipRow(
                    options = inferredCategories,
                    selectedOption = selectedCategory,
                    onOptionSelected = { selectedCategory = it }
                )
            } else {
                if (filterMode == "subcontainer" && subContainerNames.isNotEmpty()) {
                    FilterChipRow(
                        options = subContainerNames,
                        selectedOption = selectedSubContainer,
                        onOptionSelected = { selectedSubContainer = it }
                    )
                    if (selectedSubContainer != "全部" && thirdContainerNames.isNotEmpty()) {
                        FilterChipRow(
                            options = thirdContainerNames,
                            selectedOption = selectedThirdContainer,
                            onOptionSelected = { selectedThirdContainer = it }
                        )
                    }
                }
            }

            // ========== 8. 显示物品列表 ==========
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("该容器暂无物品")
                }
            } else {
                if (itemViewMode == ItemViewMode.LIST) {
                    // 使用组件中的 ItemListView，复用已有代码
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
                            // 判断位置是否一致，添加或移除选中项
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
                    // 网格模式，使用 ItemGridView 组件，参数可以根据需要进行调整
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
        }
    }

    // ========== 9. 删除容器确认弹窗 ==========
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除容器") },
            text = { Text("确定要删除此容器吗？该容器下的所有物品将失去容器关联。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        // 1) 删除容器
                        containerViewModel.deleteContainer(room, container)
                        // 2) 把该容器下的物品 container 字段置空或其他默认值
                        itemViewModel.updateItemsForContainerChange(
                            room = room,
                            oldContainerName = container,
                            newContainerName = ""
                        )
                        navController.popBackStack()
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

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
