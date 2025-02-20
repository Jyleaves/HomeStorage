package com.example.homestorage.screens

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.homestorage.data.Item
import com.example.homestorage.data.ContainerEntity
import com.example.homestorage.components.ImageSelector
import com.example.homestorage.data.SubContainerEntity
import com.example.homestorage.data.ThirdContainerEntity
import com.example.homestorage.navigation.Screen
import com.example.homestorage.util.UCropContract
import com.example.homestorage.util.createImageUri
import com.example.homestorage.util.performOcr
import com.example.homestorage.viewmodel.ContainerViewModel
import com.example.homestorage.viewmodel.ItemCategoryViewModel
import com.example.homestorage.viewmodel.ItemViewModel
import com.example.homestorage.viewmodel.RoomViewModel
import com.example.homestorage.viewmodel.SubContainerViewModel
import com.example.homestorage.viewmodel.ThirdContainerViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFormScreen(
    navController: NavController,
    itemId: Int = 0,                      // ≠0 表示编辑；0 表示添加
    defaultRoom: String = "",             // 添加模式下预填房间（不可修改）
    defaultContainer: String = "",        // 添加模式下预填容器（不可修改）
    defaultCategory: String = "",
    defaultSubContainer: String = "",
    defaultThirdContainer: String = "",
    itemViewModel: ItemViewModel = viewModel(),
    roomViewModel: RoomViewModel = viewModel(),
    containerViewModel: ContainerViewModel = viewModel(),
    subContainerViewModel: SubContainerViewModel = viewModel(),
    thirdContainerViewModel: ThirdContainerViewModel = viewModel(),
    itemCategoryViewModel: ItemCategoryViewModel = viewModel()
) {
    val context = LocalContext.current

    // 判断是否为编辑模式
    val isEditScreen = (itemId != 0)
    var isEditing by remember { mutableStateOf(!isEditScreen) }

    // 获取已有物品并查找要编辑的那一条
    val items = itemViewModel.allItems.collectAsState().value
    val existingItem = items.find { it.id == itemId }

    // 定义各字段状态
    var name by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var container by remember { mutableStateOf("") }
    var subContainer by remember { mutableStateOf("") }
    var thirdContainer by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") } // 用来显示类别名称
    var description by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // 新增：生产日期、提醒天数、有效期
    var productionDate by remember { mutableStateOf<Long?>(null) }
    var productionDateStr by remember { mutableStateOf("") }
    var reminderDays by remember { mutableStateOf<Long?>(null) }
    var reminderDaysStr by remember { mutableStateOf("") }
    var expirationDate by remember { mutableStateOf<Long?>(null) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val expirationDateStr = expirationDate?.let { dateFormat.format(Date(it)) } ?: ""
    var quantityStr by remember { mutableStateOf("") }

    // 选中的物品类别（包含属性定义）
    var selectedItemCategory by remember {
        mutableStateOf<com.example.homestorage.data.ItemCategory?>(
            null
        )
    }
    var selectedContainer by remember { mutableStateOf<ContainerEntity?>(null) }
    var selectedSubContainer by remember { mutableStateOf<SubContainerEntity?>(null) }
    var selectedThirdContainer by remember { mutableStateOf<ThirdContainerEntity?>(null) }

    // 编辑模式下预填数据
    LaunchedEffect(existingItem) {
        existingItem?.let { item ->
            name = item.name
            room = item.room
            container = item.container
            subContainer = item.subContainer ?: ""
            thirdContainer = item.thirdContainer ?: ""
            category = item.category
            description = item.description
            photoUri = Uri.parse(item.photoUri)
            productionDate = item.productionDate
            productionDateStr = item.productionDate?.let { dateFormat.format(Date(it)) } ?: ""
            reminderDays = item.reminderDays
            reminderDaysStr = item.reminderDays?.toString() ?: ""
            expirationDate = item.expirationDate
            quantityStr = item.quantity?.toString() ?: ""

            // 先获取列表，再在列表中查找
            val list = itemCategoryViewModel.allCategories.first()
            val categoryObj = list.firstOrNull { it.categoryName == item.category }
            selectedItemCategory = categoryObj

            // 获取当前房间所有容器，并查找匹配的容器对象
            val containerList =
                containerViewModel.getContainersByRoom(item.room).firstOrNull() ?: emptyList()
            selectedContainer = containerList.firstOrNull { it.name == item.container }

            if (subContainer.isNotEmpty() && selectedContainer?.hasSubContainer == true) {
                val subList = subContainerViewModel.getSubContainers(item.room, item.container)
                    .firstOrNull() ?: emptyList()
                selectedSubContainer = subList.firstOrNull { it.subContainerName == subContainer }

                if (thirdContainer.isNotEmpty() && selectedSubContainer?.hasThirdContainer == true) {
                    val thirdList = thirdContainerViewModel
                        .getThirdContainers(item.room, item.container, subContainer)
                        .firstOrNull() ?: emptyList()
                    selectedThirdContainer =
                        thirdList.firstOrNull { it.thirdContainerName == thirdContainer }
                }
            }
        }
    }
    // 新建模式时，若有默认房间或容器，则直接填入
    LaunchedEffect(defaultRoom, defaultContainer) {
        if (!isEditScreen) {
            if (defaultRoom.isNotEmpty()) {
                room = defaultRoom
            }
            if (defaultContainer.isNotEmpty()) {
                container = defaultContainer
                // 查询对应的容器实体，并更新 selectedContainer
                val containerList = containerViewModel.getContainersByRoom(defaultRoom).firstOrNull() ?: emptyList()
                selectedContainer = containerList.firstOrNull { it.name == defaultContainer }
            }
            if (defaultCategory.isNotEmpty()) {
                category = defaultCategory
                val categoryObj = itemCategoryViewModel.allCategories.firstOrNull()?.firstOrNull {
                    it.categoryName == defaultCategory
                }
                selectedItemCategory = categoryObj
            }
            if (defaultSubContainer.isNotEmpty()) {
                subContainer = defaultSubContainer
                if (selectedContainer?.hasSubContainer == true) {
                    val subList = subContainerViewModel
                        .getSubContainers(defaultRoom, defaultContainer)
                        .firstOrNull() ?: emptyList()
                    selectedSubContainer = subList.firstOrNull { it.subContainerName == defaultSubContainer }
                }
            }
            if (defaultThirdContainer.isNotEmpty()) {
                thirdContainer = defaultThirdContainer
            }
        }
    }

    // 定义可编辑状态
    val canEditRoom = if (isEditScreen) isEditing else true
    val canEditContainer = if (isEditScreen) isEditing else true
    val canEditCategory = if (isEditScreen) isEditing else true
    val canEditName = if (isEditScreen) isEditing else true
    val canEditDescription = if (isEditScreen) isEditing else true
    val canEditProductionDate = if (isEditScreen) isEditing else true
    val canEditReminderDays = if (isEditScreen) isEditing else true
    val canEditExpirationDate = if (isEditScreen) isEditing else true
    val canEditQuantity = if (isEditScreen) isEditing else true

    // 日期选择对话框（生产日期）
    val productionDateDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth, 0, 0, 0)
                productionDate = cal.timeInMillis
                productionDateStr = dateFormat.format(Date(cal.timeInMillis))
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
    }
    // 日期选择对话框（有效期）
    val expirationDateDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth, 0, 0, 0)
                expirationDate = cal.timeInMillis
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
    }

    // 大图预览弹窗
    var showLargeImageDialog by remember { mutableStateOf(false) }
    if (showLargeImageDialog && photoUri != null) {
        Dialog(onDismissRequest = { showLargeImageDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "大图预览",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showLargeImageDialog = false },
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { showLargeImageDialog = false },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "关闭预览",
                        tint = Color.White
                    )
                }
            }
        }
    }

    // 相册选择
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri
        }
    }
    // 拍照
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val imageCropLauncher = rememberLauncherForActivityResult(contract = UCropContract()) { croppedUri ->
        if (croppedUri != null) {
            photoUri = croppedUri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempPhotoUri != null) {
            val destinationUri = createDestinationUri(context)
            imageCropLauncher.launch(Pair(tempPhotoUri!!, destinationUri))
        }
    }

    fun launchCamera() {
        val uri = createImageUri(context)
        tempPhotoUri = uri
        cameraLauncher.launch(uri)
    }

    // 请求摄像头权限
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(context, "需要摄像头权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchCameraWithPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    // 图片来源选择弹窗（此处保留原方案）
    var showImageSourceDialog by remember { mutableStateOf(false) }
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("选择图片来源") },
            text = { Text("请选择从相册导入还是拍照") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    galleryLauncher.launch("image/*")
                }) { Text("相册") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    launchCameraWithPermission()
                }) { Text("拍照") }
            }
        )
    }

    // 用于保存拍照得到的原始图片 Uri
    val ocrTempPhotoUri = remember { mutableStateOf<Uri?>(null) }

    // 裁剪启动器：裁剪成功后启动 OCR 识别
    val uCropLauncher = rememberLauncherForActivityResult(contract = UCropContract()) { croppedUri ->
        if (croppedUri != null) {
            performOcr(context, croppedUri) { recognizedText ->
                if (recognizedText.isBlank()) {
                    Toast.makeText(context, "未识别到文本，请重试", Toast.LENGTH_SHORT).show()
                } else {
                    // 更新物品名称
                    name = recognizedText
                }
            }
        }
    }

    // 拍照启动器：拍照成功后直接启动裁剪
    val ocrLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && ocrTempPhotoUri.value != null) {
            // 使用 DocumentFile 检查
            val destinationUri = createDestinationUri(context)
            uCropLauncher.launch(Pair(ocrTempPhotoUri.value!!, destinationUri))
            context.revokeUriPermission(
                ocrTempPhotoUri.value!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
    }

    // 摄像头权限请求启动器
    val ocrCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createImageUri(context)
            ocrTempPhotoUri.value = uri
            ocrLauncher.launch(uri)
        } else {
            Toast.makeText(context, "需要摄像头权限才能进行OCR识别", Toast.LENGTH_SHORT).show()
        }
    }

    // 定义下拉菜单展开状态
    var roomExpanded by remember { mutableStateOf(false) }
    var containerExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // 从 ViewModel 获取数据
    val allRooms = roomViewModel.allRooms.collectAsState(initial = emptyList()).value
    var containers by remember { mutableStateOf(emptyList<ContainerEntity>()) }

    var categoriesList by remember { mutableStateOf(emptyList<com.example.homestorage.data.ItemCategory>()) }

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditScreen) "编辑物品" else "添加物品") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (isEditScreen) {
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = if (isEditing) "取消编辑" else "编辑"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isEditing) {
                ExtendedFloatingActionButton(
                    text = { Text("保存") },
                    onClick = {
                        // 保存前解析提醒天数字符串（仅当该类别需要提醒时）
                        reminderDays =
                            if (selectedItemCategory?.needReminder == true && reminderDaysStr.isNotBlank())
                                reminderDaysStr.toLongOrNull() else null

                        if (name.isNotBlank() && room.isNotBlank() &&
                            container.isNotBlank() && category.isNotBlank() && photoUri != null
                        ) {
                            val quantity =
                                if (selectedItemCategory?.needQuantity == true) quantityStr.toIntOrNull() else null
                            val newItem = Item(
                                id = existingItem?.id ?: 0,
                                name = name,
                                room = room,
                                container = container,
                                subContainer = if (selectedContainer?.hasSubContainer == true) subContainer else null,
                                thirdContainer = if (selectedSubContainer?.hasThirdContainer == true) thirdContainer else null,
                                category = category,
                                description = description,
                                photoUri = photoUri.toString(),
                                timestamp = System.currentTimeMillis(),
                                productionDate = productionDate,
                                reminderDays = reminderDays,
                                quantity = quantity,
                                expirationDate = expirationDate
                            )
                            itemViewModel.insert(newItem)
                            navController.popBackStack()
                        } else {
                            Toast.makeText(
                                context,
                                "请确保必填项已填写并选择图片",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
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
            // ========== 让内容可滚动，防止键盘遮挡或控件超出屏幕 ==========
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ===== 第一块 Card：房间 & 容器 =====
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("位置", style = MaterialTheme.typography.titleMedium)

                        // 房间 & 容器在同一行
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // 房间选择
                            ExposedDropdownMenuBox(
                                expanded = roomExpanded,
                                onExpandedChange = { roomExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = room,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("所在房间") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomExpanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor(
                                            MenuAnchorType.PrimaryNotEditable,
                                            enabled = canEditRoom
                                        )
                                        .clickable(enabled = canEditRoom) {
                                            roomExpanded = !roomExpanded
                                        }
                                )
                                ExposedDropdownMenu(
                                    expanded = roomExpanded,
                                    onDismissRequest = { roomExpanded = false }
                                ) {
                                    allRooms.forEach { r ->
                                        DropdownMenuItem(
                                            text = { Text(r.name) },
                                            onClick = {
                                                room = r.name
                                                roomExpanded = false
                                                container = ""
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "添加房间",
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            roomExpanded = false
                                            navController.navigate(Screen.AddRoom.createRoute())
                                        }
                                    )
                                }
                            }

                            val coroutineScope = rememberCoroutineScope()

                            // 容器选择
                            ExposedDropdownMenuBox(
                                expanded = containerExpanded,
                                onExpandedChange = { newExpanded ->
                                    containerExpanded =
                                        if (room.isNotBlank()) newExpanded else false
                                    if (room.isNotBlank() && newExpanded) {
                                        coroutineScope.launch {
                                            containers =
                                                containerViewModel.getContainersByRoom(room).first()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = container,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("容器名称") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = containerExpanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor(
                                            MenuAnchorType.PrimaryNotEditable,
                                            enabled = canEditContainer
                                        )
                                        .clickable(enabled = room.isNotBlank() && canEditContainer) {
                                            containerExpanded = !containerExpanded
                                        },
                                    enabled = room.isNotBlank()
                                )
                                ExposedDropdownMenu(
                                    expanded = containerExpanded,
                                    onDismissRequest = { containerExpanded = false }
                                ) {
                                    containers.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text(c.name) },
                                            onClick = {
                                                container = c.name
                                                selectedContainer = c
                                                containerExpanded = false
                                                if (!c.hasSubContainer) {
                                                    subContainer = ""
                                                }
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "添加容器",
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            containerExpanded = false
                                            navController.navigate(
                                                Screen.AddContainer.createRoute(
                                                    room
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // 如果有二级容器
                        if (selectedContainer?.hasSubContainer == true) {
                            // 声明一个 mutableState 保存当前的二级容器列表
                            var subContainerList by remember { mutableStateOf(emptyList<SubContainerEntity>()) }
                            // 获取 coroutineScope
                            val coroutineScope = rememberCoroutineScope()

                            // 当 room 或 container 发生变化时，更新 subContainerList
                            LaunchedEffect(room, container) {
                                if (room.isNotBlank() && container.isNotBlank()) {
                                    coroutineScope.launch {
                                        subContainerList = subContainerViewModel.getSubContainers(room, container)
                                            .firstOrNull() ?: emptyList()
                                    }
                                } else {
                                    subContainerList = emptyList()
                                }
                            }

                            // 用于控制下拉菜单展开状态
                            var subContainerExpanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = subContainerExpanded,
                                onExpandedChange = { subContainerExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = subContainer,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("二级容器") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = subContainerExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = canEditContainer)
                                        .clickable { subContainerExpanded = !subContainerExpanded }
                                )
                                ExposedDropdownMenu(
                                    expanded = subContainerExpanded,
                                    onDismissRequest = { subContainerExpanded = false }
                                ) {
                                    // 遍历当前获取到的二级容器列表
                                    subContainerList.forEach { sc ->
                                        DropdownMenuItem(
                                            text = { Text(sc.subContainerName) },
                                            onClick = {
                                                subContainer = sc.subContainerName
                                                selectedSubContainer = sc
                                                subContainerExpanded = false
                                            }
                                        )
                                    }
                                    // 添加新二级容器选项（点击后跳转到添加二级容器页面）
                                    DropdownMenuItem(
                                        text = { Text("添加二级容器", color = MaterialTheme.colorScheme.primary) },
                                        onClick = {
                                            subContainerExpanded = false
                                            navController.navigate(
                                                Screen.AddSubContainer.createRoute(room, container)
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        if (selectedSubContainer?.hasThirdContainer == true) {
                            var thirdContainerList by remember { mutableStateOf(emptyList<ThirdContainerEntity>()) }
                            // 获取 coroutineScope
                            val coroutineScope = rememberCoroutineScope()

                            LaunchedEffect(room, container, subContainer) {
                                if (room.isNotBlank() && container.isNotBlank() && subContainer.isNotBlank()) {
                                    coroutineScope.launch {
                                        thirdContainerList = thirdContainerViewModel.getThirdContainers(room, container, subContainer)
                                            .firstOrNull() ?: emptyList()
                                    }
                                } else {
                                    thirdContainerList = emptyList()
                                }
                            }

                            // 用于控制下拉菜单展开状态
                            var thirdContainerExpanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = thirdContainerExpanded,
                                onExpandedChange = { thirdContainerExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = thirdContainer,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("三级容器") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = thirdContainerExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = canEditContainer)
                                        .clickable { thirdContainerExpanded = !thirdContainerExpanded }
                                )
                                ExposedDropdownMenu(
                                    expanded = thirdContainerExpanded,
                                    onDismissRequest = { thirdContainerExpanded = false }
                                ) {
                                    thirdContainerList.forEach { tc ->
                                        DropdownMenuItem(
                                            text = { Text(tc.thirdContainerName) },
                                            onClick = {
                                                thirdContainer = tc.thirdContainerName
                                                selectedThirdContainer = tc
                                                thirdContainerExpanded = false
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("添加三级容器", color = MaterialTheme.colorScheme.primary) },
                                        onClick = {
                                            thirdContainerExpanded = false
                                            navController.navigate(
                                                Screen.AddThirdContainer.createRoute(room, container, subContainer)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // ===== 第二块 Card：物品信息 =====
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("物品信息", style = MaterialTheme.typography.titleMedium)

                        val coroutineScope = rememberCoroutineScope()

                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { newExpanded ->
                                if (newExpanded) {
                                    coroutineScope.launch {
                                        categoriesList = itemCategoryViewModel.allCategories.first()
                                    }
                                }
                                categoryExpanded = newExpanded
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("物品类别") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = canEditCategory)
                                    .clickable(enabled = canEditCategory) { categoryExpanded = !categoryExpanded },
                                enabled = true
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categoriesList.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.categoryName) },
                                        onClick = {
                                            category = cat.categoryName
                                            selectedItemCategory = cat
                                            reminderDaysStr = if (cat.needReminder && cat.reminderPeriodDays != null) {
                                                cat.reminderPeriodDays.toString()
                                            } else {
                                                ""
                                            }
                                            categoryExpanded = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = {
                                        Text("添加物品类别", color = MaterialTheme.colorScheme.primary)
                                    },
                                    onClick = {
                                        categoryExpanded = false
                                        navController.navigate(
                                            Screen.AddItemCategory.createRoute(room, container)
                                        )
                                    }
                                )
                            }
                        }

                        // 物品名称（单独占一行，在右侧添加拍照按钮）
                        OutlinedTextField(
                            value = name,
                            onValueChange = { if (canEditName) name = it },
                            label = { Text("物品名称") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        // 检查摄像头权限，若已授权则启动 OCR 拍照；否则请求权限
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.CAMERA
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            val uri = createImageUri(context)
                                            ocrTempPhotoUri.value = uri
                                            ocrLauncher.launch(uri)
                                        } else {
                                            ocrCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CameraAlt, // 记得引入相应图标库
                                        contentDescription = "拍照识别"
                                    )
                                }
                            }
                        )

                        // 生产日期 / 有效期 / 提前提醒 / 数量等
                        if (selectedItemCategory?.needProductionDate == true &&
                            selectedItemCategory?.needExpirationDate == true
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    DateField(
                                        label = "生产日期",
                                        dateStr = productionDateStr,
                                        onClick = {
                                            if (canEditProductionDate) productionDateDialog.show()
                                        }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    DateField(
                                        label = "有效期",
                                        dateStr = expirationDateStr,
                                        onClick = {
                                            if (canEditExpirationDate) expirationDateDialog.show()
                                        }
                                    )
                                }
                            }
                        } else if (selectedItemCategory?.needProductionDate == true) {
                            // 只有生产日期
                            DateField(
                                label = "生产日期",
                                dateStr = productionDateStr,
                                onClick = {
                                    if (canEditProductionDate) productionDateDialog.show()
                                }
                            )
                        } else if (selectedItemCategory?.needExpirationDate == true) {
                            // 只有有效期
                            DateField(
                                label = "有效期",
                                dateStr = expirationDateStr,
                                onClick = {
                                    if (canEditExpirationDate) expirationDateDialog.show()
                                }
                            )
                        }

                        if (selectedItemCategory?.needReminder == true) {
                            OutlinedTextField(
                                value = reminderDaysStr,
                                onValueChange = { if (canEditReminderDays) reminderDaysStr = it },
                                label = { Text("提醒期限（天）") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        if (selectedItemCategory?.needQuantity == true) {
                            OutlinedTextField(
                                value = quantityStr,
                                onValueChange = { if (canEditQuantity) quantityStr = it },
                                label = { Text("数量") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        // 描述
                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (canEditDescription) description = it },
                            label = { Text("描述（选填）") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // ===== 第三块 Card：图片区域 =====
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("图片", style = MaterialTheme.typography.titleMedium)

                        // 这里复用你的 ImageSelector
                        ImageSelector(
                            photoUri = photoUri,
                            isEditing = isEditing,
                            onSelectImage = { showImageSourceDialog = true },
                            onPreviewImage = { showLargeImageDialog = true },
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }

                // 为底部 FAB 留出空间
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

fun createDestinationUri(context: Context): Uri {
    val imagesDir = File(context.getExternalFilesDir(null), "camera_images")
    if (!imagesDir.exists()) {
        imagesDir.mkdirs()
    }
    val imageFile = File(imagesDir, "temp_${System.currentTimeMillis()}.jpg")
    if (!imageFile.exists()) {
        val created = imageFile.createNewFile()
        Log.d("createDestinationUri", "创建目标空文件: ${imageFile.absolutePath}, 成功: $created")
    } else {
        Log.d("createDestinationUri", "目标文件已存在: ${imageFile.absolutePath}")
    }
    Log.d("createDestinationUri", "生成的目标URI: ${Uri.fromFile(imageFile)}")
    return Uri.fromFile(imageFile)
}

@Composable
fun DateField(label: String, dateStr: String, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = dateStr,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            placeholder = { Text("点击设置$label") },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .clickable { onClick() }
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}
