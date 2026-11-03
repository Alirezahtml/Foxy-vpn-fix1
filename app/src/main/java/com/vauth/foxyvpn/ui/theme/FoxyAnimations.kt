package com.vauth.foxyvpn.ui.theme

import android.graphics.BlurMaskFilter
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vauth.foxyvpn.data.DynamicIslandHaloColorMode
import com.vauth.foxyvpn.data.DynamicIslandHaloRotationType
import com.vauth.foxyvpn.data.DynamicIslandLedStyle
import com.vauth.foxyvpn.data.DynamicIslandMotionStyle
import com.vauth.foxyvpn.data.HeroWaveAnimationStyle
import com.vauth.foxyvpn.data.WaveColorMode
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.runtime.mutableFloatStateOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 1. Staggered Entrance Modifier (Fade + Slide Up + Subtle Scale)
 */
fun Modifier.staggeredEntrance(
    index: Int,
    delayPerItemMs: Int = 55,
    durationMs: Int = 420,
): Modifier = composed {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((index.coerceAtLeast(0) * delayPerItemMs).toLong())
        anim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        )
    }
    this.graphicsLayer {
        alpha = anim.value
        translationY = (1f - anim.value) * 32f
        val s = 0.96f + (0.04f * anim.value)
        scaleX = s
        scaleY = s
    }
}

/**
 * 2. Tactile Neumorphic Press-Bounce Modifier
 */
fun Modifier.neumorphicTouchBounce(
    pressedScale: Float = 0.965f,
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "touch-bounce-scale",
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
        }
}

/**
 * 3. Shake-on-Error Horizontal Keyframe Modifier
 */
fun Modifier.shakeOnTrigger(triggerKey: Any?): Modifier = composed {
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(triggerKey) {
        if (triggerKey != null) {
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 460
                    0f at 0
                    (-18f) at 60
                    18f at 120
                    (-14f) at 180
                    14f at 240
                    (-8f) at 310
                    8f at 380
                    0f at 460
                },
            )
        }
    }
    this.graphicsLayer {
        translationX = offsetX.value
    }
}

/**
 * 4. Floating / Levitating Idle Animation Modifier
 */
fun Modifier.subtleFloatAnimation(
    amplitudePx: Float = 8f,
    durationMs: Int = 2200,
): Modifier = composed {
    val infinite = rememberInfiniteTransition(label = "float-idle")
    val dy by infinite.animateFloat(
        initialValue = -amplitudePx,
        targetValue = amplitudePx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "float-dy",
    )
    this.graphicsLayer {
        translationY = dy
    }
}

/**
 * 4b. Luminous Halo & Rotating Border Modifier
 * Supports Single-Color & Multi-Color modes, 5 rotation animation styles, adjustable speed,
 * and outward Dynamic Shadow Distance ("دوری داینامیک سایه‌ای").
 */
fun Modifier.automotiveLedBorder(
    enabled: Boolean = true,
    colorMode: DynamicIslandHaloColorMode = DynamicIslandHaloColorMode.MULTI_COLOR,
    style: DynamicIslandLedStyle = DynamicIslandLedStyle.CYBER_RAINBOW,
    customColorHex: Long = 0L,
    rotationType: DynamicIslandHaloRotationType = DynamicIslandHaloRotationType.CLOCKWISE_SWEEP,
    speedMultiplier: Float = 1.0f,
    shadowDistanceDp: Float = 12f,
    cornerRadius: Dp = 28.dp,
    borderWidth: Dp = 2.0.dp,
    glowRadius: Dp = 8.dp,
): Modifier = composed {
    if (!enabled) return@composed this

    // Keep the luminous sweep calm enough to read as a soft glow rather than a strobe.
    // The user-facing speed slider still works, but its fastest value is intentionally capped.
    val clampedSpeed = speedMultiplier.coerceIn(0.35f, 2.0f)
    val rotationDurationMs = (3600f / clampedSpeed).toInt().coerceIn(1400, 9000)

    val infinite = rememberInfiniteTransition(label = "foxy-luminous-halo")
    val linearPhase by if (rotationType != DynamicIslandHaloRotationType.BREATHING_PENDULUM) {
        infinite.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = rotationDurationMs, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "halo-angle",
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }
    val pendulumPhase by if (rotationType == DynamicIslandHaloRotationType.BREATHING_PENDULUM) {
        infinite.animateFloat(
            initialValue = -110f,
            targetValue = 110f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (2400f / clampedSpeed).toInt().coerceIn(1400, 6000),
                    easing = FastOutSlowInEasing,
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "halo-pendulum",
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }
    val pulseGlow by infinite.animateFloat(
        initialValue = 0.58f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (2300f / clampedSpeed).toInt().coerceIn(1400, 6000),
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "halo-pulse-glow",
    )

    val effectiveAngleDeg = when (rotationType) {
        DynamicIslandHaloRotationType.CLOCKWISE_SWEEP -> linearPhase
        DynamicIslandHaloRotationType.COUNTER_CLOCKWISE_SWEEP -> 360f - linearPhase
        DynamicIslandHaloRotationType.DUAL_COMET_ORBIT -> linearPhase
        DynamicIslandHaloRotationType.BREATHING_PENDULUM -> pendulumPhase
        DynamicIslandHaloRotationType.PULSE_AURA_RING -> linearPhase * 0.35f
    }

    val palette = remember(colorMode, style, customColorHex, rotationType) {
        val baseColors = if (colorMode == DynamicIslandHaloColorMode.SINGLE_COLOR) {
            val single = if (customColorHex != 0L) Color(customColorHex) else Color(0xFF1E88FF)
            when (rotationType) {
                DynamicIslandHaloRotationType.DUAL_COMET_ORBIT -> listOf(
                    single,
                    Color.White.copy(alpha = 0.95f),
                    single.copy(alpha = 0.12f),
                    Color.Transparent,
                    single,
                    Color.White.copy(alpha = 0.95f),
                    single.copy(alpha = 0.12f),
                    Color.Transparent,
                    single,
                )
                DynamicIslandHaloRotationType.PULSE_AURA_RING -> listOf(
                    single,
                    single.copy(alpha = 0.88f),
                    Color.White.copy(alpha = 0.75f),
                    single.copy(alpha = 0.88f),
                    single,
                )
                else -> listOf(
                    single,
                    Color.White.copy(alpha = 0.92f),
                    single.copy(alpha = 0.85f),
                    single.copy(alpha = 0.20f),
                    single.copy(alpha = 0.85f),
                    single,
                )
            }
        } else {
            val multiBase = when (style) {
                DynamicIslandLedStyle.CYBER_RAINBOW -> listOf(
                    // Electric blue -> cyan -> indigo -> violet -> soft blue
                    Color(0xFF1A73E8),
                    Color(0xFF00D2FF),
                    Color(0xFF4F46E5),
                    Color(0xFFA855F7),
                    Color(0xFFEC4899),
                    Color(0xFF38BDF8),
                    Color(0xFF1A73E8),
                )
                DynamicIslandLedStyle.FOXY_FIRE -> listOf(
                    // Blue -> purple -> rose -> cyan
                    Color(0xFF4285F4),
                    Color(0xFF9B72CB),
                    Color(0xFFD96570),
                    Color(0xFFF49C46),
                    Color(0xFF00E5FF),
                    Color(0xFF4285F4),
                )
                DynamicIslandLedStyle.EMERALD_PULSE -> listOf(
                    // Cyber Emerald: Blue -> Cyan -> Emerald Green
                    Color(0xFF1E88FF),
                    Color(0xFF00E5FF),
                    Color(0xFF10B981),
                    Color(0xFF34D399),
                    Color(0xFF00B4FF),
                    Color(0xFF1E88FF),
                )
                DynamicIslandLedStyle.POLICE_STROBE -> listOf(
                    Color(0xFF2979FF),
                    Color(0xFF00E5FF),
                    Color(0xFFFFFFFF),
                    Color(0xFFF43F5E),
                    Color(0xFF2979FF),
                )
                DynamicIslandLedStyle.SOLAR_GOLD -> listOf(
                    Color(0xFFF59E0B),
                    Color(0xFFF97316),
                    Color(0xFFEC4899),
                    Color(0xFF8B5CF6),
                    Color(0xFFFBBF24),
                    Color(0xFFF59E0B),
                )
                DynamicIslandLedStyle.AURORA_BOREALIS -> listOf(
                    // Full 360° Prism Rainbow Spectrum
                    Color(0xFFFF0055),
                    Color(0xFFFFAA00),
                    Color(0xFF00E676),
                    Color(0xFF00E5FF),
                    Color(0xFF2979FF),
                    Color(0xFFD500F9),
                    Color(0xFFFF0055),
                )
                DynamicIslandLedStyle.OCEAN_BIOLUME -> listOf(
                    Color(0xFF00E5FF),
                    Color(0xFF0284C7),
                    Color(0xFF38BDF8),
                    Color(0xFF67E8F9),
                    Color(0xFF1D4ED8),
                    Color(0xFF00E5FF),
                )
                DynamicIslandLedStyle.ROYAL_NEBULA -> listOf(
                    Color(0xFFC084FC),
                    Color(0xFF7C3AED),
                    Color(0xFFEC4899),
                    Color(0xFF6366F1),
                    Color(0xFFF472B6),
                    Color(0xFFC084FC),
                )
                DynamicIslandLedStyle.CYBERPUNK_SYNTH -> listOf(
                    Color(0xFFFF007F),
                    Color(0xFF00F0FF),
                    Color(0xFFFCEE09),
                    Color(0xFF7928CA),
                    Color(0xFFFF007F),
                )
                DynamicIslandLedStyle.LIQUID_MERCURY -> listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFF94A3B8),
                    Color(0xFFE2E8F0),
                    Color(0xFF38BDF8),
                    Color(0xFFCBD5E1),
                    Color(0xFFFFFFFF),
                )
                DynamicIslandLedStyle.ARCTIC_FROST -> listOf(
                    Color(0xFFE0F2FE),
                    Color(0xFF7DD3FC),
                    Color(0xFF38BDF8),
                    Color(0xFFBAE6FD),
                    Color(0xFF0284C7),
                    Color(0xFFE0F2FE),
                )
                DynamicIslandLedStyle.SUNSET_MIAMI -> listOf(
                    Color(0xFFFF5E62),
                    Color(0xFFFF9966),
                    Color(0xFFF43F5E),
                    Color(0xFFA855F7),
                    Color(0xFFFB7185),
                    Color(0xFFFF5E62),
                )
                DynamicIslandLedStyle.QUANTUM_PLASMA -> listOf(
                    Color(0xFF22D3EE),
                    Color(0xFF818CF8),
                    Color(0xFFF43F5E),
                    Color(0xFF10B981),
                    Color(0xFF22D3EE),
                )
                DynamicIslandLedStyle.SAKURA_BLOSSOM -> listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFBAE6FD),
                    Color(0xFFE0E7FF),
                    Color(0xFFFFFFFF),
                )
            }
            if (rotationType == DynamicIslandHaloRotationType.DUAL_COMET_ORBIT) {
                val c1 = multiBase.first()
                val c2 = multiBase.getOrElse(multiBase.size / 2) { c1 }
                listOf(
                    c1,
                    Color.White,
                    c1.copy(alpha = 0.10f),
                    Color.Transparent,
                    c2,
                    Color.White,
                    c2.copy(alpha = 0.10f),
                    Color.Transparent,
                    c1,
                )
            } else {
                multiBase
            }
        }
        baseColors
    }

    this.drawWithContent {
        val w = size.width
        val h = size.height
        if (!w.isFinite() || !h.isFinite()) {
            drawContent()
            return@drawWithContent
        }
        val strokePx = borderWidth.toPx().coerceAtLeast(1.2f)
        if (w <= strokePx * 2.5f || h <= strokePx * 2.5f) {
            drawContent()
            return@drawWithContent
        }

        val cornerPx = cornerRadius.toPx().coerceIn(0f, minOf(w, h) / 2f)
        val center = Offset(w / 2f, h / 2f)
        val maxShadowDistPx = shadowDistanceDp.coerceIn(2f, 28f).dp.toPx()

        val ledBrush = runCatching {
            val shader = SweepGradientShader(
                center = center,
                colors = palette,
                colorStops = null,
            )
            val matrix = android.graphics.Matrix().apply {
                setRotate(effectiveAngleDeg, center.x, center.y)
            }
            shader.setLocalMatrix(matrix)
            ShaderBrush(shader)
        }.getOrElse {
            Brush.sweepGradient(
                colors = palette,
                center = center,
            )
        }

        // 1. Outward luminous shadow halo, drawn before content so it radiates outside the island.
        val pulseScale = when (rotationType) {
            DynamicIslandHaloRotationType.PULSE_AURA_RING -> 0.65f + 0.50f * pulseGlow
            DynamicIslandHaloRotationType.BREATHING_PENDULUM -> 0.80f + 0.30f * pulseGlow
            else -> 0.88f + 0.16f * pulseGlow
        }
        val activeSpreadPx = (maxShadowDistPx * pulseScale).coerceAtLeast(2.dp.toPx())
        // More low-alpha layers remove visible rings while remaining cheaper than a blurred bitmap.
        val haloLayers = 6
        for (layer in haloLayers downTo 1) {
            val fraction = layer.toFloat() / haloLayers.toFloat()
            val expandPx = activeSpreadPx * fraction
            val layerStroke = strokePx + expandPx * 1.15f
            val falloff = (1f - fraction * 0.82f).coerceIn(0.06f, 1f)
            val layerAlpha = (0.16f * falloff * falloff * (0.82f + 0.18f * pulseGlow)).coerceIn(0.018f, 0.22f)
            drawRoundRect(
                brush = ledBrush,
                topLeft = Offset(-expandPx * 0.45f, -expandPx * 0.45f),
                size = Size(w + expandPx * 0.90f, h + expandPx * 0.90f),
                cornerRadius = CornerRadius(cornerPx + expandPx * 0.45f, cornerPx + expandPx * 0.45f),
                alpha = layerAlpha,
                style = Stroke(width = layerStroke, cap = StrokeCap.Round),
            )
        }

        // Draw the Dynamic Island capsule content itself
        drawContent()

        // 2. Crisp luminous perimeter rim
        val rectTopLeft = Offset(strokePx / 2f, strokePx / 2f)
        val rectSize = Size((w - strokePx).coerceAtLeast(1f), (h - strokePx).coerceAtLeast(1f))
        val cr = CornerRadius(cornerPx, cornerPx)

        // Inner soft bloom
        drawRoundRect(
            brush = ledBrush,
            topLeft = rectTopLeft,
            size = rectSize,
            cornerRadius = cr,
            alpha = (0.28f * pulseGlow).coerceIn(0.14f, 0.40f),
            style = Stroke(width = strokePx * 1.8f, cap = StrokeCap.Round),
        )
        // High-precision luminous core rim
        drawRoundRect(
            brush = ledBrush,
            topLeft = rectTopLeft,
            size = rectSize,
            cornerRadius = cr,
            alpha = 0.86f,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
    }
}

