package com.vauth.foxyvpn.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.data.FxaAuthRepository
import com.vauth.foxyvpn.data.SessionStatus
import com.vauth.foxyvpn.ui.theme.NeumorphicRadarPulse
import com.vauth.foxyvpn.ui.theme.currentNeumorphicColors
import com.vauth.foxyvpn.ui.theme.neumorphicRaised
import com.vauth.foxyvpn.ui.theme.staggeredEntrance
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

private const val SESSION_RESTORE_TIMEOUT_MS = 8_000L

@Composable
fun SplashScreen(
    authRepository: FxaAuthRepository,
    onSignedIn: () -> Unit,
    onNeedsLogin: () -> Unit,
) {
    val neu = currentNeumorphicColors

    LaunchedEffect(Unit) {
        delay(600)
        val status = withTimeoutOrNull(SESSION_RESTORE_TIMEOUT_MS) {
            authRepository.restoreSession()
        } ?: SessionStatus.UNREACHABLE

        when (status) {
            SessionStatus.ACTIVE -> onSignedIn()
            SessionStatus.UNREACHABLE -> onSignedIn()
            SessionStatus.NEEDS_LOGIN -> onNeedsLogin()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash-pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-scale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(neu.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            NeumorphicRadarPulse(
                color = neu.accent,
                isActive = true,
                isConnecting = true,
                size = 156.dp,
            ) {
                Box(
                    modifier = Modifier
                        .size((96 * pulseScale).dp)
                        .neumorphicRaised(
                            shape = CircleShape,
                            cornerRadius = 48.dp,
                            elevation = 8.dp,
                            lightShadowColor = neu.shadowLight.copy(alpha = if (neu.isDark) 0.3f else 0.85f),
                            darkShadowColor = neu.shadowDark.copy(alpha = if (neu.isDark) 0.7f else 0.45f),
                        )
                        .clip(CircleShape)
                        .background(Color(0xFF0A0D14)),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.vauth.foxyvpn.R.drawable.foxy_official_icon),
                        contentDescription = "FoxyVPN Official Logo",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "FoxyVpn",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp,
                ),
                maxLines = 1,
                softWrap = false,
                color = neu.textPrimary,
                modifier = Modifier.staggeredEntrance(1),
            )

            CircularProgressIndicator(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .size(24.dp),
                strokeWidth = 2.5.dp,
                color = neu.accent,
            )
        }
    }
}
