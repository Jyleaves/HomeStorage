// BatchEditScreen.kt
package com.example.homestorage.screens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.homestorage.data.ContainerEntity
import com.example.homestorage.data.SubContainerEntity
import com.example.homestorage.data.ThirdContainerEntity
import com.example.homestorage.viewmodel.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchEditScreen(
    navController: NavController,
    location: String,
    ids: String,
    itemViewModel: ItemViewModel = viewModel(),
    roomViewModel: RoomViewModel = viewModel(),
    containerViewModel: ContainerViewModel = viewModel(),
    subContainerViewModel: SubContainerViewModel = viewModel(),
    thirdContainerViewModel: ThirdContainerViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 解析传入的 ID 列表
    val itemIds = remember(ids) { ids.split(",").map { it.toInt() } }

    // 获取需要编辑的物品列表
    val allItems = itemViewModel.allItems.collectAsState().value
    val itemsToEdit = remember(allItems) {
        allItems.filter { itemIds.contains(it.id) }
    }

    // 定义新的位置状态
    var newRoom by remember { mutableStateOf("") }
    var newContainer by remember { mutableStateOf("") }
    var newSubContainer by remember { mutableStateOf("") }
    var newThirdContainer by remember { mutableStateOf("") }

    // 初始化原始位置
    LaunchedEffect(location) {
        val parts = location.split(" > ")
        newRoom = parts.getOrNull(0) ?: ""
        newContainer = parts.getOrNull(1) ?: ""
        newSubContainer = parts.getOrNull(2) ?: ""
        newThirdContainer = parts.getOrNull(3) ?: ""
    }

    // 下拉菜单相关状态
    var roomExpanded by remember { mutableStateOf(false) }
    var containerExpanded by remember { mutableStateOf(false) }
    var subContainerExpanded by remember { mutableStateOf(false) }
    var thirdContainerExpanded by remember { mutableStateOf(false) }

    // 获取数据
    val allRooms = roomViewModel.allRooms.collectAsState(initial = emptyList()).value
    var containers by remember { mutableStateOf(emptyList<ContainerEntity>()) }
    var subContainers by remember { mutableStateOf(emptyList<SubContainerEntity>()) }
    var thirdContainers by remember { mutableStateOf(emptyList<ThirdContainerEntity>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("批量编辑位置") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (newRoom.isBlank() || newContainer.isBlank()) {
                        Toast.makeText(context, "请填写完整的位置信息", Toast.LENGTH_SHORT).show()
                        return@ExtendedFloatingActionButton
                    }

                    val updatedItems = itemsToEdit.map { item ->
                        item.copy(
                            room = newRoom,
                            container = newContainer,
                            subContainer = newSubContainer.takeIf { it.isNotBlank() },
                            thirdContainer = newThirdContainer.takeIf { it.isNotBlank() }
                        )
                    }

                    itemViewModel.updateItems(updatedItems)
                    navController.popBackStack()
                    Toast.makeText(context, "已更新${updatedItems.size}件物品", Toast.LENGTH_SHORT).show()
                },
                icon = { Icon(Icons.Default.Edit, "保存") },
                text = { Text("保存修改") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 位置编辑卡片（与 ItemFormScreen 类似但更简化）
            ElevatedCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("新位置", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    // 房间选择下拉框
                    ExposedDropdownMenuBox(
                        expanded = roomExpanded,
                        onExpandedChange = {
                            roomExpanded = it
                        }
                    ) {
                        OutlinedTextField(
                            value = newRoom,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("房间") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = roomExpanded
                                )
                            },
                            modifier = Modifier
                                .menuAnchor(
                                    MenuAnchorType.PrimaryNotEditable,
                                    enabled = true
                                )
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = roomExpanded,
                            onDismissRequest = { roomExpanded = false }
                        ) {
                            allRooms.forEach { room ->
                                DropdownMenuItem(
                                    text = { Text(room.name) },
                                    onClick = {
                                        newRoom = room.name
                                        roomExpanded = false
                                        newContainer = ""
                                    }
                                )
                            }
                        }
                    }

                    // 容器选择
                    if (newRoom.isNotEmpty()) {
                        val coroutineScope = rememberCoroutineScope()

                        ExposedDropdownMenuBox(
                            expanded = containerExpanded,
                            onExpandedChange = {
                                containerExpanded = it
                            }
                        ) {
                            OutlinedTextField(
                                value = newContainer,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("容器") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = containerExpanded
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor(
                                        MenuAnchorType.PrimaryNotEditable,
                                        enabled = true
                                    )
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = containerExpanded,
                                onDismissRequest = { containerExpanded = false }
                            ) {
                                // 在展开容器下拉框之前，加载容器数据
                                if (newRoom.isNotEmpty()) {
                                    coroutineScope.launch {
                                        containers = containerViewModel.getContainersByRoom(newRoom).firstOrNull() ?: emptyList()
                                    }
                                }

                                containers.forEach { container ->
                                    DropdownMenuItem(
                                        text = { Text(container.name) },
                                        onClick = {
                                            newContainer = container.name
                                            containerExpanded = false
                                            newSubContainer = ""
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 二级容器
                    if (newContainer.isNotEmpty()) {
                        val coroutineScope = rememberCoroutineScope()
                        coroutineScope.launch {
                            subContainers = subContainerViewModel.getSubContainers(newRoom, newContainer).firstOrNull() ?: emptyList()
                        }

                        if (subContainers.isNotEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = subContainerExpanded,
                                onExpandedChange = { subContainerExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = newSubContainer,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("二级容器") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = subContainerExpanded
                                        )
                                    },
                                    modifier = Modifier
                                        .menuAnchor(
                                            MenuAnchorType.PrimaryNotEditable,
                                            enabled = true
                                        )
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = subContainerExpanded,
                                    onDismissRequest = { subContainerExpanded = false }
                                ) {
                                    subContainers.forEach { sub ->
                                        DropdownMenuItem(
                                            text = { Text(sub.subContainerName) },
                                            onClick = {
                                                newSubContainer = sub.subContainerName
                                                subContainerExpanded = false
                                                newThirdContainer = ""
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 三级容器
                    if (newSubContainer.isNotEmpty()) {
                        val coroutineScope = rememberCoroutineScope()
                        coroutineScope.launch {  // 启动协程获取三级容器数据
                            thirdContainers = thirdContainerViewModel.getThirdContainers(newRoom, newContainer, newSubContainer)
                                .firstOrNull() ?: emptyList()
                        }

                        if (thirdContainers.isNotEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = thirdContainerExpanded,
                                onExpandedChange = { thirdContainerExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = newThirdContainer,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("三级容器") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = thirdContainerExpanded
                                        )
                                    },
                                    modifier = Modifier
                                        .menuAnchor(
                                            MenuAnchorType.PrimaryNotEditable,
                                            enabled = true
                                        )
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = thirdContainerExpanded,
                                    onDismissRequest = { thirdContainerExpanded = false }
                                ) {
                                    thirdContainers.forEach { third ->
                                        DropdownMenuItem(
                                            text = { Text(third.thirdContainerName) },
                                            onClick = {
                                                newThirdContainer = third.thirdContainerName
                                                thirdContainerExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 显示当前选中的物品数量
            Text(
                text = "共选择 ${itemsToEdit.size} 件物品",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
