package com.example.homestorage.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

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
    // ========== 1) 读取容器信息，确定是否编辑模式 ==========
    val existingContainer = containerViewModel
        .getContainerByRoomAndName(defaultRoom, containerNameToEdit)
        .collectAsState(initial = null).value
    val isEditMode = (existingContainer != null)

    // ========== 2) 相关状态 ==========
    val containerNameState = remember { mutableStateOf("") }
    val hasSubContainerState = remember { mutableStateOf(false) }
    val subContainerList = remember { mutableStateListOf<String>() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // 编辑模式下，填充已有数据
    LaunchedEffect(existingContainer) {
        if (existingContainer != null) {
            containerNameState.value = existingContainer.name
            hasSubContainerState.value = existingContainer.hasSubContainer
            if (existingContainer.hasSubContainer) {
                val existingSubs = subContainerViewModel
                    .getSubContainers(defaultRoom, existingContainer.name)
                    .firstOrNull() ?: emptyList()
                subContainerList.clear()
                subContainerList.addAll(existingSubs.map { it.subContainerName })
            }
        }
    }

    // ========== 3) Scaffold：将「保存」按钮放到TopAppBar的actions里 ==========
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
                },
                actions = {
                    // 将「保存」按钮移到右上角
                    TextButton(
                        onClick = {
                            val containerName = containerNameState.value.trim()
                            val hasSubContainer = hasSubContainerState.value
                            if (containerName.isNotBlank()) {
                                if (isEditMode) {
                                    // 编辑模式：更新容器 & 二级容器
                                    val oldName = existingContainer.name
                                    containerViewModel.updateContainer(
                                        room = defaultRoom,
                                        oldName = oldName,
                                        newName = containerName,
                                        newHasSubContainer = hasSubContainer
                                    )
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
                                            popUpTo("container_screen/$defaultRoom/$oldName") {
                                                inclusive = true
                                            }
                                        }
                                    } else {
                                        navController.popBackStack()
                                    }
                                } else {
                                    // 新增模式
                                    containerViewModel.insertContainer(
                                        defaultRoom,
                                        containerName,
                                        hasSubContainer
                                    )
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
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("保存")
                    }
                }
            )
        }
    ) { innerPadding ->
        // 监听点击空白处收起键盘
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            // ========== 4) 两张卡片：第一张容器设置，第二张二级容器管理 ==========
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),    // 顶部留一点空白
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ----- 第一张卡片：容器名称 & 是否需要二级容器 -----
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "房间：$defaultRoom",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = containerNameState.value,
                            onValueChange = { containerNameState.value = it },
                            label = { Text("容器名称") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = hasSubContainerState.value,
                                onCheckedChange = { hasSubContainerState.value = it }
                            )
                            Text("需要二级容器")
                        }
                    }
                }

                // ----- 第二张卡片：二级容器管理（可局部滚动，占满下方剩余空间） -----
                if (hasSubContainerState.value) {
                    val scrollState = rememberScrollState()

                    // 使用 weight(1f) 让它占满下方剩余空间
                    Box(
                        modifier = Modifier
                            .weight(1f)                         // 占用剩余高度
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)             // 在底部留空白，更美观
                    ) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxSize(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "二级容器管理",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // 列表可滚动
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(scrollState),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
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
                                                onClick = {
                                                    subContainerList.removeAt(index)
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "删除"
                                                )
                                            }
                                        }
                                    }
                                }

                                // 添加按钮：点击后自动滚动到底部
                                OutlinedButton(
                                    onClick = {
                                        subContainerList.add("")
                                        coroutineScope.launch {
                                            delay(50)
                                            scrollState.animateScrollTo(scrollState.maxValue)
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "添加")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("添加二级容器")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
