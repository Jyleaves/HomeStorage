package com.example.homestorage.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.homestorage.data.ItemCategory
import com.example.homestorage.viewmodel.ItemCategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCategoryFormScreen(
    navController: NavController,
    categoryNameToEdit: String = "",
    itemCategoryViewModel: ItemCategoryViewModel = viewModel()
) {
    // 查询已有的类别
    val allCategories = itemCategoryViewModel.allCategories.collectAsState(emptyList()).value
    val existingCategory = allCategories.find { it.categoryName == categoryNameToEdit }

    // 如果能找到，就说明是“编辑模式”，否则是“添加模式”
    val isEditMode = (existingCategory != null)

    // 状态
    var categoryName by remember { mutableStateOf(existingCategory?.categoryName ?: "") }
    var needProductionDate by remember { mutableStateOf(existingCategory?.needProductionDate ?: false) }
    var needExpirationDate by remember { mutableStateOf(existingCategory?.needExpirationDate ?: false) }
    var needReminder by remember { mutableStateOf(existingCategory?.needReminder ?: false) }
    var reminderPeriodDays by remember { mutableStateOf(existingCategory?.reminderPeriodDays?.toString() ?: "") }
    var needQuantity by remember { mutableStateOf(existingCategory?.needQuantity ?: false) }

    LaunchedEffect(existingCategory) {
        if (existingCategory != null) {
            categoryName = existingCategory.categoryName
            needProductionDate = existingCategory.needProductionDate
            needExpirationDate = existingCategory.needExpirationDate
            needReminder = existingCategory.needReminder
            reminderPeriodDays = existingCategory.reminderPeriodDays?.toString() ?: ""
            needQuantity = existingCategory.needQuantity
        }
    }

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "编辑物品类别" else "添加物品类别")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
                verticalArrangement = Arrangement.SpaceBetween

            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 物品类别名称输入框
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            OutlinedTextField(
                                value = categoryName,
                                onValueChange = { categoryName = it },
                                label = { Text("物品类别名称") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        // 需要生产日期属性
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = needProductionDate,
                                onCheckedChange = { needProductionDate = it }
                            )
                            Text("需要生产日期属性")
                        }

                        // 需要有效期属性
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = needExpirationDate,
                                onCheckedChange = { newValue ->
                                    needExpirationDate = newValue
                                    if (!newValue) {
                                        needReminder = false
                                        reminderPeriodDays = ""
                                    }
                                }
                            )
                            Text("需要有效期属性")
                        }

                        if (needExpirationDate) {
                            // 快到期时发送提醒
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = needReminder,
                                    onCheckedChange = { needReminder = it }
                                )
                                Text("快到期时发送提醒")
                            }

                            if (needReminder) {
                                // 提醒天数
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = reminderPeriodDays,
                                        onValueChange = { reminderPeriodDays = it },
                                        label = { Text("默认提醒天数（天）") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }
                        }

                        // 需要数量属性
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = needQuantity,
                                onCheckedChange = { needQuantity = it }
                            )
                            Text("需要数量属性")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // 底部保存按钮
                Button(
                    onClick = {
                        val reminderDays = if (needReminder) reminderPeriodDays.toLongOrNull() else null

                        // 如果是编辑模式，就 update；否则就 insert
                        if (isEditMode && existingCategory != null) {
                            val updatedCat = existingCategory.copy(
                                categoryName = categoryName,
                                needProductionDate = needProductionDate,
                                needExpirationDate = needExpirationDate,
                                needReminder = needReminder,
                                reminderPeriodDays = reminderDays,
                                needQuantity = needQuantity
                            )
                            itemCategoryViewModel.updateCategory(existingCategory, updatedCat)
                        } else {
                            // 添加模式
                            val newCat = ItemCategory(
                                categoryName = categoryName,
                                needProductionDate = needProductionDate,
                                needExpirationDate = needExpirationDate,
                                needReminder = needReminder,
                                reminderPeriodDays = reminderDays,
                                needQuantity = needQuantity
                            )
                            itemCategoryViewModel.insert(newCat)
                        }
                        navController.popBackStack()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("保存")
                }
            }
        }
    }
}
