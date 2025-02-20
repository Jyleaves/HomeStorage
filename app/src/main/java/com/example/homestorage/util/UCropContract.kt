package com.example.homestorage.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import com.yalantis.ucrop.UCrop

/**
 * UCropContract 用于启动 uCrop 裁剪界面。
 * 输入参数为 Pair(sourceUri, destinationUri)，返回裁剪后的 Uri。
 */
class UCropContract : ActivityResultContract<Pair<Uri, Uri>, Uri?>() {
    override fun createIntent(context: Context, input: Pair<Uri, Uri>): Intent {
        val (sourceUri, destinationUri) = input

        // 授予临时URI访问权限
        context.grantUriPermission(
            "com.yalantis.ucrop",  // UCrop 的包名
            sourceUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        // 配置 UCrop 选项，启用自由裁剪
        val options = UCrop.Options().apply {
            setFreeStyleCropEnabled(true)
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
        }

        return UCrop.of(sourceUri, destinationUri)
            .withOptions(options)
            .withMaxResultSize(2160, 2160)
            .getIntent(context)
            .apply {
                // 添加 URI 权限标志
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK && intent != null) {
            UCrop.getOutput(intent)
        } else {
            null
        }
    }
}

