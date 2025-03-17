package com.example.homestorage.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.homestorage.data.SubContainerEntity
import com.example.homestorage.viewmodel.ItemViewModel
import com.example.homestorage.viewmodel.SubContainerViewModel
import com.example.homestorage.viewmodel.ThirdContainerViewModel
import kotlinx.coroutines.delay
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
            subContainerEntities = subContainerViewModel
                .getSubContainers(room, container)
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

    // 每次切换选中的二级容器或数据库数据更新时，重置thirdContainerList
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
                },
                // 将「保存」按钮移到右上角，并加上保存图标
                actions = {
                    TextButton(
                        onClick = {
                            // 点击保存
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
                                        // 同步更新 Item 表中的子容器引用
                                        itemViewModel.updateSubContainerName(
                                            room = room,
                                            container = container,
                                            oldSubContainer = selectedSubContainer,
                                            newSubContainer = newSubContainerName
                                        )
                                        // 同步修改当前选中
                                        selectedSubContainer = newSubContainerName
                                    }

                                    // 更新是否需要三级容器
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
                                    // 保存后刷新二级容器数据
                                    fetchSubContainers()
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
        // 点击空白处收起键盘
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),   // 给顶部一点空白
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1) 上方固定不动的「二级容器设置」卡片
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),  // 左右留白
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "二级容器设置",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // 下拉菜单选择二级容器
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
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = subDropdownExpanded
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(
                                        MenuAnchorType.PrimaryNotEditable,
                                        enabled = true
                                    )
                                    .clickable {
                                        subDropdownExpanded = !subDropdownExpanded
                                    }
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
                                            newSubContainerName = name
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

                // 2) 若需要三级容器，则显示中间卡片：三级容器管理
                if (needThirdLevel) {
                    val scrollState = rememberScrollState()

                    // 用 Box + weight(1f) 让这张卡片占据下方剩余空间
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp) // 在底部留些空白，让界面更美观
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
                                    text = "三级容器管理",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // 让「列表」可滚动
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(scrollState),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
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
                                                keyboardOptions = KeyboardOptions(
                                                    capitalization = KeyboardCapitalization.None,
                                                    autoCorrectEnabled = false
                                                )
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
                                    }
                                }

                                // 点击「添加」后自动滚动到底部
                                OutlinedButton(
                                    onClick = {
                                        thirdContainerList.add("")
                                        coroutineScope.launch {
                                            delay(50)
                                            scrollState.animateScrollTo(scrollState.maxValue)
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "添加")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("添加三级容器")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
