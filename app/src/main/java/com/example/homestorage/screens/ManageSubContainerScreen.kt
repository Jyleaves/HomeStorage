package com.example.homestorage.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.homestorage.data.SubContainerEntity
import com.example.homestorage.viewmodel.ItemViewModel
import com.example.homestorage.viewmodel.SubContainerViewModel
import com.example.homestorage.viewmodel.ThirdContainerViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSubContainerScreen(
    navController: NavController,
    room: String,
    container: String,
    itemViewModel: ItemViewModel = viewModel(),
    subContainerViewModel: SubContainerViewModel = viewModel(),
    thirdContainerViewModel: ThirdContainerViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // 保存当前主容器下的所有二级容器记录
    var subContainerEntities by remember { mutableStateOf(emptyList<SubContainerEntity>()) }

    // 定义方法重新获取二级容器数据
    fun fetchSubContainers() {
        coroutineScope.launch {
            subContainerEntities = subContainerViewModel.getSubContainers(room, container)
                .firstOrNull() ?: emptyList()
        }
    }
    LaunchedEffect(room, container) { fetchSubContainers() }

    // 提取二级容器名称列表
    val subContainerNames = subContainerEntities.map { it.subContainerName }

    // 当前选中的二级容器（默认选中第一个，如有）
    var selectedSubContainer by remember { mutableStateOf("") }
    LaunchedEffect(subContainerNames) {
        if (selectedSubContainer.isEmpty() && subContainerNames.isNotEmpty()) {
            selectedSubContainer = subContainerNames.first()
        }
    }
    // 用于编辑时显示的二级容器名称
    var newSubContainerName by remember { mutableStateOf(selectedSubContainer) }
    LaunchedEffect(selectedSubContainer) {
        newSubContainerName = selectedSubContainer
    }

    // 当 selectedSubContainer 或 subContainerEntities 变化时，同步更新三级容器复选框状态
    var needThirdLevel by remember { mutableStateOf(false) }
    LaunchedEffect(selectedSubContainer, subContainerEntities) {
        val entity = subContainerEntities.find { it.subContainerName == selectedSubContainer }
        needThirdLevel = entity?.hasThirdContainer ?: false
    }

    // 用于保存当前选中二级容器下的所有三级容器名称
    val thirdContainerList = remember { mutableStateListOf<String>() }
    // 从数据库中加载三级容器数据
    val thirdContainersState by thirdContainerViewModel.getThirdContainers(
        room = room,
        container = container,
        subContainer = selectedSubContainer
    ).collectAsState(initial = emptyList())
    LaunchedEffect(selectedSubContainer, thirdContainersState) {
        thirdContainerList.clear()
        thirdContainerList.addAll(thirdContainersState.map { it.thirdContainerName })
    }

    // 控制二级容器下拉菜单展开状态
    var subDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("管理二级容器") },
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
        // 使用 Surface 捕捉空白处点击以收起键盘
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            // 整体分为上下两部分：上半部分为表单卡片，下半部分为“保存”按钮
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // ========== 上半部分：表单区域 ==========
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 卡片：二级容器设置
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "二级容器设置",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ExposedDropdownMenuBox(
                                expanded = subDropdownExpanded,
                                onExpandedChange = { subDropdownExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedSubContainer,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("选择二级容器") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = subDropdownExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                        .clickable { subDropdownExpanded = !subDropdownExpanded }
                                )
                                ExposedDropdownMenu(
                                    expanded = subDropdownExpanded,
                                    onDismissRequest = { subDropdownExpanded = false }
                                ) {
                                    subContainerNames.forEach { name ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                selectedSubContainer = name
                                                newSubContainerName = name // 同步更新编辑框显示
                                                subDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newSubContainerName,
                                onValueChange = { newSubContainerName = it },
                                label = { Text("修改二级容器名称") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = needThirdLevel,
                                    onCheckedChange = { needThirdLevel = it }
                                )
                                Text("需要三级容器")
                            }
                        }
                    }
                    // 卡片：三级容器管理（仅在需要时显示）
                    if (needThirdLevel) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "三级容器管理",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                thirdContainerList.forEachIndexed { index, thirdName ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = thirdName,
                                            onValueChange = { newValue ->
                                                thirdContainerList[index] = newValue
                                            },
                                            label = { Text("三级容器 ${index + 1}") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false)
                                        )
                                        IconButton(
                                            onClick = { thirdContainerList.removeAt(index) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "删除"
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                OutlinedButton(
                                    onClick = { thirdContainerList.add("") },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "添加")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("添加三级容器")
                                }
                            }
                        }
                    }
                }
                // ========== 底部“保存”按钮 ==========
                Button(
                    onClick = {
                        if (selectedSubContainer.isNotBlank() && newSubContainerName.isNotBlank()) {
                            coroutineScope.launch {
                                // 若名称有变，则更新选中记录
                                if (selectedSubContainer != newSubContainerName) {
                                    subContainerViewModel.updateSubContainer(
                                        room = room,
                                        container = container,
                                        oldSubContainer = selectedSubContainer,
                                        newSubContainer = newSubContainerName
                                    )
                                    itemViewModel.updateSubContainerName(
                                        room = room,
                                        container = container,
                                        oldSubContainer = selectedSubContainer,
                                        newSubContainer = newSubContainerName
                                    )
                                    selectedSubContainer = newSubContainerName
                                }
                                // 更新是否支持三级容器状态
                                subContainerViewModel.updateHasThirdContainer(
                                    room = room,
                                    containerName = container,
                                    subContainerName = newSubContainerName,
                                    hasThirdContainer = needThirdLevel
                                )
                                // 若需要三级容器，则更新对应数据
                                if (needThirdLevel) {
                                    thirdContainerViewModel.insertOrUpdateThirdContainers(
                                        room = room,
                                        container = container,
                                        subContainer = newSubContainerName,
                                        thirdContainers = thirdContainerList.toList()
                                    )
                                }
                                Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                                // 保存后重新刷新数据（页面不退出）
                                fetchSubContainers()
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
