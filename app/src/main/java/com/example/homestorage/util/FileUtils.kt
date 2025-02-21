package com.example.homestorage.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

fun createImageUri(context: Context): Uri {
    val imagesDir = File(context.getExternalFilesDir(null), "camera_images")
    if (!imagesDir.exists()) {
        imagesDir.mkdirs()
    }
    val imageFile = File(context.getExternalFilesDir(null), "camera_images/temp_${System.currentTimeMillis()}.jpg")
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
    return uri
}

fun deletePhotoFile(context: Context, uriString: String?) {
    uriString ?: return

    try {
        val uri = Uri.parse(uriString)
        when {
            // 处理应用私有目录文件
            uri.path?.startsWith(context.getExternalFilesDir(null)?.path ?: "") == true -> {
                File(uri.path!!).takeIf { it.exists() }?.delete()
            }
            // 处理ContentResolver管理的文件（如相册文件）
            uri.scheme == "content" -> {
                context.contentResolver.delete(uri, null, null)
            }
            // 其他URI类型
            else -> {
                Log.w("FileUtils", "Unsupported URI type: $uri")
            }
        }
    } catch (e: Exception) {
        Log.e("FileUtils", "Delete photo failed: ${e.message}")
    }
}
