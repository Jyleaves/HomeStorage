package com.example.homestorage.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.homestorage.viewmodel.RoomViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoomScreen(
    navController: NavController,
    viewModel: RoomViewModel = viewModel()
) {
    // 从 BackStackEntry 中获取传入的 roomName 参数
    val roomNameParam = navController.currentBackStackEntry?.arguments?.getString("roomName")
    // 判断是否为编辑模式
    val isEditMode = !roomNameParam.isNullOrBlank()

    // 定义房间名称状态，类似于容器添加界面的处理
    val roomNameState = remember { mutableStateOf("") }
    LaunchedEffect(roomNameParam) {
        if (roomNameParam != null) {
            roomNameState.value = roomNameParam
        }
    }

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "编辑房间" else "添加房间") },
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
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = roomNameState.value,
                            onValueChange = { roomNameState.value = it },
                            label = { Text("房间名称") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
                Button(
                    onClick = {
                        val newRoomName = roomNameState.value.trim()
                        if (newRoomName.isNotBlank()) {
                            if (isEditMode) {
                                // 编辑状态时调用更新方法（传入旧名称和新名称）
                                viewModel.updateRoom(roomNameParam!!, newRoomName)
                            } else {
                                viewModel.insertRoom(newRoomName)
                            }
                            navController.popBackStack()
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
