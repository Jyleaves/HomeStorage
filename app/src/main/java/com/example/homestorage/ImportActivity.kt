package com.example.homestorage

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.homestorage.viewmodel.ItemViewModel
import com.example.homestorage.screens.importZipData
import kotlinx.coroutines.launch

class ImportActivity : ComponentActivity() {

    // 通过 viewModels() 获取同一个应用内的 ViewModel（也可自行构造）
    private val itemViewModel: ItemViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIncomingIntent(intent)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun handleIncomingIntent(intent: Intent?) {
        // 判断是否为 ACTION_SEND，且 MIME type 是 application/zip
        if (intent?.action == Intent.ACTION_SEND && intent.type == "application/zip") {
            // 共享文件通常放在 EXTRA_STREAM
            val uri = intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            if (uri != null) {
                // 在协程里调用新的 importZipData
                lifecycleScope.launch {
                    importZipData(applicationContext, uri)
                }
            }
        }
        // 最后结束该 Activity，或可以先显示一个界面再 finish
        finish()
    }
}
