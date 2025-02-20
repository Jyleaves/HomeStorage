// AddContainer.kt
package com.example.homestorage.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.homestorage.navigation.Screen
import com.example.homestorage.viewmodel.ContainerViewModel
import com.example.homestorage.viewmodel.ItemViewModel
import com.example.homestorage.viewmodel.SubContainerViewModel
import kotlinx.coroutines.flow.firstOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContainerScreen(
    navController: NavController,
    defaultRoom: String,
    containerNameToEdit: String = "",
    containerViewModel: ContainerViewModel = viewModel(),
    itemViewModel: ItemViewModel = viewModel(),
    subContainerViewModel: SubContainerViewModel = viewModel()
) {
    // 从数据库中获取容器数据（用于编辑模式）
    val existingContainer = containerViewModel
        .getContainerByRoomAndName(defaultRoom, containerNameToEdit)
        .collectAsState(initial = null).value

    val isEditMode = (existingContainer != null)

    // 容器名称和是否有二级容器状态
    val containerNameState = remember { mutableStateOf("") }
    val hasSubContainerState = remember { mutableStateOf(false) }
    // 动态二级容器名称列表
    val subContainerList = remember { mutableStateListOf<String>() }

    // 当查询到数据库中的容器后，填充状态（编辑模式）
    LaunchedEffect(existingContainer) {
        if (existingContainer != null) {
            containerNameState.value = existingContainer.name
            hasSubContainerState.value = existingContainer.hasSubContainer
            if (existingContainer.hasSubContainer) {
                // 从二级容器表中加载数据
                val existingSubs = subContainerViewModel
                    .getSubContainers(defaultRoom, existingContainer.name)
                    .firstOrNull() ?: emptyList()
                subContainerList.clear()
                subContainerList.addAll(existingSubs.map { it.subContainerName })
            }
        }
    }

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "编辑容器" else "添加容器")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        // 顶层容器，监听点击空白处收起键盘
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            // 整体分为上下两部分：上方是表单，底部是“保存”按钮
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // ========== 上半部分：卡片中的输入区域 ==========
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 显示房间信息
                        Text(
                            text = "房间：$defaultRoom",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // 容器名称
                        OutlinedTextField(
                            value = containerNameState.value,
                            onValueChange = { containerNameState.value = it },
                            label = { Text("容器名称") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // 是否有二级容器
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = hasSubContainerState.value,
                                onCheckedChange = { hasSubContainerState.value = it }
                            )
                            Text("需要二级容器")
                        }

                        // 如果选中“有二级容器”，显示动态输入区域
                        if (hasSubContainerState.value) {
                            // 用一个小的标题来提示分区
                            Text(
                                text = "二级容器列表",
                                style = MaterialTheme.typography.titleMedium
                            )
                            // 列表
                            subContainerList.forEachIndexed { index, subContainerName ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = subContainerName,
                                        onValueChange = { newValue ->
                                            subContainerList[index] = newValue
                                        },
                                        label = { Text("二级容器 ${index + 1}") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    IconButton(
                                        onClick = { subContainerList.removeAt(index) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "删除"
                                        )
                                    }
                                }
                            }
                            // 添加二级容器按钮
                            OutlinedButton(
                                onClick = { subContainerList.add("") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "添加")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("添加二级容器")
                            }
                        }
                    }
                }

                // ========== 底部“保存”按钮 ==========
                Button(
                    onClick = {
                        val containerName = containerNameState.value.trim()
                        val hasSubContainer = hasSubContainerState.value
                        if (containerName.isNotBlank()) {
                            if (isEditMode && existingContainer != null) {
                                val oldName = existingContainer.name
                                // 更新容器信息
                                containerViewModel.updateContainer(
                                    room = defaultRoom,
                                    oldName = oldName,
                                    newName = containerName,
                                    newHasSubContainer = hasSubContainer
                                )
                                // 更新二级容器数据
                                subContainerViewModel.updateSubContainers(
                                    room = defaultRoom,
                                    oldContainerName = oldName,
                                    newContainerName = containerName,
                                    subContainers = subContainerList.toList()
                                )
                                if (oldName != containerName) {
                                    itemViewModel.updateItemsForContainerChange(
                                        room = defaultRoom,
                                        oldContainerName = oldName,
                                        newContainerName = containerName
                                    )
                                    navController.navigate(
                                        Screen.ContainerScreen.createRoute(defaultRoom, containerName)
                                    ) {
                                        popUpTo("container_screen/$defaultRoom/$oldName") { inclusive = true }
                                    }
                                } else {
                                    navController.popBackStack()
                                }
                            } else {
                                // 插入新容器
                                containerViewModel.insertContainer(
                                    defaultRoom,
                                    containerName,
                                    hasSubContainer
                                )
                                // 插入对应的二级容器数据（如果有）
                                if (hasSubContainer) {
                                    subContainerViewModel.insertOrUpdateSubContainers(
                                        room = defaultRoom,
                                        container = containerName,
                                        subContainers = subContainerList.toList()
                                    )
                                }
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("保存")
                }
            }
        }
    }
}
