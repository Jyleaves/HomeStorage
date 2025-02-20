package com.example.homestorage.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.homestorage.data.AppDatabase
import com.example.homestorage.data.ContainerEntity
import com.example.homestorage.data.Item
import com.example.homestorage.data.ItemCategory
import com.example.homestorage.data.RoomEntity
import com.example.homestorage.data.SubContainerEntity
import com.example.homestorage.data.ThirdContainerEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * 用于封装导出数据的全部信息
 */
data class ExportData(
    val items: List<Item>,
    val rooms: List<RoomEntity>,
    val containers: List<ContainerEntity>,
    val subContainers: List<SubContainerEntity>,
    val thirdContainers: List<ThirdContainerEntity>,
    val categories: List<ItemCategory>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImportScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 创建 Zip 文件（MimeType 为 application/zip），自动生成文件名（含时间戳）
    val createFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                exportDataAndImages(context, it)
                shareZipFile(context, it)
                navController.navigate("home") {
                    popUpTo(0)
                }
            }
        }
    }

    // 打开 Zip 文件，用于导入数据
    val importFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                importZipData(context, it)
                navController.navigate("home") {
                    popUpTo(0)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入/导出数据") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    // 自动生成包含时间戳的文件名
                    val fileName = "home_inventory_data_${System.currentTimeMillis()}.zip"
                    createFileLauncher.launch(fileName)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("导出数据")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { importFileLauncher.launch(arrayOf("application/zip")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("导入数据")
            }
        }
    }
}

/**
 * 导出数据时，将数据库中的物品、房间、容器、物品类别数据转换为 JSON，
 * 同时处理物品中引用的图片（photoUri），将图片文件以二进制形式写入 Zip 文件，
 * JSON 中将 photoUri 修改为相对路径 "images/xxx"。
 */
