package com.example.homestorage.screens

import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.homestorage.viewmodel.SubContainerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubContainerScreen(
    navController: NavController,
    room: String,
    containerName: String,
    subContainerViewModel: SubContainerViewModel = viewModel()
) {
    // 获取当前上下文，用于 Toast 显示
    val context = LocalContext.current
    // 用于新增二级容器的输入
    var newSubContainer by remember { mutableStateOf("") }
    // 从数据库中获取当前主容器下所有二级容器
    val subContainers by subContainerViewModel.getSubContainers(room, containerName)
        .collectAsState(initial = emptyList())
    // 获取协程作用域，用于异步操作
    val coroutineScope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加二级容器") },
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
        // 整体内容区
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 显示所属房间与主容器信息
                Text(
                    text = "房间：$room",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "主容器：$containerName",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                // 新增二级容器输入区域
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newSubContainer,
                            onValueChange = { newSubContainer = it },
                            label = { Text("二级容器名称") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (newSubContainer.isNotBlank()) {
                                    // 获取已有的二级容器名称列表
                                    val currentNames = subContainers.map { it.subContainerName }
                                    if (newSubContainer in currentNames) {
                                        Toast
                                            .makeText(context, "同名二级容器已存在", Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        // 合并当前已有的名称和新输入的名称
                                        val updatedList = currentNames + newSubContainer
                                        coroutineScope.launch {
                                            // 更新所有二级容器记录：删除旧记录，再插入合并后的新记录
                                            subContainerViewModel.insertOrUpdateSubContainers(
                                                room = room,
                                                container = containerName,
                                                subContainers = updatedList
                                            )
                                            newSubContainer = ""
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("添加")
                        }
                    }
                }

                // 显示已有的二级容器列表（如果有）
                if (subContainers.isNotEmpty()) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "已添加的二级容器",
                                style = MaterialTheme.typography.titleMedium
                            )
                            subContainers.forEach { subContainer ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = subContainer.subContainerName)
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                // 删除指定的二级容器记录
                                                subContainerViewModel.deleteSubContainer(room, containerName,
                                                    subContainer.subContainerName
                                                )
                                            }
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
                    }
                }
            }
        }
    }
}
