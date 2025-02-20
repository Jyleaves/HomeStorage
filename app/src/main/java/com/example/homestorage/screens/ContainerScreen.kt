package com.example.homestorage.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.homestorage.navigation.Screen
import com.example.homestorage.components.ItemRow
import com.example.homestorage.viewmodel.ItemViewModel
import com.example.homestorage.viewmodel.ContainerViewModel
import com.example.homestorage.viewmodel.SubContainerViewModel
import com.example.homestorage.viewmodel.ThirdContainerViewModel

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
    val filteredItems = when (filterMode) {
        "category" -> {
            if (selectedCategory == "全部") items
            else items.filter { it.category == selectedCategory }
        }
        "subcontainer" -> {
            // 先按二级容器过滤
            val bySubContainer = if (selectedSubContainer == "全部") {
                items
            } else {
                items.filter { it.subContainer == selectedSubContainer }
            }
            // 如果该二级容器还有三级容器可选，则继续过滤
            if (thirdContainerNames.isNotEmpty() && selectedSubContainer != "全部") {
                if (selectedThirdContainer == "全部") bySubContainer
                else bySubContainer.filter { it.thirdContainer == selectedThirdContainer }
            } else {
                bySubContainer
            }
        }
        else -> items
    }

    // ========== 更多操作菜单（编辑 / 删除容器） ==========
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

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
            if (filterMode == "category") {
                if (inferredCategories.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = (selectedCategory == "全部"),
                                onClick = { selectedCategory = "全部" },
                                label = { Text("全部") }
                            )
                        }
                        items(inferredCategories) { cat ->
                            FilterChip(
                                selected = (selectedCategory == cat),
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
            } else {
                // subcontainer 模式
                if (subContainerNames.isNotEmpty()) {
                    // ========== 二级容器过滤 ==========
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = (selectedSubContainer == "全部"),
                                onClick = { selectedSubContainer = "全部" },
                                label = { Text("全部") }
                            )
                        }
                        items(subContainerNames) { sc ->
                            FilterChip(
                                selected = (selectedSubContainer == sc),
                                onClick = { selectedSubContainer = sc },
                                label = { Text(sc) }
                            )
                        }
                    }

                    // ========== 7. 如果当前二级容器支持三级容器，则再显示三级容器过滤 ==========
                    //   注意：只有当 selectedSubContainer != "全部" 且 thirdContainerNames 不为空时才显示
                    if (selectedSubContainer != "全部" && thirdContainerNames.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = (selectedThirdContainer == "全部"),
                                    onClick = { selectedThirdContainer = "全部" },
                                    label = { Text("全部") }
                                )
                            }
                            items(thirdContainerNames) { tc ->
                                FilterChip(
                                    selected = (selectedThirdContainer == tc),
                                    onClick = { selectedThirdContainer = tc },
                                    label = { Text(tc) }
                                )
                            }
                        }
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems) { item ->
                        ItemRow(
                            item = item,
                            onClick = {
                                // 这里编辑物品，不需要预填
                                navController.navigate(Screen.ItemForm.createRoute(item.id))
                            },
                            onDelete = {
                                itemViewModel.delete(item)
                            }
                        )
                    }
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
}