/**
 * 4c. Ultra-Smooth 12-Mode Dynamic Island Motion & Shader Physics Modifier
 * Combines 60/120Hz frame-synchronized motion physics + surface shaders so the Dynamic Island
 * feels alive, fluid, and responsive.
 */
fun Modifier.dynamicIslandMotionEffect(
    enabled: Boolean = true,
    style: DynamicIslandMotionStyle = DynamicIslandMotionStyle.LIQUID_SPRING,
    isConnected: Boolean = false,
    isExpanded: Boolean = false,
    trafficRateBytesPerSec: Long = 0L,
    accentColor: Color = Color(0xFF00B4FF),
    cornerRadius: Dp = 28.dp,
): Modifier = composed {
    if (!enabled) return@composed this

    val infinite = rememberInfiniteTransition(label = "island-motion-physics")
    val phaseRad by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isConnected) 2600 else 3800,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "island-phase-rad",
    )
    val shimmerProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "island-shimmer-prog",
    )

    val trafficFactor = (trafficRateBytesPerSec.toFloat() / (512f * 1024f)).coerceIn(0f, 1f)

    this
        .graphicsLayer {
            when (style) {
                DynamicIslandMotionStyle.LIQUID_SPRING -> {
                    val s = if (isExpanded) 1f else 1f + 0.012f * sin(phaseRad)
                    scaleX = s
                    scaleY = s
                    translationY = if (isExpanded) 0f else sin(phaseRad) * 2.0f
                }
                DynamicIslandMotionStyle.ZERO_GRAVITY -> {
                    translationY = if (isExpanded) 0f else sin(phaseRad) * 4.5f
                    translationX = if (isExpanded) 0f else cos(phaseRad) * 2.2f
                    rotationZ = if (isExpanded) 0f else sin(phaseRad) * 0.65f
                }
                DynamicIslandMotionStyle.BREATHING_HALO -> {
                    val breath = 1f + 0.018f * ((sin(phaseRad) + 1f) * 0.5f)
                    scaleX = if (isExpanded) 1f else breath
                    scaleY = if (isExpanded) 1f else breath
                }
                DynamicIslandMotionStyle.SONIC_WAVE_SYNC -> {
                    val dynamicAmp = 0.008f + trafficFactor * 0.030f
                    val s = if (isExpanded) 1f else 1f + sin(phaseRad * 2f) * dynamicAmp
                    scaleX = s
                    scaleY = s
                }
                DynamicIslandMotionStyle.HEARTBEAT_PULSE -> {
                    val t = (phaseRad / (2f * PI.toFloat())).coerceIn(0f, 1f)
                    val beat = when {
                        t < 0.14f -> sin((t / 0.14f) * PI.toFloat()) * 0.024f
                        t in 0.20f..0.34f -> sin(((t - 0.20f) / 0.14f) * PI.toFloat()) * 0.015f
                        else -> 0f
                    }
                    val s = if (isExpanded) 1f else 1f + beat
                    scaleX = s
                    scaleY = s
                }
                DynamicIslandMotionStyle.PRISM_SHIMMER -> {
                    translationY = if (isExpanded) 0f else sin(phaseRad) * 1.6f
                }
                DynamicIslandMotionStyle.MAGNETIC_ELASTIC -> {
                    val stretchX = if (isExpanded) 1f else 1f + sin(phaseRad) * 0.015f
                    val stretchY = if (isExpanded) 1f else 1f - sin(phaseRad) * 0.011f
                    scaleX = stretchX
                    scaleY = stretchY
                }
                DynamicIslandMotionStyle.STARLIGHT_ORBIT -> {
                    translationY = if (isExpanded) 0f else sin(phaseRad) * 2.0f
                }
                DynamicIslandMotionStyle.CYBER_GLITCH_PULSE -> {
                    val microPulse = sin(phaseRad * 3f)
                    val s = if (isExpanded) 1f else 1f + (if (microPulse > 0.85f) 0.016f else 0.004f * microPulse)
                    scaleX = s
                    scaleY = s
                    translationX = if (isExpanded) 0f else (if (microPulse > 0.92f) 2.2f else 0f)
                }
                DynamicIslandMotionStyle.AURORA_WAVE_FLOW -> {
                    translationY = if (isExpanded) 0f else sin(phaseRad) * 2.6f
                    val s = if (isExpanded) 1f else 1f + 0.008f * cos(phaseRad)
                    scaleX = s
                    scaleY = s
                }
                DynamicIslandMotionStyle.GRAVITY_PENDULUM -> {
                    rotationZ = if (isExpanded) 0f else sin(phaseRad) * 1.35f
                    translationX = if (isExpanded) 0f else sin(phaseRad) * 3.4f
                    translationY = if (isExpanded) 0f else abs(cos(phaseRad)) * 1.8f
                }
                DynamicIslandMotionStyle.QUANTUM_PARTICLES -> {
                    val s = if (isExpanded) 1f else 1f + 0.010f * sin(phaseRad * 2f)
                    scaleX = s
                    scaleY = s
                }
                DynamicIslandMotionStyle.CRYO_FROST_GLIDE -> {
                    translationX = if (isExpanded) 0f else sin(phaseRad * 0.85f) * 3.0f
                    translationY = if (isExpanded) 0f else cos(phaseRad * 1.15f) * 1.6f
                    val s = if (isExpanded) 1f else 1f + 0.011f * sin(phaseRad)
                    scaleX = s
                    scaleY = s
                }
                DynamicIslandMotionStyle.ELECTRIC_COBALT_FLUX -> {
                    val flux = (sin(phaseRad * 2.5f) + cos(phaseRad * 1.5f)) * 0.5f
                    val s = if (isExpanded) 1f else 1f + 0.014f * flux
                    scaleX = s
                    scaleY = s
                    rotationZ = if (isExpanded) 0f else sin(phaseRad * 1.5f) * 0.48f
                }
                DynamicIslandMotionStyle.HYPER_WARP_DRIVE -> {
                    val warp = sin(phaseRad * 3.0f)
                    scaleX = if (isExpanded) 1f else 1f + 0.022f * warp
                    scaleY = if (isExpanded) 1f else 1f - 0.008f * warp
                    translationY = if (isExpanded) 0f else cos(phaseRad * 2f) * 1.8f
                }
                DynamicIslandMotionStyle.LIQUID_NEON_RIPPLE -> {
                    val ripple = sin(phaseRad * 2.0f)
                    scaleX = if (isExpanded) 1f else 1f + 0.015f * ripple
                    scaleY = if (isExpanded) 1f else 1f + 0.015f * cos(phaseRad * 2.0f)
                    translationY = if (isExpanded) 0f else sin(phaseRad) * 2.4f
                }
                DynamicIslandMotionStyle.CYBER_TURBINE_SPIN -> {
                    rotationZ = if (isExpanded) 0f else sin(phaseRad * 2.2f) * 0.85f
                    val s = if (isExpanded) 1f else 1f + 0.014f * cos(phaseRad * 2.2f)
                    scaleX = s
                    scaleY = s
                }
                DynamicIslandMotionStyle.ASTRAL_NOVA_PULSE -> {
                    val nova = (sin(phaseRad * 2.4f) + 1f) * 0.5f
                    val s = if (isExpanded) 1f else 0.992f + 0.024f * nova
                    scaleX = s
                    scaleY = s
                }
            }
        }
        .drawWithContent {
            drawContent()
            val w = size.width
            val h = size.height
            if (w <= 8f || h <= 8f) return@drawWithContent
            val rPx = cornerRadius.toPx().coerceAtLeast(0f)

            when (style) {
                DynamicIslandMotionStyle.PRISM_SHIMMER,
                DynamicIslandMotionStyle.LIQUID_SPRING -> {
                    val sweepX = (-0.35f + shimmerProgress * 1.7f) * w
                    val shimmerAlpha = if (style == DynamicIslandMotionStyle.PRISM_SHIMMER) 0.18f else 0.08f
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = shimmerAlpha),
                                accentColor.copy(alpha = shimmerAlpha * 0.65f),
                                Color.Transparent,
                            ),
                            start = Offset(sweepX - w * 0.25f, 0f),
                            end = Offset((sweepX + w * 0.25f).coerceAtLeast(sweepX - w * 0.25f + 1f), h),
                        ),
                        cornerRadius = CornerRadius(rPx, rPx),
                    )
                }
                DynamicIslandMotionStyle.AURORA_WAVE_FLOW -> {
                    val shift = sin(phaseRad) * w * 0.3f
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF00F5D4).copy(alpha = 0.12f),
                                accentColor.copy(alpha = 0.14f),
                                Color(0xFFA855F7).copy(alpha = 0.12f),
                            ),
                            start = Offset(shift, 0f),
                            end = Offset(w + shift, h),
                        ),
                        cornerRadius = CornerRadius(rPx, rPx),
                    )
                }
                DynamicIslandMotionStyle.CYBER_GLITCH_PULSE -> {
                    val scanY = (shimmerProgress * h).coerceIn(0f, h)
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                accentColor.copy(alpha = 0.16f),
                                Color.Transparent,
                            ),
                            startY = (scanY - 10.dp.toPx()).coerceAtLeast(0f),
                            endY = (scanY + 10.dp.toPx()).coerceAtMost(h),
                        ),
                        cornerRadius = CornerRadius(rPx, rPx),
                    )
                }
                DynamicIslandMotionStyle.BREATHING_HALO,
                DynamicIslandMotionStyle.SONIC_WAVE_SYNC -> {
                    val inset = 1.5.dp.toPx()
                    if (w > inset * 2f && h > inset * 2f) {
                        val haloAlpha = (0.14f + 0.12f * sin(phaseRad) + trafficFactor * 0.14f).coerceIn(0.05f, 0.36f)
                        drawRoundRect(
                            color = accentColor.copy(alpha = haloAlpha),
                            topLeft = Offset(inset, inset),
                            size = Size(w - inset * 2f, h - inset * 2f),
                            cornerRadius = CornerRadius(rPx, rPx),
                            style = Stroke(width = 1.6.dp.toPx()),
                        )
                    }
                }
                DynamicIslandMotionStyle.STARLIGHT_ORBIT,
                DynamicIslandMotionStyle.QUANTUM_PARTICLES -> {
                    val cx = w / 2f
                    val cy = h / 2f
                    val rx = ((w / 2f) - 4.dp.toPx()).coerceAtLeast(1f)
                    val ry = ((h / 2f) - 4.dp.toPx()).coerceAtLeast(1f)
                    val count = if (style == DynamicIslandMotionStyle.QUANTUM_PARTICLES) 6 else 4
                    for (idx in 0 until count) {
                        val a = phaseRad * (if (idx % 2 == 0) 1f else -1.2f) + idx * (2f * PI.toFloat() / count)
                        val px = cx + rx * cos(a) * (if (style == DynamicIslandMotionStyle.QUANTUM_PARTICLES) 0.92f else 1f)
                        val py = cy + ry * sin(a) * (if (style == DynamicIslandMotionStyle.QUANTUM_PARTICLES) 0.85f else 1f)
                        drawCircle(
                            color = accentColor.copy(alpha = 0.48f),
                            radius = 4.0.dp.toPx(),
                            center = Offset(px, py),
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.90f),
                            radius = 1.7.dp.toPx(),
                            center = Offset(px, py),
                        )
                    }
                }
                DynamicIslandMotionStyle.CRYO_FROST_GLIDE -> {
                    val frostSweep = (-0.25f + shimmerProgress * 1.5f) * w
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF7DD3FC).copy(alpha = 0.22f),
                                Color.White.copy(alpha = 0.24f),
                                Color(0xFF00B4FF).copy(alpha = 0.18f),
                                Color.Transparent,
                            ),
                            start = Offset(frostSweep - w * 0.2f, 0f),
                            end = Offset((frostSweep + w * 0.2f).coerceAtLeast(frostSweep - w * 0.2f + 1f), h),
                        ),
                        cornerRadius = CornerRadius(rPx, rPx),
                    )
                }
                DynamicIslandMotionStyle.ELECTRIC_COBALT_FLUX -> {
                    val inset = 1.2.dp.toPx()
                    if (w > inset * 2f && h > inset * 2f) {
                        val pulseAlpha = (0.20f + 0.16f * sin(phaseRad * 2f)).coerceIn(0.08f, 0.42f)
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0066FF).copy(alpha = pulseAlpha),
                                    Color(0xFF00F0FF).copy(alpha = pulseAlpha),
                                    Color.White.copy(alpha = pulseAlpha * 0.85f),
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(w, h),
                            ),
                            topLeft = Offset(inset, inset),
                            size = Size(w - inset * 2f, h - inset * 2f),
                            cornerRadius = CornerRadius(rPx, rPx),
                            style = Stroke(width = 1.8.dp.toPx()),
                        )
                    }
                }
                DynamicIslandMotionStyle.HYPER_WARP_DRIVE -> {
                    val cx = w / 2f
                    val cy = h / 2f
                    for (i in 0 until 6) {
                        val p = (shimmerProgress + i / 6f) % 1f
                        val angle = i * (PI.toFloat() / 3f) + phaseRad * 0.5f
                        val rStart = minOf(w, h) * 0.12f * p
                        val rEnd = minOf(w, h) * 0.48f * p
                        drawLine(
                            color = accentColor.copy(alpha = (1f - p) * 0.36f),
                            start = Offset(cx + cos(angle) * rStart, cy + sin(angle) * rStart),
                            end = Offset(cx + cos(angle) * rEnd, cy + sin(angle) * rEnd),
                            strokeWidth = 1.6.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }
                DynamicIslandMotionStyle.LIQUID_NEON_RIPPLE -> {
                    for (rIdx in 0..1) {
                        val p = (shimmerProgress + rIdx * 0.5f) % 1f
                        val inset = (2.dp.toPx() + p * 8.dp.toPx()).coerceAtMost(minOf(w, h) * 0.35f)
                        if (w > inset * 2f && h > inset * 2f) {
                            drawRoundRect(
                                color = accentColor.copy(alpha = (1f - p) * 0.32f),
                                topLeft = Offset(inset, inset),
                                size = Size(w - inset * 2f, h - inset * 2f),
                                cornerRadius = CornerRadius(rPx, rPx),
                                style = Stroke(width = 1.4.dp.toPx()),
                            )
                        }
                    }
                }
                DynamicIslandMotionStyle.CYBER_TURBINE_SPIN -> {
                    val sweepX = (sin(phaseRad) * 0.5f + 0.5f) * w
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.22f),
                                Color(0xFF00F5D4).copy(alpha = 0.10f),
                                Color.Transparent,
                            ),
                            center = Offset(sweepX, h * 0.5f),
                            radius = maxOf(w, h) * 0.55f,
                        ),
                        cornerRadius = CornerRadius(rPx, rPx),
                    )
                }
                DynamicIslandMotionStyle.ASTRAL_NOVA_PULSE -> {
                    val pulse = (sin(phaseRad * 2f) + 1f) * 0.5f
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.18f * pulse),
                                Color.White.copy(alpha = 0.14f * (1f - pulse)),
                                accentColor.copy(alpha = 0.18f * pulse),
                            ),
                        ),
                        cornerRadius = CornerRadius(rPx, rPx),
                    )
                }
                else -> Unit
            }
        }
}

