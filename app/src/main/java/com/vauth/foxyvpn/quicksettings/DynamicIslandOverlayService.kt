package com.vauth.foxyvpn.quicksettings

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.vauth.foxyvpn.FoxyVpnApp
import com.vauth.foxyvpn.MainActivity
import com.vauth.foxyvpn.ui.components.DynamicIsland
import com.vauth.foxyvpn.ui.theme.FoxyVpnTheme
import com.vauth.foxyvpn.vpn.FoxyVpnService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.math.roundToInt

class DynamicIslandOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var windowLayoutParams: WindowManager.LayoutParams? = null

    private var lifecycleInitialized = false

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            runCatching { !Settings.canDrawOverlays(this) }.getOrDefault(true)
        ) {
            stopSelf()
            return
        }

        val app = applicationContext as? FoxyVpnApp
        if (app == null) {
            stopSelf()
            return
        }

        runCatching {
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            lifecycleInitialized = true
        }.onFailure {
            stopSelf()
            return
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager

        val baseOffsetY = 8
        val initialX = app.settingsStore.dynamicIslandOffsetX.takeIf { it.isFinite() }?.roundToInt() ?: 0
        val initialY = (baseOffsetY + (app.settingsStore.dynamicIslandOffsetY.takeIf { it.isFinite() }?.roundToInt() ?: 0)).coerceIn(0, 2200)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = initialX
            y = initialY
        }
        windowLayoutParams = params

        val composeView = runCatching {
            ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@DynamicIslandOverlayService)
                setViewTreeSavedStateRegistryOwner(this@DynamicIslandOverlayService)
                setViewTreeViewModelStoreOwner(this@DynamicIslandOverlayService)
                setContent {
                    val revision by app.settingsStore.islandConfigRevision.collectAsState()
                    val proxy by app.proxyStateStore.selectedProxyFlow.collectAsState()
                    val inForeground by isAppInForeground.collectAsState()

                    LaunchedEffect(inForeground) {
                        overlayView?.visibility = if (inForeground) View.GONE else View.VISIBLE
                    }

                    // Sync overlay position when user resets position in settings
                    LaunchedEffect(revision) {
                        val lp = windowLayoutParams ?: return@LaunchedEffect
                        val targetX = app.settingsStore.dynamicIslandOffsetX.takeIf { it.isFinite() }?.roundToInt() ?: 0
                        val targetY = (baseOffsetY + (app.settingsStore.dynamicIslandOffsetY.takeIf { it.isFinite() }?.roundToInt() ?: 0)).coerceIn(0, 2200)
                        if (lp.x != targetX || lp.y != targetY) {
                            lp.x = targetX
                            lp.y = targetY
                            runCatching {
                                overlayView?.let { v -> windowManager?.updateViewLayout(v, lp) }
                            }
                        }
                    }

                    if (!inForeground) {
                        FoxyVpnTheme(
                            themeMode = app.settingsStore.themeMode,
                            appLanguage = app.settingsStore.appLanguage,
                            colorTheme = app.settingsStore.appColorTheme,
                            customAccentArgb = app.settingsStore.appCustomAccentArgb,
                            customPalette = app.settingsStore.appCustomPalette,
                            uiDesignStyle = app.settingsStore.uiDesignStyle,
                        ) {
                            DynamicIsland(
                                settingsStore = app.settingsStore,
                                selectedProxy = proxy,
                                onDisconnect = { FoxyVpnService.stop(this@DynamicIslandOverlayService) },
                                onRequestConnect = {
                                    runCatching {
                                        val intent = Intent(this@DynamicIslandOverlayService, MainActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        }
                                        startActivity(intent)
                                    }
                                },
                                onOpenServers = {
                                    runCatching {
                                        val intent = Intent(this@DynamicIslandOverlayService, MainActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        }
                                        startActivity(intent)
                                    }
                                },
                                isSystemOverlay = true,
                                onOverlayDragDelta = { dx, dy ->
                                    val lp = windowLayoutParams ?: return@DynamicIsland
                                    if (!dx.isFinite() || !dy.isFinite()) return@DynamicIsland
                                    lp.x += dx.roundToInt()
                                    lp.y = (lp.y + dy.roundToInt()).coerceIn(0, 2200)
                                    app.settingsStore.dynamicIslandOffsetX = lp.x.toFloat()
                                    app.settingsStore.dynamicIslandOffsetY = (lp.y - baseOffsetY).toFloat()
                                    runCatching {
                                        overlayView?.let { v -> windowManager?.updateViewLayout(v, lp) }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }.getOrElse {
            stopSelf()
            return
        }

        composeView.visibility = if (isAppInForeground.value) View.GONE else View.VISIBLE
        overlayView = composeView
        runCatching {
            windowManager?.addView(composeView, params)
        }.onFailure {
            overlayView = null
            stopSelf()
        }
    }

    override fun onDestroy() {
        if (lifecycleInitialized) {
            runCatching {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }
        }
        runCatching { store.clear() }
        runCatching { scope.cancel() }

        overlayView?.let { view ->
            runCatching { windowManager?.removeView(view) }
        }
        overlayView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private val _isAppInForeground = kotlinx.coroutines.flow.MutableStateFlow(false)
        val isAppInForeground: kotlinx.coroutines.flow.StateFlow<Boolean> = _isAppInForeground

        fun setAppInForeground(inForeground: Boolean) {
            _isAppInForeground.value = inForeground
        }

        fun start(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context)) {
                runCatching {
                    context.startService(Intent(context, DynamicIslandOverlayService::class.java))
                }
            }
        }

        fun stop(context: Context) {
            runCatching {
                context.stopService(Intent(context, DynamicIslandOverlayService::class.java))
            }
        }
    }
}
