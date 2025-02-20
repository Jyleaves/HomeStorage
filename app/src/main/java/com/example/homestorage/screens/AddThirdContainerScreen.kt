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
import com.example.homestorage.data.ThirdContainerEntity
import com.example.homestorage.viewmodel.ThirdContainerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddThirdContainerScreen(
    navController: NavController,
    room: String,
    containerName: String,
    subContainerName: String,
    thirdContainerViewModel: ThirdContainerViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 新增三级容器的输入
    var newThirdContainer by remember { mutableStateOf("") }

    // 从数据库获取当前二级容器下所有三级容器
    val thirdContainers by thirdContainerViewModel.getThirdContainers(
        room = room,
        container = containerName,
        subContainer = subContainerName
    ).collectAsState(initial = emptyList())

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加三级容器") },
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
                // 显示房间、主容器、二级容器（都是锁定的，只读）
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
                Text(
                    text = "二级容器：$subContainerName",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                // 新增三级容器输入
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newThirdContainer,
                            onValueChange = { newThirdContainer = it },
                            label = { Text("三级容器名称") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (newThirdContainer.isNotBlank()) {
                                    // 判断是否重名
                                    val currentNames = thirdContainers.map { it.thirdContainerName }
                                    if (newThirdContainer in currentNames) {
                                        Toast.makeText(context, "同名三级容器已存在", Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        val updatedList = currentNames + newThirdContainer
                                        coroutineScope.launch {
                                            // 更新所有三级容器记录：先删再插或直接“insertOrUpdate”都行
                                            thirdContainerViewModel.insertOrUpdateThirdContainers(
                                                room = room,
                                                container = containerName,
                                                subContainer = subContainerName,
                                                thirdContainers = updatedList
                                            )
                                            newThirdContainer = ""
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

                // 显示已有的三级容器列表
                if (thirdContainers.isNotEmpty()) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "已添加的三级容器",
                                style = MaterialTheme.typography.titleMedium
                            )
                            thirdContainers.forEach { third ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = third.thirdContainerName)
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                // 删除指定的三级容器记录
                                                thirdContainerViewModel.deleteThirdContainer(third)
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
