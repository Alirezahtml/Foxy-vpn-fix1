package com.vauth.foxyvpn

import android.Manifest
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.vauth.foxyvpn.data.AppLogger
import com.vauth.foxyvpn.data.model.ConnectionState
import com.vauth.foxyvpn.quicksettings.DynamicIslandOverlayService
import com.vauth.foxyvpn.ui.navigation.FoxyNavGraph
import com.vauth.foxyvpn.ui.theme.FoxyVpnTheme
import com.vauth.foxyvpn.ui.theme.rememberLanguageController
import com.vauth.foxyvpn.ui.theme.rememberThemeController
import com.vauth.foxyvpn.vpn.FoxyVpnService
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            requestNotificationPermissionIfNeeded()
            FoxyVpnService.start(this)
        } else {
            val app = application as FoxyVpnApp
            val msg = when (app.settingsStore.appLanguage) {
                com.vauth.foxyvpn.ui.theme.AppLanguage.FA ->
                    "برنامه FoxyVpn برای عبور ترافیک دستگاه به مجوز وی‌پی‌ان اندروید نیاز دارد."
                com.vauth.foxyvpn.ui.theme.AppLanguage.AR ->
                    "يحتاج تطبيق FoxyVpn إلى إذن الشبكة الافتراضية من أندرويد لتمرير حركة المرور."
                com.vauth.foxyvpn.ui.theme.AppLanguage.EN ->
                    "FoxyVpn needs Android's VPN permission to tunnel this device's traffic."
            }
            runCatching {
                Toast.makeText(
                    this,
                    msg,
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    private fun requestConnect() {
        runCatching {
            val app = application as FoxyVpnApp
            if (app.settingsStore.dynamicIslandEnabled && app.settingsStore.dynamicIslandOverlayEnabled) {
                DynamicIslandOverlayService.start(this)
            }
            if (app.settingsStore.proxyOnlyMode) {
                requestNotificationPermissionIfNeeded()
                FoxyVpnService.start(this)
                return
            }

            val prepareIntent = runCatching { VpnService.prepare(this) }.getOrNull()
            if (prepareIntent != null) {
                vpnPermissionLauncher.launch(prepareIntent)
            } else {
                requestNotificationPermissionIfNeeded()
                FoxyVpnService.start(this)
            }
        }.onFailure { err ->
            AppLogger.e("MainActivity", "requestConnect failed", err)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        runCatching {
            val alreadyGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!alreadyGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

        override fun onStart() {
        super.onStart()
        DynamicIslandOverlayService.setAppInForeground(true)
    }

    override fun onResume() {
        super.onResume()
        DynamicIslandOverlayService.setAppInForeground(true)
    }

    override fun onStop() {
        DynamicIslandOverlayService.setAppInForeground(false)
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicIslandOverlayService.setAppInForeground(true)
        runCatching { enableEdgeToEdge() }
        val app = application as FoxyVpnApp
        val isFirstLaunch = savedInstanceState == null

        setContent {
            val themeController = rememberThemeController(app.settingsStore)
            val languageController = rememberLanguageController(app.settingsStore)

            LaunchedEffect(Unit) {
                delay(650L)
                runCatching {
                    if (app.settingsStore.dynamicIslandEnabled && app.settingsStore.dynamicIslandOverlayEnabled) {
                        DynamicIslandOverlayService.start(this@MainActivity)
                    }
                    if (isFirstLaunch &&
                        app.settingsStore.autoConnectOnLaunch &&
                        FoxyVpnService.state.value == ConnectionState.DISCONNECTED &&
                        app.tokenStore.loadAuth() != null
                    ) {
                        requestConnect()
                    }
                }.onFailure { err ->
                    AppLogger.w("MainActivity", "Post-launch startup action failed: ${err.message}")
                }
            }

            FoxyVpnTheme(
                themeMode = themeController.mode,
                appLanguage = languageController.language,
                colorTheme = themeController.colorTheme,
                customAccentArgb = themeController.customAccentArgb,
                customPalette = themeController.customPalette,
                uiDesignStyle = themeController.uiDesignStyle,
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    FoxyNavGraph(
                        navController = navController,
                        app = app,
                        themeController = themeController,
                        languageController = languageController,
                        onRequestConnect = { requestConnect() },
                        onDisconnect = { FoxyVpnService.stop(this) },
                    )
                }
            }
        }
    }
}
