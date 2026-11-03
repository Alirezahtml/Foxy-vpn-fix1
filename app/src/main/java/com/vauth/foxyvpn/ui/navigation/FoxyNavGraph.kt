package com.vauth.foxyvpn.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vauth.foxyvpn.FoxyVpnApp
import com.vauth.foxyvpn.ui.screens.HomeScreen
import com.vauth.foxyvpn.ui.screens.LoginScreen
import com.vauth.foxyvpn.ui.screens.LogsScreen
import com.vauth.foxyvpn.ui.screens.NetworkToolsScreen
import com.vauth.foxyvpn.ui.screens.ServerListScreen
import com.vauth.foxyvpn.ui.screens.SettingsScreen
import com.vauth.foxyvpn.ui.screens.SplashScreen
import com.vauth.foxyvpn.ui.theme.LanguageController
import com.vauth.foxyvpn.ui.theme.ThemeController

object FoxyRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
    const val SERVERS = "servers"
    const val SETTINGS = "settings"
    const val LOGS = "logs"
    const val TOOLS_PATTERN = "tools/{initialTab}"
    fun tools(initialTab: Int = 0): String = "tools/$initialTab"
}

@Composable
fun FoxyNavGraph(
    navController: NavHostController = rememberNavController(),
    app: FoxyVpnApp,
    themeController: ThemeController,
    languageController: LanguageController,
    onRequestConnect: () -> Unit,
    onDisconnect: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = FoxyRoutes.SPLASH,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(220, easing = FastOutSlowInEasing),
            ) + fadeIn(tween(180)) + scaleIn(initialScale = 0.985f, animationSpec = tween(210))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(200, easing = FastOutSlowInEasing),
            ) + fadeOut(tween(160)) + scaleOut(targetScale = 0.985f, animationSpec = tween(200))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(210, easing = FastOutSlowInEasing),
            ) + fadeIn(tween(170)) + scaleIn(initialScale = 0.985f, animationSpec = tween(200))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(190, easing = FastOutSlowInEasing),
            ) + fadeOut(tween(150)) + scaleOut(targetScale = 0.985f, animationSpec = tween(190))
        },
    ) {
        composable(FoxyRoutes.SPLASH) {
            SplashScreen(
                authRepository = app.authRepository,
                onSignedIn = {
                    navController.navigate(FoxyRoutes.HOME) { popUpTo(FoxyRoutes.SPLASH) { inclusive = true } }
                },
                onNeedsLogin = {
                    navController.navigate(FoxyRoutes.LOGIN) { popUpTo(FoxyRoutes.SPLASH) { inclusive = true } }
                },
            )
        }
        composable(FoxyRoutes.LOGIN) {
            LoginScreen(
                authRepository = app.authRepository,
                onSignedIn = {
                    navController.navigate(FoxyRoutes.HOME) { popUpTo(FoxyRoutes.LOGIN) { inclusive = true } }
                },
            )
        }
        composable(FoxyRoutes.HOME) {
            HomeScreen(
                app = app,
                themeController = themeController,
                languageController = languageController,
                onRequestConnect = onRequestConnect,
                onDisconnect = onDisconnect,
                onOpenServers = { navController.navigate(FoxyRoutes.SERVERS) },
                onOpenSettings = { navController.navigate(FoxyRoutes.SETTINGS) },
                onOpenTools = { tab -> navController.navigate(FoxyRoutes.tools(tab)) },
            )
        }
        composable(FoxyRoutes.SERVERS) {
            ServerListScreen(
                serverListClient = app.serverListClient,
                proxyStateStore = app.proxyStateStore,
                onServerSelected = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(FoxyRoutes.SETTINGS) {
            SettingsScreen(
                settingsStore = app.settingsStore,
                themeController = themeController,
                languageController = languageController,
                onOpenLogs = { navController.navigate(FoxyRoutes.LOGS) },
                onOpenTools = { tab -> navController.navigate(FoxyRoutes.tools(tab)) },
                onSignOut = {
                    onDisconnect()
                    app.tokenStore.clear()
                    app.settingsStore.clearAccountSession()
                    app.proxyStateStore.clear()
                    navController.navigate(FoxyRoutes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = FoxyRoutes.TOOLS_PATTERN,
            arguments = listOf(navArgument("initialTab") { type = NavType.IntType; defaultValue = 0 }),
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt("initialTab") ?: 0
            NetworkToolsScreen(
                app = app,
                initialTab = initialTab,
                onRequestConnect = {
                    navController.popBackStack()
                    onRequestConnect()
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(FoxyRoutes.LOGS) {
            LogsScreen(onBack = { navController.popBackStack() })
        }
    }
}
