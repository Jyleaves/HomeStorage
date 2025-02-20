package com.example.homestorage

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.content.ContextCompat
import com.example.homestorage.navigation.AppNavigation
import com.example.homestorage.ui.theme.HomeInventoryAppTheme

class MainActivity : ComponentActivity() {
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "通知权限被拒绝，提醒功能将无法使用", Toast.LENGTH_SHORT).show()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 请求通知权限，仅适用于 Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        setContent {
            HomeInventoryAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}