/**
 * 5. Enlarged Screen-Spanning Multi-Mode Connection Wave & Radar Pulse (5 Distinct Styles)
 */
@Composable
fun NeumorphicRadarPulse(
    color: Color,
    isActive: Boolean,
    isConnecting: Boolean = false,
    size: Dp = 320.dp,
    waveStyle: HeroWaveAnimationStyle = HeroWaveAnimationStyle.HARMONIC_ORBIT,
    speedMultiplier: Float = 1.0f,
    amplitudeScale: Float = 1.0f,
    glowEnabled: Boolean = true,
    colorMode: WaveColorMode = WaveColorMode.AUTO_STATUS,
    modifier: Modifier = Modifier,
    centerContent: @Composable () -> Unit = {},
) {
    val effectiveColor = if (colorMode == WaveColorMode.AUTO_STATUS || colorMode.colorHex == 0L) {
        color
    } else {
        Color(colorMode.colorHex)
    }
    val safeSpeed = speedMultiplier.coerceIn(0.4f, 2.5f)
    val safeAmp = amplitudeScale.coerceIn(0.5f, 1.8f)

    val infinite = rememberInfiniteTransition(label = "harmonic-wave-2026")
    val wavePhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = ((if (isConnecting) 1400 else 3000) / safeSpeed).toInt().coerceIn(450, 7500),
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wave-phase",
    )
    val rippleProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = ((if (isConnecting) 1250 else 2500) / safeSpeed).toInt().coerceIn(450, 6500),
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ripple-progress",
    )
    val breathScale by infinite.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.09f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breath-scale",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = (this.size.minDimension / 2f)
            if (maxRadius <= 4f || !maxRadius.isFinite()) return@Canvas

            // Ambient soft radial aura glow spanning almost full screen width
            if (glowEnabled) {
                val auraAlpha = when {
                    isActive -> 0.28f
                    isConnecting -> 0.24f
                    else -> 0.12f
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            effectiveColor.copy(alpha = auraAlpha),
                            effectiveColor.copy(alpha = auraAlpha * 0.45f),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = maxRadius * breathScale,
                    ),
                    radius = maxRadius * breathScale,
                    center = center,
                )
            }

            when (waveStyle) {
                HeroWaveAnimationStyle.HARMONIC_ORBIT -> {
                    val layerCount = 5
                    for (layer in 0 until layerCount) {
                        val layerOffset = layer * 0.20f
                        val progress = (rippleProgress + layerOffset) % 1f
                        val baseR = maxRadius * (0.30f + 0.68f * progress)
                        val alphaEnvelope = sin(progress * PI.toFloat()).coerceIn(0f, 1f)
                        val ringAlpha = alphaEnvelope * (if (isActive || isConnecting) 0.62f else 0.28f)

                        val wavePath = Path()
                        val steps = 88
                        val lobeCount = if (layer % 2 == 0) 6 else 8
                        val amplitude = maxRadius * (if (isActive || isConnecting) 0.038f else 0.018f) * (1f + layer * 0.20f) * safeAmp
                        val dir = if (layer % 2 == 0) 1f else -1f

                        for (i in 0..steps) {
                            val theta = (i.toFloat() / steps) * (2f * PI.toFloat())
                            val harmonic = sin(lobeCount * theta + dir * wavePhase + layer * 0.9f) * amplitude +
                                cos((lobeCount / 2) * theta - dir * wavePhase * 0.75f) * (amplitude * 0.48f)
                            val r = baseR + harmonic
                            val x = center.x + r * cos(theta)
                            val y = center.y + r * sin(theta)
                            if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                        }
                        wavePath.close()

                        drawPath(
                            path = wavePath,
                            color = effectiveColor.copy(alpha = ringAlpha),
                            style = Stroke(
                                width = (2.8f - layer * 0.36f).coerceAtLeast(1.1f).dp.toPx(),
                                cap = StrokeCap.Round,
                            ),
                        )
                    }
                }
                HeroWaveAnimationStyle.SONAR_PULSE -> {
                    val ringCount = 5
                    for (ring in 0 until ringCount) {
                        val p = (rippleProgress + ring * 0.20f) % 1f
                        val r = maxRadius * (0.26f + 0.72f * p * safeAmp.coerceIn(0.75f, 1.15f))
                        val alpha = (1f - p) * (if (isActive || isConnecting) 0.58f else 0.26f)
                        drawCircle(
                            color = effectiveColor.copy(alpha = alpha),
                            radius = r,
                            center = center,
                            style = Stroke(
                                width = (2.6f * (1f - p * 0.5f)).dp.toPx(),
                                pathEffect = if (ring % 2 == 1) {
                                    PathEffect.dashPathEffect(floatArrayOf(18f, 12f), phase = wavePhase * 40f)
                                } else null,
                            ),
                        )
                    }
                    val sweepDeg = (wavePhase / (2f * PI.toFloat())) * 360f
                    rotate(sweepDeg, pivot = center) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                0f to Color.Transparent,
                                0.75f to effectiveColor.copy(alpha = 0.02f),
                                1f to effectiveColor.copy(alpha = if (isActive || isConnecting) 0.35f else 0.16f),
                                center = center,
                            ),
                            startAngle = 0f,
                            sweepAngle = 95f,
                            useCenter = true,
                            topLeft = Offset(center.x - maxRadius * 0.90f, center.y - maxRadius * 0.90f),
                            size = Size(maxRadius * 1.80f, maxRadius * 1.80f),
                        )
                    }
                }
                HeroWaveAnimationStyle.PLASMA_VORTEX -> {
                    val arms = 4
                    for (arm in 0 until arms) {
                        val path = Path()
                        val steps = 72
                        val armAngleOffset = arm * (2f * PI.toFloat() / arms)
                        for (s in 0..steps) {
                            val t = s.toFloat() / steps
                            val r = maxRadius * (0.28f + 0.68f * t)
                            val theta = armAngleOffset + t * 3.6f * safeAmp - wavePhase * 1.4f
                            val x = center.x + r * cos(theta)
                            val y = center.y + r * sin(theta)
                            if (s == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(
                            path = path,
                            color = effectiveColor.copy(alpha = if (isActive || isConnecting) 0.46f else 0.22f),
                            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                }
                HeroWaveAnimationStyle.STARFIELD_NOVA -> {
                    val rayCount = 16
                    for (ray in 0 until rayCount) {
                        val angle = (ray.toFloat() / rayCount) * (2f * PI.toFloat()) + wavePhase * 0.35f
                        val pulse = (sin(wavePhase * 2f + ray * 0.8f) + 1f) * 0.5f * safeAmp.coerceIn(0.6f, 1.3f)
                        val innerR = maxRadius * 0.32f
                        val outerR = maxRadius * (0.58f + 0.38f * pulse)
                        drawLine(
                            color = effectiveColor.copy(alpha = ((0.18f + 0.36f * pulse) * (if (isActive || isConnecting) 1f else 0.55f)).coerceIn(0.05f, 0.95f)),
                            start = Offset(center.x + innerR * cos(angle), center.y + innerR * sin(angle)),
                            end = Offset(center.x + outerR * cos(angle), center.y + outerR * sin(angle)),
                            strokeWidth = 2.2.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                    for (ring in 0 until 3) {
                        val p = (rippleProgress + ring * 0.33f) % 1f
                        val r = maxRadius * (0.34f + 0.62f * p)
                        drawCircle(
                            color = effectiveColor.copy(alpha = sin(p * PI.toFloat()) * 0.42f),
                            radius = r,
                            center = center,
                            style = Stroke(width = 1.6.dp.toPx()),
                        )
                    }
                }
                HeroWaveAnimationStyle.CYBER_HEX_SHIELD -> {
                    val hexLayers = 4
                    for (layer in 0 until hexLayers) {
                        val p = (rippleProgress + layer * 0.25f) % 1f
                        val r = maxRadius * (0.34f + 0.62f * p)
                        val hexPath = Path()
                        val sides = 6
                        val rot = wavePhase * (if (layer % 2 == 0) 0.4f else -0.4f)
                        for (i in 0..sides) {
                            val a = rot + i * (2f * PI.toFloat() / sides)
                            val x = center.x + r * cos(a)
                            val y = center.y + r * sin(a)
                            if (i == 0) hexPath.moveTo(x, y) else hexPath.lineTo(x, y)
                        }
                        hexPath.close()
                        drawPath(
                            path = hexPath,
                            color = effectiveColor.copy(alpha = sin(p * PI.toFloat()) * (if (isActive || isConnecting) 0.55f else 0.25f)),
                            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                }
                HeroWaveAnimationStyle.QUANTUM_HELIX -> {
                    val strands = 2
                    val steps = 96
                    val baseR = maxRadius * 0.68f
                    val amp = maxRadius * 0.14f * safeAmp
                    for (strand in 0 until strands) {
                        val path = Path()
                        val phaseShift = strand * PI.toFloat()
                        for (i in 0..steps) {
                            val theta = (i.toFloat() / steps) * (2f * PI.toFloat())
                            val r = baseR + sin(theta * 8f + wavePhase * 2f + phaseShift) * amp
                            val x = center.x + r * cos(theta)
                            val y = center.y + r * sin(theta)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        path.close()
                        drawPath(
                            path = path,
                            color = effectiveColor.copy(alpha = if (isActive || isConnecting) 0.58f else 0.28f),
                            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                    for (node in 0 until 16) {
                        val theta = (node.toFloat() / 16f) * (2f * PI.toFloat())
                        val r1 = baseR + sin(theta * 8f + wavePhase * 2f) * amp
                        val r2 = baseR + sin(theta * 8f + wavePhase * 2f + PI.toFloat()) * amp
                        drawLine(
                            color = effectiveColor.copy(alpha = if (isActive || isConnecting) 0.36f else 0.18f),
                            start = Offset(center.x + r1 * cos(theta), center.y + r1 * sin(theta)),
                            end = Offset(center.x + r2 * cos(theta), center.y + r2 * sin(theta)),
                            strokeWidth = 1.4.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }
                HeroWaveAnimationStyle.LIQUID_SPECTRUM -> {
                    val barCount = 48
                    val innerR = maxRadius * 0.44f
                    for (i in 0 until barCount) {
                        val theta = (i.toFloat() / barCount) * (2f * PI.toFloat())
                        val wave = ((sin(theta * 6f + wavePhase * 2.2f) + cos(theta * 4f - wavePhase * 1.5f)) * 0.25f + 0.5f)
                            .coerceIn(0.08f, 1f)
                        val barLen = maxRadius * (if (isActive || isConnecting) 0.42f else 0.18f) * wave * safeAmp.coerceIn(0.6f, 1.35f)
                        val outerR = (innerR + barLen).coerceAtMost(maxRadius * 0.96f)
                        drawLine(
                            color = effectiveColor.copy(alpha = (0.28f + 0.58f * wave) * (if (isActive || isConnecting) 1f else 0.55f)),
                            start = Offset(center.x + innerR * cos(theta), center.y + innerR * sin(theta)),
                            end = Offset(center.x + outerR * cos(theta), center.y + outerR * sin(theta)),
                            strokeWidth = 3.2.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }
                HeroWaveAnimationStyle.AURORA_CROWN -> {
                    for (ring in 0 until 4) {
                        val p = (rippleProgress + ring * 0.25f) % 1f
                        val r = maxRadius * (0.36f + 0.58f * p)
                        val rot = wavePhase * (if (ring % 2 == 0) 1f else -1.2f) * 57.2958f
                        rotate(rot, pivot = center) {
                            for (arcIdx in 0 until 3) {
                                drawArc(
                                    color = effectiveColor.copy(
                                        alpha = sin(p * PI.toFloat()) * (if (isActive || isConnecting) 0.65f else 0.30f),
                                    ),
                                    startAngle = arcIdx * 120f + 12f,
                                    sweepAngle = 78f * safeAmp.coerceIn(0.7f, 1.2f),
                                    useCenter = false,
                                    topLeft = Offset(center.x - r, center.y - r),
                                    size = Size(r * 2f, r * 2f),
                                    style = Stroke(width = (3.0f - ring * 0.4f).dp.toPx(), cap = StrokeCap.Round),
                                )
                            }
                        }
                    }
                }
                HeroWaveAnimationStyle.CYBER_FOX_AURORA -> {
                    val ribbons = 5
                    val steps = 84
                    for (rib in 0 until ribbons) {
                        val p = (rippleProgress + rib * 0.20f) % 1f
                        val baseR = maxRadius * (0.32f + 0.64f * p)
                        val ribbonPath = Path()
                        val earPeakFactor = maxRadius * 0.065f * safeAmp * (if (isActive || isConnecting) 1.15f else 0.6f)
                        for (i in 0..steps) {
                            val theta = (i.toFloat() / steps) * (2f * PI.toFloat())
                            val foxContour = sin(theta * 3f + wavePhase * (if (rib % 2 == 0) 1.2f else -1.1f)) * earPeakFactor +
                                cos(theta * 6f - wavePhase * 1.8f) * (earPeakFactor * 0.55f)
                            val r = baseR + foxContour
                            val x = center.x + r * cos(theta)
                            val y = center.y + r * sin(theta)
                            if (i == 0) ribbonPath.moveTo(x, y) else ribbonPath.lineTo(x, y)
                        }
                        ribbonPath.close()
                        val alpha = sin(p * PI.toFloat()).coerceIn(0f, 1f) * (if (isActive || isConnecting) 0.68f else 0.30f)
                        drawPath(
                            path = ribbonPath,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    effectiveColor.copy(alpha = alpha),
                                    Color(0xFF00F0FF).copy(alpha = alpha * 0.9f),
                                    Color.White.copy(alpha = alpha * 0.75f),
                                    effectiveColor.copy(alpha = alpha),
                                ),
                                center = center,
                            ),
                            style = Stroke(width = (2.8f - rib * 0.32f).coerceAtLeast(1.2f).dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                }
                HeroWaveAnimationStyle.ORBITAL_GYROSCOPE -> {
                    val rings = 4
                    for (g in 0 until rings) {
                        val r = maxRadius * (0.42f + g * 0.15f)
                        val tilt = 0.52f + 0.38f * sin(wavePhase + g * 0.85f)
                        val rotDeg = (wavePhase * 57.2958f * (if (g % 2 == 0) 1.1f else -0.9f)) + g * 45f
                        rotate(rotDeg, pivot = center) {
                            val rx = r
                            val ry = (r * tilt * safeAmp.coerceIn(0.65f, 1.25f)).coerceAtLeast(r * 0.25f)
                            drawOval(
                                color = effectiveColor.copy(alpha = if (isActive || isConnecting) (0.64f - g * 0.10f) else 0.26f),
                                topLeft = Offset(center.x - rx, center.y - ry),
                                size = Size(rx * 2f, ry * 2f),
                                style = Stroke(width = (2.4f - g * 0.25f).dp.toPx(), cap = StrokeCap.Round),
                            )
                            val nodeAngle = wavePhase * 2f + g
                            val nx = center.x + rx * cos(nodeAngle)
                            val ny = center.y + ry * sin(nodeAngle)
                            drawCircle(
                                color = Color.White.copy(alpha = if (isActive || isConnecting) 0.92f else 0.45f),
                                radius = 3.2.dp.toPx(),
                                center = Offset(nx, ny),
                            )
                        }
                    }
                }
                HeroWaveAnimationStyle.CRYSTAL_RESONANCE -> {
                    val facets = 8
                    val layers = 4
                    for (layer in 0 until layers) {
                        val p = (rippleProgress + layer * 0.25f) % 1f
                        val r = maxRadius * (0.34f + 0.62f * p)
                        val crystalPath = Path()
                        val rot = wavePhase * (if (layer % 2 == 0) 0.55f else -0.55f)
                        for (i in 0..(facets * 2)) {
                            val a = rot + i * (PI.toFloat() / facets)
                            val modR = if (i % 2 == 0) r else r * (0.86f + 0.08f * sin(wavePhase * 2f + layer) * safeAmp)
                            val x = center.x + modR * cos(a)
                            val y = center.y + modR * sin(a)
                            if (i == 0) crystalPath.moveTo(x, y) else crystalPath.lineTo(x, y)
                        }
                        crystalPath.close()
                        drawPath(
                            path = crystalPath,
                            color = effectiveColor.copy(alpha = sin(p * PI.toFloat()) * (if (isActive || isConnecting) 0.62f else 0.28f)),
                            style = Stroke(width = 2.0.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                }
                HeroWaveAnimationStyle.GRAVITY_SINGULARITY -> {
                    val streamCount = 6
                    for (sIdx in 0 until streamCount) {
                        val streamPath = Path()
                        val steps = 60
                        val baseAngle = sIdx * (2f * PI.toFloat() / streamCount)
                        for (step in 0..steps) {
                            val t = step.toFloat() / steps
                            val inwardT = (1f - ((t + rippleProgress) % 1f))
                            val r = maxRadius * (0.30f + 0.68f * inwardT)
                            val theta = baseAngle + (1f - inwardT) * 2.8f * safeAmp + wavePhase
                            val x = center.x + r * cos(theta)
                            val y = center.y + r * sin(theta)
                            if (step == 0) streamPath.moveTo(x, y) else streamPath.lineTo(x, y)
                        }
                        drawPath(
                            path = streamPath,
                            color = effectiveColor.copy(alpha = if (isActive || isConnecting) 0.54f else 0.24f),
                            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                    for (ring in 0 until 3) {
                        val invP = 1f - ((rippleProgress + ring * 0.33f) % 1f)
                        val r = maxRadius * (0.32f + 0.64f * invP)
                        drawCircle(
                            color = effectiveColor.copy(alpha = sin(invP * PI.toFloat()) * (if (isActive || isConnecting) 0.52f else 0.22f)),
                            radius = r,
                            center = center,
                            style = Stroke(width = 1.8.dp.toPx()),
                        )
                    }
                }
                HeroWaveAnimationStyle.HYPERDRIVE_WARP -> {
                    val streakCount = 24
                    for (i in 0 until streakCount) {
                        val p = (rippleProgress + (i % 6) * 0.16f) % 1f
                        val theta = (i.toFloat() / streakCount) * (2f * PI.toFloat()) + wavePhase * 0.25f
                        val rInner = maxRadius * (0.28f + 0.42f * p)
                        val rOuter = (rInner + maxRadius * 0.26f * p * safeAmp.coerceIn(0.7f, 1.3f)).coerceAtMost(maxRadius * 0.98f)
                        val alpha = sin(p * PI.toFloat()) * (if (isActive || isConnecting) 0.72f else 0.34f)
                        drawLine(
                            color = if (i % 3 == 0) Color.White.copy(alpha = alpha * 0.85f) else effectiveColor.copy(alpha = alpha),
                            start = Offset(center.x + rInner * cos(theta), center.y + rInner * sin(theta)),
                            end = Offset(center.x + rOuter * cos(theta), center.y + rOuter * sin(theta)),
                            strokeWidth = (1.6f + 1.8f * p).dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                    for (ring in 0 until 3) {
                        val p = (rippleProgress + ring * 0.33f) % 1f
                        val r = maxRadius * (0.30f + 0.66f * p)
                        drawCircle(
                            color = effectiveColor.copy(alpha = (1f - p) * (if (isActive || isConnecting) 0.52f else 0.24f)),
                            radius = r,
                            center = center,
                            style = Stroke(width = 1.8.dp.toPx()),
                        )
                    }
                }
                HeroWaveAnimationStyle.NEON_PRISM_MATRIX -> {
                    val layers = 4
                    for (layer in 0 until layers) {
                        val p = (rippleProgress + layer * 0.25f) % 1f
                        val r = maxRadius * (0.34f + 0.62f * p)
                        val sides = if (layer % 2 == 0) 3 else 6
                        val rot = wavePhase * (if (layer % 2 == 0) 0.8f else -0.65f) + layer * 0.5f
                        val poly = Path()
                        for (s in 0..sides) {
                            val a = rot + s * (2f * PI.toFloat() / sides)
                            val x = center.x + r * cos(a)
                            val y = center.y + r * sin(a)
                            if (s == 0) poly.moveTo(x, y) else poly.lineTo(x, y)
                        }
                        poly.close()
                        val alpha = sin(p * PI.toFloat()) * (if (isActive || isConnecting) 0.64f else 0.28f)
                        drawPath(
                            path = poly,
                            color = effectiveColor.copy(alpha = alpha),
                            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                }
                HeroWaveAnimationStyle.CYBER_PULSE_TURBINE -> {
                    val bladeCount = 12
                    val rotBase = wavePhase * 1.35f
                    for (b in 0 until bladeCount) {
                        val startA = rotBase + b * (2f * PI.toFloat() / bladeCount)
                        val endA = startA + 0.42f * safeAmp.coerceIn(0.7f, 1.3f)
                        val r1 = maxRadius * 0.38f
                        val r2 = maxRadius * (0.76f + 0.14f * sin(wavePhase * 2f + b))
                        val bladePath = Path().apply {
                            moveTo(center.x + r1 * cos(startA), center.y + r1 * sin(startA))
                            quadraticTo(
                                center.x + ((r1 + r2) * 0.54f) * cos((startA + endA) * 0.5f),
                                center.y + ((r1 + r2) * 0.54f) * sin((startA + endA) * 0.5f),
                                center.x + r2 * cos(endA),
                                center.y + r2 * sin(endA),
                            )
                        }
                        drawPath(
                            path = bladePath,
                            color = effectiveColor.copy(alpha = if (isActive || isConnecting) 0.58f else 0.26f),
                            style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                }
                HeroWaveAnimationStyle.AURORA_RIBBON_SILK -> {
                    val silkCount = 6
                    val steps = 90
                    for (sIdx in 0 until silkCount) {
                        val p = (rippleProgress + sIdx * (1f / silkCount)) % 1f
                        val baseR = maxRadius * (0.32f + 0.62f * p)
                        val silkPath = Path()
                        val amp = maxRadius * 0.055f * safeAmp * (if (isActive || isConnecting) 1.2f else 0.65f)
                        for (i in 0..steps) {
                            val theta = (i.toFloat() / steps) * (2f * PI.toFloat())
                            val offset = sin(theta * 4f + wavePhase * 1.5f + sIdx * 0.8f) * amp +
                                sin(theta * 7f - wavePhase * 2.1f) * (amp * 0.45f)
                            val r = baseR + offset
                            val x = center.x + r * cos(theta)
                            val y = center.y + r * sin(theta)
                            if (i == 0) silkPath.moveTo(x, y) else silkPath.lineTo(x, y)
                        }
                        silkPath.close()
                        val alpha = sin(p * PI.toFloat()) * (if (isActive || isConnecting) 0.66f else 0.30f)
                        drawPath(
                            path = silkPath,
                            color = effectiveColor.copy(alpha = alpha),
                            style = Stroke(width = (2.6f - sIdx * 0.25f).coerceAtLeast(1.1f).dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                }
            }

            // Inner subtle precision ring + rotating orbital satellites
            val innerRingR = maxRadius * 0.36f * breathScale
            drawCircle(
                color = effectiveColor.copy(alpha = if (isActive || isConnecting) 0.34f else 0.16f),
                radius = innerRingR,
                center = center,
                style = Stroke(width = 1.5.dp.toPx()),
            )

            if (glowEnabled) {
                for (orb in 0 until 4) {
                    val orbAngle = wavePhase * (if (orb % 2 == 0) 1f else -0.8f) + (orb * 2f * PI.toFloat() / 4f)
                    val orbRadius = maxRadius * (0.48f + orb * 0.13f)
                    val ox = center.x + orbRadius * cos(orbAngle)
                    val oy = center.y + orbRadius * sin(orbAngle)
                    drawCircle(
                        color = effectiveColor.copy(alpha = if (isActive || isConnecting) 0.72f else 0.32f),
                        radius = 3.5.dp.toPx(),
                        center = Offset(ox, oy),
                    )
                }
            }

            if (isConnecting) {
                val deg = (wavePhase / (2f * PI.toFloat())) * 360f
                rotate(deg, pivot = center) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            0f to Color.Transparent,
                            0.60f to effectiveColor.copy(alpha = 0.08f),
                            1f to effectiveColor.copy(alpha = 0.58f),
                            center = center,
                        ),
                        startAngle = 0f,
                        sweepAngle = 135f,
                        useCenter = false,
                        topLeft = Offset(center.x - maxRadius * 0.78f, center.y - maxRadius * 0.78f),
                        size = Size(maxRadius * 1.56f, maxRadius * 1.56f),
                        style = Stroke(width = 3.4.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
            }
        }
        centerContent()
    }
}

/**
 * 6. 3D Interactive World Globe & Cyber Projection Background
 * Renders a large, screen-spanning 3D spherical globe centered behind the hero area.
 */
private val WORLD_MAP_BITMAP: List<String> = listOf(
    "................................................",
    "......#####....####..............#####..........",
    "....########..#####...........#############.....",
    "...##########...##..........################....",
    "...###########.............##################...",
    "....#########......###....###################...",
    ".....######.......#####...###################...",
    "......####........#####....#################....",
    ".......###.........#######..###############.....",
    "........###........#######...#############......",
    ".........#####.....#######....###########.......",
    "..........######....######.....########..##.....",
    "..........#######...######......#####....###....",
    "...........######....#####.......###............",
    "...........#####.....####.................####..",
    "............####......###................######.",
    "............###.......##.................######.",
    ".............##...........................####..",
    "................................................",
)

// (Lon in radians -PI..PI, Lat in radians -PI/2..PI/2) for major global VPN hubs
private val GLOBE_HUB_COORDS: List<Pair<Float, Float>> = listOf(
    Pair(-1.29f, 0.71f),  // New York
    Pair(-2.13f, 0.65f),  // Los Angeles
    Pair(0.15f, 0.88f),   // Frankfurt / Brussels
    Pair(-0.02f, 0.90f),  // London / Amsterdam
    Pair(0.43f, 1.05f),   // Helsinki / Stockholm
    Pair(1.81f, 0.02f),   // Singapore
    Pair(2.44f, 0.62f),   // Tokyo
    Pair(2.64f, -0.59f),  // Sydney
)

@Composable
fun AnimatedWorldMapBackground(
    isConnected: Boolean,
    isConnecting: Boolean,
    accentColor: Color,
    isDark: Boolean,
    scrollOffsetPx: Float = 0f,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "vpn-2d-dot-map-bg")
    val scanWave by infinite.animateFloat(
        initialValue = -0.25f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isConnecting) 2800 else 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "vpn-map-scan-wave",
    )
    val nodePulse by infinite.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "vpn-map-node-pulse",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 16f || h <= 16f || !w.isFinite() || !h.isFinite()) return@Canvas

        val safeScrollPx = if (scrollOffsetPx.isFinite()) scrollOffsetPx else 0f
        val mapTopY = (h * 0.12f - safeScrollPx * 0.05f).coerceIn(24.dp.toPx(), h * 0.25f)
        val mapHeight = (h * 0.46f).coerceAtLeast(180.dp.toPx())
        val mapCenter = Offset(w * 0.5f, mapTopY + mapHeight * 0.48f)

        // 1. Keep background 100% free of any radial color wash/stain (matches Deklik dark/light reference)
        val neutralMapTint = if (isDark) Color.White else Color(0xFF09090B)
        val activeMapColor = if (isConnected || isConnecting) accentColor else neutralMapTint

        // 2. Flat 2D Tactical Mercator Grid Lines (Subtle neutral)
        val gridAlpha = if (isDark) 0.035f else 0.03f
        for (i in 1..7) {
            val gx = w * (i / 8f)
            drawLine(
                color = neutralMapTint.copy(alpha = gridAlpha),
                start = Offset(gx, mapTopY),
                end = Offset(gx, mapTopY + mapHeight),
                strokeWidth = 0.8.dp.toPx(),
            )
        }
        for (j in 0..4) {
            val gy = mapTopY + mapHeight * (j / 4f)
            drawLine(
                color = neutralMapTint.copy(alpha = gridAlpha),
                start = Offset(0f, gy),
                end = Offset(w, gy),
                strokeWidth = 0.8.dp.toPx(),
            )
        }

        // 3. Flat 2D Continental Dot-Matrix Map (Clean neutral dots when idle, subtle accent wave when connected)
        val rows = WORLD_MAP_BITMAP.size
        val cols = WORLD_MAP_BITMAP.first().length
        val baseDotRadius = 1.8.dp.toPx()

        for (r in 0 until rows) {
            val rowStr = WORLD_MAP_BITMAP[r]
            val normY = r.toFloat() / (rows - 1).toFloat()
            val sy = mapTopY + normY * mapHeight
            for (c in 0 until cols) {
                if (rowStr[c] != '#') continue
                val normX = (c + 0.5f) / cols.toFloat()
                val sx = normX * w

                val distToWave = abs(((normX + normY * 0.15f) % 1f) - scanWave)
                val waveGlow = (1f - (distToWave / 0.22f)).coerceIn(0f, 1f)
                val baseAlpha = if (isDark) 0.09f else 0.07f
                val activeBoost = if (isConnected || isConnecting) 0.18f else 0.05f
                val dotAlpha = (baseAlpha + waveGlow * activeBoost).coerceIn(0.03f, 0.35f)

                drawCircle(
                    color = activeMapColor.copy(alpha = dotAlpha),
                    radius = baseDotRadius * (0.85f + waveGlow * 0.20f),
                    center = Offset(sx, sy),
                )
            }
        }

        // 4. Flat 2D Global VPN PoP Hub Nodes & Encrypted Relay Arcs
        val hubs2D = GLOBE_HUB_COORDS.map { (lon, lat) ->
            val nx = ((lon / (2f * PI.toFloat())) + 0.5f).coerceIn(0.06f, 0.94f)
            val ny = (0.5f - (lat / (PI.toFloat() * 0.85f))).coerceIn(0.08f, 0.92f)
            Offset(nx * w, mapTopY + ny * mapHeight)
        }

        val primaryHub = hubs2D[2] // Europe hub (Frankfurt / Amsterdam)
        val arcAlpha = if (isConnected) 0.28f else if (isConnecting) 0.22f else 0.10f
        hubs2D.forEachIndexed { idx, target ->
            if (idx != 2) {
                val midX = (primaryHub.x + target.x) * 0.5f
                val dist = sqrt(
                    (target.x - primaryHub.x) * (target.x - primaryHub.x) +
                        (target.y - primaryHub.y) * (target.y - primaryHub.y),
                )
                val midY = minOf(primaryHub.y, target.y) - dist * 0.22f
                val arcPath = Path().apply {
                    moveTo(primaryHub.x, primaryHub.y)
                    quadraticTo(midX, midY, target.x, target.y)
                }
                drawPath(
                    path = arcPath,
                    color = accentColor.copy(alpha = arcAlpha),
                    style = Stroke(
                        width = 1.25.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(10f, 8f),
                            phase = (scanWave * 180f + idx * 28f),
                        ),
                    ),
                )
            }
        }

        hubs2D.forEachIndexed { idx, hubPt ->
            val isPrimary = idx == 2 || idx == 3
            val pulseR = (if (isPrimary) 7.5.dp.toPx() else 5.0.dp.toPx()) * nodePulse
            drawCircle(
                color = accentColor.copy(alpha = (if (isPrimary) 0.28f else 0.16f) * nodePulse),
                radius = pulseR,
                center = hubPt,
            )
            drawCircle(
                color = accentColor.copy(alpha = if (isPrimary) 0.72f else 0.45f),
                radius = if (isPrimary) 3.0.dp.toPx() else 2.2.dp.toPx(),
                center = hubPt,
            )
        }
    }
}

/**
 * 7. Odometer-style Animated Text for Timers & Counters
 */
@Composable
fun AnimatedOdometerText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val cleanText = remember(text) {
        text.filterNot { it == '\u200E' || it == '\u200F' || it in '\u202A'..'\u202E' }
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            cleanText.forEachIndexed { index, char ->
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        (slideInVertically { height -> height / 2 } + fadeIn(tween(180)))
                            .togetherWith(slideOutVertically { height -> -height / 2 } + fadeOut(tween(180)))
                            .using(SizeTransform(clip = false))
                    },
                    label = "odometer-char-$index",
                ) { targetChar ->
                    Text(
                        text = targetChar.toString(),
                        style = style,
                        color = color,
                    )
                }
            }
        }
    }
}

/**
 * 8. Dual-Line Live Traffic Waveform Sparkline Canvas
 */
@Composable
fun LiveTrafficSparkline(
    rxHistory: List<Long>,
    txHistory: List<Long>,
    rxColor: Color,
    txColor: Color,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "wave-phase")
    val waveShift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wave-shift",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val maxVal = maxOf(
            rxHistory.maxOrNull() ?: 1024L,
            txHistory.maxOrNull() ?: 1024L,
            4096L,
        ).toFloat()

        fun buildSmoothPath(points: List<Long>, phaseOffset: Float): Path {
            val path = Path()
            val count = points.size.coerceAtLeast(2)
            val stepX = w / (count - 1).toFloat()
            for (i in 0 until count) {
                val sample = points.getOrElse(i) { 0L }.toFloat()
                val normalized = (sample / maxVal).coerceIn(0f, 1f)
                val idleBreath = sin(waveShift + phaseOffset + i * 0.45f) * 0.04f
                val yFactor = (normalized * 0.78f + 0.10f + idleBreath).coerceIn(0.06f, 0.92f)
                val x = i * stepX
                val y = h - (yFactor * h)
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    val prevX = (i - 1) * stepX
                    val prevSample = points.getOrElse(i - 1) { 0L }.toFloat()
                    val prevNorm = (prevSample / maxVal).coerceIn(0f, 1f)
                    val prevBreath = sin(waveShift + phaseOffset + (i - 1) * 0.45f) * 0.04f
                    val prevY = h - ((prevNorm * 0.78f + 0.10f + prevBreath).coerceIn(0.06f, 0.92f) * h)
                    val ctrlX = (prevX + x) / 2f
                    path.cubicTo(ctrlX, prevY, ctrlX, y, x, y)
                }
            }
            return path
        }

        val rxPath = buildSmoothPath(rxHistory, 0f)
        val txPath = buildSmoothPath(txHistory, 1.6f)

        // Fill under RX curve
        val fillPath = Path().apply {
            addPath(rxPath)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(rxColor.copy(alpha = 0.28f), Color.Transparent),
                startY = 0f,
                endY = h,
            ),
        )

        drawPath(
            path = txPath,
            color = txColor.copy(alpha = 0.75f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )
        drawPath(
            path = rxPath,
            color = rxColor,
            style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

/**
 * 9a. Animated Broadcasting Signal Tower Wave (For Dynamic Island)
 * Renders a central radio/cell signal tower mast with glowing beacon and outward-propagating
 * electromagnetic wave arcs on both sides ((( T ))).
 */
@Composable
fun AnimatedSignalTowerWave(
    active: Boolean,
    color: Color,
    waveStyle: com.vauth.foxyvpn.data.DynamicIslandWaveStyle = com.vauth.foxyvpn.data.DynamicIslandWaveStyle.SIGNAL_TOWER,
    speedMultiplier: Float = 1.0f,
    amplitudeScale: Float = 1.0f,
    glowEnabled: Boolean = true,
    colorMode: WaveColorMode = WaveColorMode.AUTO_STATUS,
    modifier: Modifier = Modifier,
) {
    val effectiveColor = if (colorMode == WaveColorMode.AUTO_STATUS || colorMode.colorHex == 0L) {
        color
    } else {
        Color(colorMode.colorHex)
    }
    val safeSpeed = speedMultiplier.coerceIn(0.4f, 2.5f)
    val safeAmp = amplitudeScale.coerceIn(0.5f, 1.8f)

    val infinite = rememberInfiniteTransition(label = "signal-tower-wave")
    val waveExpand by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = ((if (active) 1350 else 2600) / safeSpeed).toInt().coerceIn(380, 6000),
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "tower-wave-expand",
    )
    val beaconPulse by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (550 / safeSpeed).toInt().coerceIn(220, 1500),
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tower-beacon-pulse",
    )

    Canvas(
        modifier = modifier
            .width(26.dp)
            .height(18.dp),
    ) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        if (glowEnabled && active) {
            drawCircle(
                color = effectiveColor.copy(alpha = 0.20f * beaconPulse),
                radius = minOf(w, h) * 0.52f,
                center = Offset(cx, cy),
            )
        }

        when (waveStyle) {
            com.vauth.foxyvpn.data.DynamicIslandWaveStyle.EQUALIZER_BARS -> {
                val barCount = 5
                val barW = 2.6.dp.toPx()
                val gap = (w - barCount * barW) / (barCount + 1).coerceAtLeast(1)
                for (i in 0 until barCount) {
                    val phase = (waveExpand * 2f * PI.toFloat()) + i * 0.95f
                    val amp = if (active) ((0.28f + 0.68f * ((sin(phase) + 1f) * 0.5f)) * safeAmp).coerceIn(0.2f, 0.98f) else 0.25f
                    val barH = (h * amp).coerceAtLeast(3.dp.toPx())
                    val x = gap + i * (barW + gap)
                    drawRoundRect(
                        color = effectiveColor.copy(alpha = if (active) 0.92f else 0.45f),
                        topLeft = Offset(x, (h - barH) / 2f),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(barW / 2f, barW / 2f),
                    )
                }
            }
            com.vauth.foxyvpn.data.DynamicIslandWaveStyle.SINE_WAVE -> {
                val path = Path()
                val steps = 20
                val phaseRad = waveExpand * 2f * PI.toFloat()
                for (i in 0..steps) {
                    val t = i.toFloat() / steps
                    val x = t * w
                    val amp = (if (active) h * 0.34f else h * 0.12f) * safeAmp.coerceIn(0.6f, 1.3f)
                    val y = cy + sin(t * 2.2f * PI.toFloat() + phaseRad) * amp
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = effectiveColor.copy(alpha = if (active) 0.95f else 0.48f),
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round),
                )
            }
            com.vauth.foxyvpn.data.DynamicIslandWaveStyle.RADAR_PULSE -> {
                val maxR = minOf(w, h) * 0.46f * safeAmp.coerceIn(0.75f, 1.15f)
                drawCircle(
                    color = effectiveColor.copy(alpha = if (active) beaconPulse else 0.45f),
                    radius = 2.4.dp.toPx(),
                    center = Offset(cx, cy),
                )
                for (i in 0 until 3) {
                    val p = (waveExpand + i / 3f) % 1f
                    val r = 2.5.dp.toPx() + p * (maxR - 2.5.dp.toPx())
                    drawCircle(
                        color = effectiveColor.copy(alpha = (1f - p) * (if (active) 0.88f else 0.30f)),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.4.dp.toPx()),
                    )
                }
            }
            com.vauth.foxyvpn.data.DynamicIslandWaveStyle.ORBIT_DOTS -> {
                val rx = w * 0.38f * safeAmp.coerceIn(0.7f, 1.2f)
                val ry = h * 0.36f * safeAmp.coerceIn(0.7f, 1.2f)
                drawCircle(
                    color = effectiveColor.copy(alpha = if (active) 0.90f else 0.45f),
                    radius = 2.4.dp.toPx(),
                    center = Offset(cx, cy),
                )
                for (i in 0 until 3) {
                    val angle = waveExpand * 2f * PI.toFloat() + i * (2f * PI.toFloat() / 3f)
                    val px = cx + cos(angle) * rx
                    val py = cy + sin(angle) * ry
                    drawCircle(
                        color = effectiveColor.copy(alpha = if (active) 0.92f else 0.40f),
                        radius = 2.0.dp.toPx(),
                        center = Offset(px, py),
                    )
                }
            }
            com.vauth.foxyvpn.data.DynamicIslandWaveStyle.HEARTBEAT_WAVE -> {
                val path = Path()
                val pulseShift = (waveExpand - 0.5f) * w * 0.5f
                val midX = (cx + pulseShift).coerceIn(w * 0.25f, w * 0.75f)
                val peak = (if (active) h * 0.38f * beaconPulse else h * 0.12f) * safeAmp.coerceIn(0.6f, 1.25f)
                path.moveTo(0f, cy)
                path.lineTo(midX - 5.dp.toPx(), cy)
                path.lineTo(midX - 2.dp.toPx(), cy - peak)
                path.lineTo(midX + 2.dp.toPx(), cy + peak * 0.85f)
                path.lineTo(midX + 5.dp.toPx(), cy)
                path.lineTo(w, cy)
                drawPath(
                    path = path,
                    color = effectiveColor.copy(alpha = if (active) 0.95f else 0.48f),
                    style = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round),
                )
            }
            com.vauth.foxyvpn.data.DynamicIslandWaveStyle.DUAL_HELIX -> {
                val steps = 18
                val phaseRad = waveExpand * 2f * PI.toFloat()
                val amp = (if (active) h * 0.32f else h * 0.12f) * safeAmp.coerceIn(0.6f, 1.25f)
                val path1 = Path()
                val path2 = Path()
                for (i in 0..steps) {
                    val t = i.toFloat() / steps
                    val x = t * w
                    val y1 = cy + sin(t * 2f * PI.toFloat() + phaseRad) * amp
                    val y2 = cy + sin(t * 2f * PI.toFloat() + phaseRad + PI.toFloat()) * amp
                    if (i == 0) {
                        path1.moveTo(x, y1)
                        path2.moveTo(x, y2)
                    } else {
                        path1.lineTo(x, y1)
                        path2.lineTo(x, y2)
                    }
                }
                drawPath(path1, effectiveColor.copy(alpha = if (active) 0.95f else 0.45f), style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))
                drawPath(path2, effectiveColor.copy(alpha = if (active) 0.55f else 0.25f), style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round))
            }
            com.vauth.foxyvpn.data.DynamicIslandWaveStyle.CYBER_SPECTRUM -> {
                val barCount = 7
                val barW = 2.0.dp.toPx()
                val gap = (w - barCount * barW) / (barCount + 1).coerceAtLeast(1)
                for (i in 0 until barCount) {
                    val centerWeight = 1f - abs(i - 3) * 0.18f
                    val phase = (waveExpand * 2f * PI.toFloat() * 1.5f) + i * 0.7f
                    val rawAmp = if (active) (0.25f + 0.70f * ((sin(phase) + 1f) * 0.5f) * centerWeight * safeAmp) else 0.22f
                    val barH = (h * rawAmp.coerceIn(0.18f, 0.96f)).coerceAtLeast(2.5.dp.toPx())
                    val x = gap + i * (barW + gap)
                    drawRoundRect(
                        color = effectiveColor.copy(alpha = if (active) 0.94f else 0.42f),
                        topLeft = Offset(x, (h - barH) / 2f),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(barW / 2f, barW / 2f),
                    )
                }
            }
            com.vauth.foxyvpn.data.DynamicIslandWaveStyle.SONIC_RIPPLE -> {
                for (i in 0 until 3) {
                    val p = (waveExpand + i / 3f) % 1f
                    val rx = w * (0.14f + 0.34f * p * safeAmp.coerceIn(0.75f, 1.15f))
                    val ry = h * (0.18f + 0.30f * p * safeAmp.coerceIn(0.75f, 1.15f))
                    drawOval(
                        color = effectiveColor.copy(alpha = (1f - p) * (if (active) 0.90f else 0.34f)),
                        topLeft = Offset(cx - rx, cy - ry),
                        size = Size(rx * 2f, ry * 2f),
                        style = Stroke(width = 1.4.dp.toPx()),
                    )
                }
            }
            com.vauth.foxyvpn.data.DynamicIslandWaveStyle.LASER_MATRIX -> {
                val cols = 4
                val rows = 3
                val dotR = 1.6.dp.toPx()
                for (c in 0 until cols) {
                    for (r in 0 until rows) {
                        val x = w * (0.20f + c * 0.20f)
                        val y = h * (0.24f + r * 0.26f)
                        val wave = (sin(waveExpand * 2f * PI.toFloat() + c * 0.9f + r * 0.6f) + 1f) * 0.5f
                        val alpha = if (active) (0.25f + 0.72f * wave) else 0.28f
                        drawCircle(
                            color = effectiveColor.copy(alpha = alpha),
                            radius = dotR * (0.8f + 0.35f * wave * safeAmp.coerceIn(0.7f, 1.2f)),
                            center = Offset(x, y),
                        )
                    }
                }
            }
            com.vauth.foxyvpn.data.DynamicIslandWaveStyle.SIGNAL_TOWER -> {
                val towerCy = h * 0.42f
                val maxWaveRadius = w * 0.46f * safeAmp.coerceIn(0.75f, 1.15f)
                val towerBottom = h * 0.94f
                val towerBaseHalfW = w * 0.14f
                val towerPath = Path().apply {
                    moveTo(cx, towerCy)
                    lineTo(cx - towerBaseHalfW, towerBottom)
                    moveTo(cx, towerCy)
                    lineTo(cx + towerBaseHalfW, towerBottom)
                    val crossY = towerCy + (towerBottom - towerCy) * 0.58f
                    val crossHalfW = towerBaseHalfW * 0.6f
                    moveTo(cx - crossHalfW, crossY)
                    lineTo(cx + crossHalfW, crossY)
                }
                drawPath(
                    path = towerPath,
                    color = effectiveColor.copy(alpha = if (active) 0.95f else 0.5f),
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
                )
                drawCircle(
                    color = effectiveColor.copy(alpha = if (active) beaconPulse else 0.45f),
                    radius = 2.3.dp.toPx(),
                    center = Offset(cx, towerCy),
                )
                val waveCount = 3
                for (i in 0 until waveCount) {
                    val phase = (waveExpand + i / waveCount.toFloat()) % 1f
                    val r = 3.dp.toPx() + phase * (maxWaveRadius - 3.dp.toPx())
                    val alpha = (1f - phase) * (if (active) 0.92f else 0.32f)
                    val arcTopLeft = Offset(cx - r, towerCy - r)
                    val arcSize = Size(r * 2f, r * 2f)
                    val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(
                        color = effectiveColor.copy(alpha = alpha),
                        startAngle = -42f,
                        sweepAngle = 84f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = stroke,
                    )
                    drawArc(
                        color = effectiveColor.copy(alpha = alpha),
                        startAngle = 138f,
                        sweepAngle = 84f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = stroke,
                    )
                }
            }
        }
    }
}

/**
 * 9b. Animated Equalizer Activity Bars (Secondary helper)
 */
@Composable
fun AnimatedTrafficEqualizer(
    active: Boolean,
    color: Color,
    barCount: Int = 4,
    modifier: Modifier = Modifier,
) {
    AnimatedSignalTowerWave(
        active = active,
        color = color,
        modifier = modifier,
    )
}

/**
 * 10. Neumorphic Speedometer Analog + Digital Gauge Canvas
 * Needle is drawn in the outer ring band (0.34R..0.74R) so it never overlaps the center/bottom text.
 */
@Composable
fun NeumorphicSpeedometerGauge(
    speedMbps: Float,
    maxMbps: Float = 100f,
    accentColor: Color,
    trackColor: Color,
    isRunning: Boolean,
    modifier: Modifier = Modifier,
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = speedMbps.coerceIn(0f, maxMbps),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "speed-gauge-needle",
    )

    val infinite = rememberInfiniteTransition(label = "gauge-glow")
    val glowPulse by infinite.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "gauge-glow-val",
    )

    Canvas(modifier = modifier) {
        val strokeWidth = 14.dp.toPx()
        val diameter = size.minDimension - strokeWidth * 2f
        if (diameter <= 8f || !diameter.isFinite()) return@Canvas
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = diameter / 2f

        // Background track arc (135 deg to 405 deg -> 270 deg sweep)
        drawArc(
            color = trackColor,
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )

        // Tick marks
        val tickCount = 28
        for (i in 0..tickCount) {
            val fraction = i.toFloat() / tickCount
            val angleDeg = 135f + fraction * 270f
            val angleRad = angleDeg * (PI / 180.0)
            val isMajor = i % 4 == 0
            val innerR = radius - strokeWidth * (if (isMajor) 1.4f else 1.1f)
            val outerR = radius - strokeWidth * 0.7f
            val start = Offset(
                x = center.x + (innerR * cos(angleRad)).toFloat(),
                y = center.y + (innerR * sin(angleRad)).toFloat(),
            )
            val end = Offset(
                x = center.x + (outerR * cos(angleRad)).toFloat(),
                y = center.y + (outerR * sin(angleRad)).toFloat(),
            )
            drawLine(
                color = trackColor.copy(alpha = if (isMajor) 0.9f else 0.45f),
                start = start,
                end = end,
                strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        val sweepFraction = (animatedSpeed / maxMbps).coerceIn(0f, 1f)
        val activeSweep = sweepFraction * 270f

        if (activeSweep > 0.5f) {
            drawArc(
                color = accentColor.copy(alpha = if (isRunning) glowPulse else 0.9f),
                startAngle = 135f,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }

        // Outer-band floating needle (starts outside center readout zone so text never overlaps)
        val needleAngleRad = (135f + activeSweep) * (PI / 180.0)
        val needleStartLen = radius * 0.42f
        val needleEndLen = radius * 0.76f
        val needleBase = Offset(
            x = center.x + (needleStartLen * cos(needleAngleRad)).toFloat(),
            y = center.y + (needleStartLen * sin(needleAngleRad)).toFloat(),
        )
        val needleTip = Offset(
            x = center.x + (needleEndLen * cos(needleAngleRad)).toFloat(),
            y = center.y + (needleEndLen * sin(needleAngleRad)).toFloat(),
        )
        drawLine(
            color = accentColor,
            start = needleBase,
            end = needleTip,
            strokeWidth = 3.6.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = accentColor,
            radius = 4.2.dp.toPx(),
            center = needleBase,
        )
    }
}
