package com.example.homestorage.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun createImageUri(context: Context): Uri {
    val imagesDir = File(context.getExternalFilesDir(null), "camera_images")
    if (!imagesDir.exists()) {
        imagesDir.mkdirs()
    }
    val imageFile = File(context.getExternalFilesDir(null), "camera_images/temp_${System.currentTimeMillis()}.jpg")
    if (!imageFile.exists()) {
        val created = imageFile.createNewFile()
    }
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
    return uri
}