suspend fun exportDataAndImages(context: Context, uri: Uri) {
    try {
        val db = AppDatabase.getDatabase(context)
        // 获取各个数据表最新数据（使用 first() 获取 Flow 的第一项）
        val items = db.itemDao().getAllItems().first()
        val rooms = db.roomDao().getAllRooms().first()
        val containers = db.containerDao().getAllContainers().first()
        val subContainers = db.subContainerDao().getAllSubContainers().first()
        val thirdContainers = db.thirdContainerDao().getAllThirdContainers().first()
        val categories = db.itemCategoryDao().getAllCategories().first()

        // 对物品进行处理：若有图片，则将图片路径改为相对路径，并保存图片映射
        val exportItems = mutableListOf<Item>()
        val imageMap = mutableMapOf<String, Uri>()
        items.forEachIndexed { index, item ->
            if (item.photoUri.isNotBlank()) {
                val originalPhotoUri = Uri.parse(item.photoUri)
                // 尝试从 URI 获取文件名；如果为空则用 photo_index.jpg
                val fileName = originalPhotoUri.lastPathSegment ?: "photo_$index.jpg"
                imageMap[fileName] = originalPhotoUri
                // 使用 copy() 修改 photoUri 为相对路径
                val newItem = item.copy(photoUri = "images/$fileName")
                exportItems.add(newItem)
            } else {
                exportItems.add(item)
            }
        }
        // 构造封装所有数据的对象
        val exportData = ExportData(
            items = exportItems,
            rooms = rooms,
            containers = containers,
            subContainers = subContainers,
            thirdContainers = thirdContainers,
            categories = categories
        )
        val gson = Gson()
        val json = gson.toJson(exportData)
        // 写入 Zip 文件：第一项为 JSON 文件 data.json，后续为 images 下的各个图片
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            ZipOutputStream(outputStream).use { zipOut ->
                // 写入 JSON 文件
                val jsonEntry = ZipEntry("data.json")
                zipOut.putNextEntry(jsonEntry)
                zipOut.write(json.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()
                // 写入所有图片
                imageMap.forEach { (fileName, imageUri) ->
                    // 判断图片 URI 的 scheme
                    val inputStream = when (imageUri.scheme) {
                        "content" -> context.contentResolver.openInputStream(imageUri)
                        "file" -> imageUri.path?.let { java.io.File(it).inputStream() }
                        else -> {
                            // 如果没有 scheme，尝试当作文件路径处理
                            java.io.File(imageUri.toString()).takeIf { it.exists() }?.inputStream()
                        }
                    }
                    inputStream?.use { imageInputStream ->
                        val imageBytes = imageInputStream.readBytes()
                        val imageEntry = ZipEntry("images/$fileName")
                        zipOut.putNextEntry(imageEntry)
                        zipOut.write(imageBytes)
                        zipOut.closeEntry()
                    }
                }
                zipOut.finish()
            }
        }
        Toast.makeText(context, "数据导出成功", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "导出失败: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * 导入时，从 Zip 文件中读取 "data.json" 文件和 "images" 目录下的所有图片，
 * 解析 JSON 得到 ExportData 对象，对于物品中 photoUri 为相对路径 "images/xxx" 的，
 * 将对应图片保存到缓存目录，并更新 photoUri 为文件绝对路径；同时插入房间、容器和类别数据。
 */
suspend fun importZipData(context: Context, uri: Uri) {
    try {
        val db = AppDatabase.getDatabase(context)
        val zipInputStream = ZipInputStream(context.contentResolver.openInputStream(uri))
        var jsonContent: String? = null
        // 用于保存图片的映射：文件名 -> 图片字节
        val imageBytesMap = mutableMapOf<String, ByteArray>()
        var entry: ZipEntry? = zipInputStream.nextEntry
        while (entry != null) {
            if (!entry.isDirectory) {
                when {
                    entry.name == "data.json" -> {
                        jsonContent = zipInputStream.readBytes().toString(Charsets.UTF_8)
                    }
                    entry.name.startsWith("images/") -> {
                        val fileName = entry.name.removePrefix("images/")
                        val bytes = zipInputStream.readBytes()
                        imageBytesMap[fileName] = bytes
                    }
                }
            }
            withContext(Dispatchers.IO) {
                zipInputStream.closeEntry()
            }
            entry = zipInputStream.nextEntry
        }
        withContext(Dispatchers.IO) {
            zipInputStream.close()
        }
        if (jsonContent != null) {
            val gson = Gson()
            val type = object : TypeToken<ExportData>() {}.type
            val exportData: ExportData = gson.fromJson(jsonContent, type)
            // 1. 房间
            exportData.rooms.forEach { room ->
                db.roomDao().insert(room.copy(id = 0))
            }
            // 2. 容器
            exportData.containers.forEach { container ->
                db.containerDao().insert(container.copy(id = 0))
            }
            // 3. 二级容器
            exportData.subContainers.forEach { sub ->
                db.subContainerDao().insert(sub.copy(id = 0))
            }
            // 4. 三级容器
            exportData.thirdContainers.forEach { third ->
                db.thirdContainerDao().insert(third.copy(id = 0))
            }
            // 5. 物品类别
            exportData.categories.forEach { cat ->
                db.itemCategoryDao().insert(cat.copy(id = 0))
            }
            // 6. 物品
            exportData.items.forEach { item ->
                if (item.photoUri.isNotBlank() && item.photoUri.startsWith("images/")) {
                    val fileName = item.photoUri.removePrefix("images/")
                    val imageBytes = imageBytesMap[fileName]
                    if (imageBytes != null) {
                        val file = java.io.File(context.cacheDir, fileName)
                        file.writeBytes(imageBytes)
                        // 更新 photoUri 为保存后文件的绝对路径，并重置 id
                        db.itemDao().insert(item.copy(id = 0, photoUri = file.absolutePath))
                    } else {
                        db.itemDao().insert(item.copy(id = 0))
                    }
                } else {
                    db.itemDao().insert(item.copy(id = 0))
                }
            }
            Toast.makeText(context, "数据导入成功", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "未找到 data.json", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "导入失败: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * 分享导出的 Zip 文件
 */
fun shareZipFile(context: Context, fileUri: Uri) {
    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "application/zip"
        putExtra(android.content.Intent.EXTRA_STREAM, fileUri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(
        android.content.Intent.createChooser(shareIntent, "分享 Zip 文件")
    )
}
