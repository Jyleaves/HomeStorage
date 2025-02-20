package com.example.homestorage.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions

/**
 * performOcr 处理传入图片 Uri 的 OCR 识别，识别完成后通过 onResult 回调返回识别的文本。
 */
fun performOcr(context: Context, imageUri: Uri, onResult: (String) -> Unit) {
    try {
        // 使用ContentResolver打开输入流
        context.contentResolver.openInputStream(imageUri)?.use {
            val image = InputImage.fromFilePath(context, imageUri)

            val recognizer = TextRecognition.getClient(
                ChineseTextRecognizerOptions.Builder().build()
            )

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    onResult(visionText.text)
                }
                .addOnFailureListener {
                    onResult("")
                }
        } ?: run {
            onResult("")
        }
    } catch (e: Exception) {
        onResult("")
    }
}
