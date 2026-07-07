package com.example

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.QuranAppUi
import com.example.ui.QuranViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    // Handle permission outcome if needed
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Request notification permission on Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
          this,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }

    // Ensure the app can actually show the full-screen alarm over the lock screen.
    ensureAlarmDisplayPermissions()

    setContent {
      MyApplicationTheme {
        val viewModel: QuranViewModel = viewModel()
        QuranAppUi(viewModel = viewModel)
      }
    }
  }

  /**
   * Two real-world causes make an alarm screen appear "under" the lock screen
   * instead of on top of it:
   *  1) On Android 14+ the user must explicitly grant "Full screen notifications"
   *     access, otherwise the system silently downgrades the alarm to a normal
   *     heads-up notification.
   *  2) Aggressive battery optimization kills/limits the app in the background,
   *     so the alarm service never gets to run in time.
   * This gently routes the user to the right system settings screen, once,
   * only if the permission is actually missing.
   */
  private fun ensureAlarmDisplayPermissions() {
    try {
      if (Build.VERSION.SDK_INT >= 34) { // Android 14 (UPSIDE_DOWN_CAKE)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.canUseFullScreenIntent()) {
          val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
            data = Uri.parse("package:$packageName")
          }
          startActivity(intent)
          return // Ask for one permission at a time to avoid overwhelming the user
        }
      }

      val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
      if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
          data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
