package com.vauth.foxyvpn.ui.theme

import android.graphics.BlurMaskFilter
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.R
import com.vauth.foxyvpn.data.FoxyUiDesignStyle
import com.vauth.foxyvpn.data.model.ConnectionState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class NeumorphicColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val shadowLight: Color,
    val shadowDark: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val isDark: Boolean,
    val designStyle: FoxyUiDesignStyle = FoxyUiDesignStyle.NEUMORPHISM,
)

val LocalNeumorphicColors = staticCompositionLocalOf {
    NeumorphicColors(
        background = NeuLightBg,
        surface = NeuLightSurface,
        surfaceElevated = NeuLightSurfaceElevated,
        shadowLight = NeuLightShadowLight,
        shadowDark = NeuLightShadowDark,
        accent = md_theme_light_primary,
        textPrimary = md_theme_light_onBackground,
        textSecondary = md_theme_light_onSurfaceVariant,
        isDark = false,
        designStyle = FoxyUiDesignStyle.NEUMORPHISM,
    )
}

val currentNeumorphicColors: NeumorphicColors
    @Composable
    @ReadOnlyComposable
    get() = LocalNeumorphicColors.current

/**
 * Renders style-adaptive outer shadow / bevel effects for Soft Neumorphism.
 */
fun Modifier.neumorphicRaised(
    shape: Shape = RoundedCornerShape(16.dp),
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 6.dp,
    isPressed: Boolean = false,
    lightShadowColor: Color? = null,
    darkShadowColor: Color? = null,
    designStyle: FoxyUiDesignStyle = FoxyUiDesignStyle.NEUMORPHISM,
    isDark: Boolean = false,
    accentColor: Color = Color(0xFF00B4FF),
): Modifier = this.then(
    Modifier.drawBehind {
        if (size.width <= 2f || size.height <= 2f || !size.width.isFinite() || !size.height.isFinite()) return@drawBehind
        val radiusPx = cornerRadius.toPx().coerceAtLeast(0f)

        // Pixel UI uses Material surfaces and tonal elevation rather than neumorphic blur.
        // The border supplied by the caller provides the separation between surfaces.
        if (designStyle == FoxyUiDesignStyle.PIXEL_GOOGLE || designStyle == FoxyUiDesignStyle.SAFETY_SURF) return@drawBehind

        val lightColor = lightShadowColor ?: if (isDark) Color(0xFF2F343F).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.92f)
        val darkColor = darkShadowColor ?: if (isDark) Color(0xFF0B0C10).copy(alpha = 0.85f) else Color(0xFFC2C9D6).copy(alpha = 0.65f)

        val currentElev = if (isPressed) (elevation * 0.25f) else elevation
        val blurPx = (currentElev * 1.35f).toPx()
        val offsetPx = (currentElev * 0.85f).toPx()

        drawIntoCanvas { canvas ->
            if (blurPx > 0.5f && blurPx.isFinite()) {
                val paintLight = Paint().asFrameworkPaint().apply {
                    color = lightColor.toArgb()
                    isAntiAlias = true
                    maskFilter = BlurMaskFilter(blurPx.coerceAtLeast(1f), BlurMaskFilter.Blur.NORMAL)
                }
                canvas.nativeCanvas.drawRoundRect(
                    -offsetPx,
                    -offsetPx,
                    size.width - offsetPx,
                    size.height - offsetPx,
                    radiusPx,
                    radiusPx,
                    paintLight,
                )

                val paintDark = Paint().asFrameworkPaint().apply {
                    color = darkColor.toArgb()
                    isAntiAlias = true
                    maskFilter = BlurMaskFilter(blurPx.coerceAtLeast(1f), BlurMaskFilter.Blur.NORMAL)
                }
                canvas.nativeCanvas.drawRoundRect(
                    offsetPx,
                    offsetPx,
                    size.width + offsetPx,
                    size.height + offsetPx,
                    radiusPx,
                    radiusPx,
                    paintDark,
                )
            }
        }
    }
)

/**
 * Renders tactile Soft Neumorphism inner top-left rim bevel and subtle gradient shading matching the reference images.
 */
fun Modifier.styleInnerTreatment(
    designStyle: FoxyUiDesignStyle,
    cornerRadius: Dp,
    isDark: Boolean,
    accentColor: Color,
): Modifier = this.then(
    Modifier.drawBehind {
        val w = size.width
        val h = size.height
        if (w <= 4f || h <= 4f || !w.isFinite() || !h.isFinite()) return@drawBehind
        if (designStyle == FoxyUiDesignStyle.PIXEL_GOOGLE || designStyle == FoxyUiDesignStyle.SAFETY_SURF) return@drawBehind
        val r = cornerRadius.toPx().coerceAtLeast(0f)

        // Pure neutral top-left luminosity + diagonal tactile sheen (zero color tint on cards)
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isDark) 0.025f else 0.35f),
                    Color.Transparent,
                    Color.Black.copy(alpha = if (isDark) 0.14f else 0.02f),
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h),
            ),
            size = size,
            cornerRadius = CornerRadius(r, r),
        )
    }
)

/**
 * Lightweight glass treatment for Pixel surfaces. It deliberately avoids a runtime blur
 * so older and low-memory devices get the translucent liquid-glass depth without GPU spikes.
 */
fun Modifier.pixelGlassOverlay(
    shape: Shape,
    accent: Color,
    isDark: Boolean,
    opacity: Float = 0.50f,
): Modifier = this
    .clip(shape)
    .background(accent.copy(alpha = opacity.coerceIn(0.35f, 0.62f)))
    .drawBehind {
        if (size.width <= 2f || size.height <= 2f) return@drawBehind
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isDark) 0.12f else 0.34f),
                    Color.Transparent,
                    Color.Black.copy(alpha = if (isDark) 0.08f else 0.035f),
                ),
            ),
            size = size,
            cornerRadius = CornerRadius((size.minDimension * 0.12f).coerceAtLeast(8f)),
        )
    }

/**
 * Renders a sunken / inset neumorphic groove for sliders, textboxes, and tracks.
 */
fun Modifier.neumorphicSunken(
    shape: Shape = RoundedCornerShape(16.dp),
    cornerRadius: Dp = 16.dp,
    depth: Dp = 4.dp,
    surfaceColor: Color = Color.Transparent,
): Modifier = this.then(
    Modifier
        .clip(shape)
        .drawBehind {
            val w = size.width
            val h = size.height
            if (w <= 2f || h <= 2f || !w.isFinite() || !h.isFinite()) return@drawBehind
            val r = cornerRadius.toPx().coerceAtLeast(0f)
            val d = depth.toPx().coerceAtLeast(1f)

            // Subtle base fill
            if (surfaceColor != Color.Transparent) {
                drawRoundRect(
                    color = surfaceColor,
                    size = size,
                    cornerRadius = CornerRadius(r, r),
                )
            }

            // Top-left inset dark shadow
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.32f), Color.Transparent),
                    start = Offset(0f, 0f),
                    end = Offset((d * 3f).coerceAtLeast(2f), (d * 3f).coerceAtLeast(2f)),
                ),
                size = size,
                cornerRadius = CornerRadius(r, r),
            )

            // Bottom-right inset light highlight
            val startX = (w - d * 2.4f).coerceAtMost(w - 2f)
            val startY = (h - d * 2.4f).coerceAtMost(h - 2f)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.14f)),
                    start = Offset(startX, startY),
                    end = Offset(w, h),
                ),
                size = size,
                cornerRadius = CornerRadius(r, r),
            )
        }
)

@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    elevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val neu = currentNeumorphicColors
    val style = neu.designStyle
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isPixel = style == FoxyUiDesignStyle.PIXEL_GOOGLE
    val isSurf = style == FoxyUiDesignStyle.SAFETY_SURF

    val effectiveCorner = when (style) {
        FoxyUiDesignStyle.PIXEL_GOOGLE -> 28.dp
        FoxyUiDesignStyle.SAFETY_SURF -> 22.dp
        FoxyUiDesignStyle.CLAYMORPHISM -> 26.dp
        FoxyUiDesignStyle.NEO_BRUTALISM -> 10.dp
        FoxyUiDesignStyle.EDITORIAL_DESIGN -> 8.dp
        FoxyUiDesignStyle.BENTO_UI -> 20.dp
        else -> cornerRadius
    }

    val animatedElevation by animateDpAsState(
        targetValue = if (isPixel) {
            if (isPressed) elevation * 0.45f else elevation * 0.85f
        } else if (isPressed) elevation * 0.3f else elevation,
        animationSpec = spring(stiffness = if (isPixel) 700f else 400f),
        label = "neu-card-elevation",
    )

    val shape = RoundedCornerShape(effectiveCorner)
    val borderWidth = when {
        isPixel -> 1.dp
        isSurf -> 0.dp
        style == FoxyUiDesignStyle.NEO_BRUTALISM -> 2.dp
        style == FoxyUiDesignStyle.DARK_LUXURY -> 1.3.dp
        else -> 1.1.dp
    }
    val borderBrush = if (neu.isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.05f),
                Color.White.copy(alpha = 0.03f),
            ),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.06f),
                Color.Black.copy(alpha = 0.04f),
                Color.Black.copy(alpha = 0.03f),
            ),
        )
    }

    Box(
        modifier = modifier
            .neumorphicRaised(
                shape = shape,
                cornerRadius = effectiveCorner,
                elevation = animatedElevation,
                isPressed = isPressed,
                lightShadowColor = neu.shadowLight.copy(alpha = if (neu.isDark) 0.45f else 0.94f),
                darkShadowColor = neu.shadowDark.copy(alpha = if (neu.isDark) 0.88f else 0.50f),
                designStyle = style,
                isDark = neu.isDark,
                accentColor = neu.accent,
            )
            .clip(shape)
            .then(
                if (isPixel) {
                    Modifier.pixelGlassOverlay(shape, MaterialTheme.colorScheme.surfaceContainer, neu.isDark)
                } else {
                    Modifier.background(neu.surface)
                },
            )
            .styleInnerTreatment(
                designStyle = style,
                cornerRadius = effectiveCorner,
                isDark = neu.isDark,
                accentColor = neu.accent,
            )
            .border(
                width = borderWidth,
                brush = borderBrush,
                shape = shape,
            )
            .graphicsLayer {
                if (isPixel && isPressed) {
                    scaleX = 0.985f
                    scaleY = 0.985f
                }
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else Modifier
            ),
    ) {
        content()
    }
}

@Composable
fun NeumorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 5.dp,
    accentColor: Color? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val neu = currentNeumorphicColors
    val style = neu.designStyle
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isPixel = style == FoxyUiDesignStyle.PIXEL_GOOGLE
    val isSurf = style == FoxyUiDesignStyle.SAFETY_SURF
    val haptics = LocalHapticFeedback.current

    val effectiveCorner = when (style) {
        FoxyUiDesignStyle.PIXEL_GOOGLE -> 24.dp
        FoxyUiDesignStyle.SAFETY_SURF -> 18.dp
        FoxyUiDesignStyle.CLAYMORPHISM -> 26.dp
        FoxyUiDesignStyle.NEO_BRUTALISM -> 8.dp
        FoxyUiDesignStyle.EDITORIAL_DESIGN -> 4.dp
        FoxyUiDesignStyle.BENTO_UI -> 20.dp
        else -> cornerRadius
    }

    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed && enabled) 1.5.dp else elevation,
        label = "btn-elev",
    )
    val shape = RoundedCornerShape(effectiveCorner)
    val bgColor = when {
        isPixel -> accentColor ?: MaterialTheme.colorScheme.secondaryContainer
        isSurf -> accentColor ?: neu.surface
        else -> accentColor ?: neu.surface
    }

    val borderWidth = when {
        isPixel -> 1.dp
        isSurf -> 0.dp
        style == FoxyUiDesignStyle.NEO_BRUTALISM -> 2.4.dp
        style == FoxyUiDesignStyle.DARK_LUXURY -> 1.4.dp
        else -> 1.dp
    }
    val borderColor = when (style) {
        FoxyUiDesignStyle.PIXEL_GOOGLE -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
        FoxyUiDesignStyle.NEO_BRUTALISM -> if (neu.isDark) Color.White else Color(0xFF070A10)
        FoxyUiDesignStyle.DARK_LUXURY -> Color(0xFFE5B84B).copy(alpha = 0.45f)
        else -> if (neu.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFF070A10).copy(alpha = 0.07f)
    }

    Box(
        modifier = modifier
            .neumorphicRaised(
                shape = shape,
                cornerRadius = effectiveCorner,
                elevation = animatedElevation,
                isPressed = isPressed && enabled,
                lightShadowColor = neu.shadowLight.copy(alpha = if (neu.isDark) 0.35f else 0.9f),
                darkShadowColor = neu.shadowDark.copy(alpha = if (neu.isDark) 0.75f else 0.5f),
                designStyle = style,
                isDark = neu.isDark,
                accentColor = neu.accent,
            )
            .clip(shape)
            .then(
                if (isPixel) {
                    Modifier.pixelGlassOverlay(shape, bgColor, neu.isDark)
                } else {
                    Modifier.background(bgColor)
                },
            )
            .styleInnerTreatment(
                designStyle = style,
                cornerRadius = effectiveCorner,
                isDark = neu.isDark,
                accentColor = neu.accent,
            )
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape,
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
            )
            .graphicsLayer {
                if (isPixel && isPressed && enabled) {
                    scaleX = 0.97f
                    scaleY = 0.97f
                }
            }
            .padding(horizontal = if (isPixel) 24.dp else 20.dp, vertical = if (isPixel) 16.dp else 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Row wrapper prevents Icon + Text from overlapping on top of each other!
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            content()
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun NeumorphicIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    content: @Composable () -> Unit,
) {
    val neu = currentNeumorphicColors
    val style = neu.designStyle
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isPixel = style == FoxyUiDesignStyle.PIXEL_GOOGLE
    val isSurf = style == FoxyUiDesignStyle.SAFETY_SURF
    val haptics = LocalHapticFeedback.current

    val shape = CircleShape
    val cornerRadius = size / 2

    val elevation by animateDpAsState(
        targetValue = if (isPixel) 0.dp else if (isPressed || isSelected) 1.dp else 4.dp,
        label = "icon-btn-elev",
    )

    val borderWidth = when {
        isPixel || isSurf -> 0.dp
        style == FoxyUiDesignStyle.NEO_BRUTALISM -> 2.2.dp
        style == FoxyUiDesignStyle.DARK_LUXURY -> 1.3.dp
        else -> 1.dp
    }
    val borderColor = when {
        isSelected -> neu.accent
        style == FoxyUiDesignStyle.NEO_BRUTALISM -> if (neu.isDark) Color.White else Color(0xFF070A10)
        style == FoxyUiDesignStyle.DARK_LUXURY -> Color(0xFFE5B84B).copy(alpha = 0.40f)
        neu.isDark -> Color.White.copy(alpha = 0.08f)
        else -> Color(0xFF070A10).copy(alpha = 0.07f)
    }

    Box(
        modifier = modifier
            .size(size)
            .neumorphicRaised(
                shape = shape,
                cornerRadius = cornerRadius,
                elevation = elevation,
                isPressed = isPressed || isSelected,
                lightShadowColor = neu.shadowLight.copy(alpha = if (neu.isDark) 0.35f else 0.9f),
                darkShadowColor = neu.shadowDark.copy(alpha = if (neu.isDark) 0.75f else 0.5f),
                designStyle = style,
                isDark = neu.isDark,
                accentColor = neu.accent,
            )
            .clip(shape)
            .then(
                if (isPixel) {
                    Modifier.pixelGlassOverlay(
                        shape,
                        if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        neu.isDark,
                    )
                } else {
                    Modifier.background(
                        when {
                            isSelected -> if (isSurf) neu.accent.copy(alpha = 0.18f) else neu.accent.copy(alpha = 0.16f)
                            isSurf -> neu.surface
                            else -> neu.surface
                        },
                    )
                },
            )
            .styleInnerTreatment(
                designStyle = style,
                cornerRadius = cornerRadius,
                isDark = neu.isDark,
                accentColor = neu.accent,
            )
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape,
            )
            .graphicsLayer { if (isPixel && isPressed) { scaleX = 0.92f; scaleY = 0.92f } }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
                onLongClick = onLongClick,
                onLongClickLabel = onLongClickLabel,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * Slide-to-Connect Neumorphic Component.
 * Minimalist Black + Blue + White Combined Design for both Day and Night modes.
 */
@Composable
fun NeumorphicSlideToConnect(
    state: ConnectionState,
    lastError: String?,
    onRequestConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val neu = currentNeumorphicColors
    val statusColors = LocalFoxyStatusColors.current
    val s = currentFoxyStrings
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val trackHeight = 68.dp
    val thumbSize = 56.dp
    val trackPadding = 6.dp
    val thumbRadius = 28.dp
    val trackCornerRadius = 34.dp

    var totalWidthPx by remember { mutableFloatStateOf(0f) }
    val maxDragPx = remember(totalWidthPx, density) {
        with(density) {
            (totalWidthPx - (thumbSize + trackPadding * 2).toPx()).coerceAtLeast(0f)
        }
    }

    // Drag offset in px
    val offsetX = remember { Animatable(0f) }

    var isDisconnecting by remember { mutableStateOf(false) }

    // When connection state changes, reset or position thumb
    LaunchedEffect(state, maxDragPx) {
        if (state != ConnectionState.CONNECTED) {
            isDisconnecting = false
        }
        if (maxDragPx > 0f) {
            when (state) {
                ConnectionState.CONNECTED -> {
                    offsetX.animateTo(maxDragPx, spring(stiffness = 300f))
                }
                ConnectionState.CONNECTING -> {
                    offsetX.animateTo(maxDragPx, spring(stiffness = 300f))
                }
                ConnectionState.DISCONNECTED -> {
                    offsetX.animateTo(0f, spring(stiffness = 400f))
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "slide-pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-alpha",
    )

    val hasError = state == ConnectionState.DISCONNECTED && !lastError.isNullOrBlank()
    val errorColor = MaterialTheme.colorScheme.error

    val trackBgColor by animateColorAsState(
        targetValue = when {
            isDisconnecting -> statusColors.connecting.copy(alpha = 0.14f)
            state == ConnectionState.CONNECTED -> statusColors.connected.copy(alpha = 0.14f)
            state == ConnectionState.CONNECTING -> statusColors.connecting.copy(alpha = 0.14f)
            hasError -> errorColor.copy(alpha = 0.14f)
            else -> neu.surfaceElevated
        },
        label = "track-bg-color",
    )

    val powerButtonBlue = if (neu.isDark) Color(0xFF1E88FF) else Color(0xFF0066FF)
    val thumbAccentColor by animateColorAsState(
        targetValue = when {
            hasError -> errorColor
            state == ConnectionState.CONNECTED -> statusColors.connected
            else -> powerButtonBlue
        },
        label = "thumb-accent-color",
    )

    val trackShape = RoundedCornerShape(trackCornerRadius)
    val thumbShape = CircleShape

    val dragProgress = if (maxDragPx > 0f) (offsetX.value / maxDragPx).coerceIn(0f, 1f) else 0f

    val dragModifier = Modifier.draggable(
        orientation = Orientation.Horizontal,
        reverseDirection = s.isRtl,
        enabled = state != ConnectionState.CONNECTING && !isDisconnecting,
        state = rememberDraggableState { delta ->
            if (maxDragPx > 0f) {
                coroutineScope.launch {
                    val newOffset = (offsetX.value + delta).coerceIn(0f, maxDragPx)
                    offsetX.snapTo(newOffset)
                }
            }
        },
        onDragStopped = {
            if (maxDragPx > 0f) {
                when (state) {
                    ConnectionState.DISCONNECTED -> {
                        if (offsetX.value >= maxDragPx * 0.55f) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            offsetX.animateTo(maxDragPx, spring(stiffness = 500f))
                            onRequestConnect()
                            kotlinx.coroutines.delay(350)
                            if (state == ConnectionState.DISCONNECTED) {
                                offsetX.animateTo(0f, spring(stiffness = 400f))
                            }
                        } else {
                            offsetX.animateTo(0f, spring(stiffness = 500f))
                        }
                    }
                    ConnectionState.CONNECTED -> {
                        if (offsetX.value <= maxDragPx * 0.45f) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            isDisconnecting = true
                            offsetX.animateTo(0f, spring(stiffness = 500f))
                            onDisconnect()
                            kotlinx.coroutines.delay(400)
                            isDisconnecting = false
                        } else {
                            offsetX.animateTo(maxDragPx, spring(stiffness = 500f))
                        }
                    }
                    ConnectionState.CONNECTING -> {
                        // During connecting, allow tap to cancel
                    }
                }
            }
        },
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .onSizeChanged { totalWidthPx = it.width.toFloat() }
            .then(dragModifier)
            .neumorphicRaised(
                shape = trackShape,
                cornerRadius = trackCornerRadius,
                elevation = 5.dp,
                lightShadowColor = neu.shadowLight.copy(alpha = if (neu.isDark) 0.35f else 0.85f),
                darkShadowColor = neu.shadowDark.copy(alpha = if (neu.isDark) 0.75f else 0.45f),
            )
            .clip(trackShape)
            .background(neu.surface)
            .border(
                width = 1.dp,
                color = when {
                    isDisconnecting -> statusColors.connecting.copy(alpha = 0.45f)
                    state == ConnectionState.CONNECTED -> statusColors.connected.copy(alpha = 0.45f)
                    state == ConnectionState.CONNECTING -> statusColors.connecting.copy(alpha = 0.45f)
                    hasError -> errorColor.copy(alpha = 0.55f)
                    neu.isDark -> Color.White.copy(alpha = 0.10f)
                    else -> Color.White.copy(alpha = 0.80f)
                },
                shape = trackShape,
            )
            .neumorphicSunken(
                shape = trackShape,
                cornerRadius = trackCornerRadius,
                depth = 3.dp,
                surfaceColor = trackBgColor,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Track text and guidance
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isDisconnecting -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 48.dp),
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(18.dp),
                            color = statusColors.connecting,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = s.disconnecting,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColors.connecting,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
                state == ConnectionState.DISCONNECTED -> {
                    val labelAlpha = (1f - dragProgress * 1.4f).coerceIn(0f, 1f)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 60.dp, end = 12.dp)
                            .graphicsLayer { alpha = labelAlpha },
                    ) {
                        Text(
                            text = if (hasError) s.slideToRetry else s.slideToConnect,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp,
                            ),
                            color = if (hasError) errorColor else neu.textPrimary.copy(alpha = pulseAlpha),
                            maxLines = 1,
                            softWrap = false,
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = (if (hasError) errorColor else neu.accent).copy(alpha = pulseAlpha),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                state == ConnectionState.CONNECTING -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 60.dp, start = 12.dp)
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDisconnect()
                            },
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(18.dp),
                            color = statusColors.connecting,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = s.connectingTapToCancel,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColors.connecting,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
                state == ConnectionState.CONNECTED -> {
                    val labelAlpha = (dragProgress * 1.4f - 0.4f).coerceIn(0f, 1f)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 60.dp, start = 12.dp)
                            .graphicsLayer { alpha = labelAlpha },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = statusColors.connected.copy(alpha = pulseAlpha),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = s.slideToDisconnect,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp,
                            ),
                            color = statusColors.connected,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        }

        // Draggable / Tappable Thumb
        val currentOffsetPx = offsetX.value

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(currentOffsetPx.roundToInt(), 0)
                }
                .padding(trackPadding)
                .size(thumbSize)
                .neumorphicRaised(
                    shape = thumbShape,
                    cornerRadius = thumbRadius,
                    elevation = 6.dp,
                    lightShadowColor = neu.shadowLight.copy(alpha = if (neu.isDark) 0.4f else 0.95f),
                    darkShadowColor = neu.shadowDark.copy(alpha = if (neu.isDark) 0.8f else 0.55f),
                )
                .clip(thumbShape)
                .background(thumbAccentColor)
                .border(
                    width = 1.2.dp,
                    color = Color.White.copy(alpha = 0.45f),
                    shape = thumbShape,
                )
                .clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    when (state) {
                        ConnectionState.DISCONNECTED -> {
                            coroutineScope.launch {
                                if (maxDragPx > 0f) {
                                    offsetX.animateTo(maxDragPx, spring(stiffness = 400f))
                                }
                                onRequestConnect()
                                kotlinx.coroutines.delay(350)
                                if (state == ConnectionState.DISCONNECTED) {
                                    offsetX.animateTo(0f, spring(stiffness = 400f))
                                }
                            }
                        }
                        ConnectionState.CONNECTED -> {
                            coroutineScope.launch {
                                isDisconnecting = true
                                offsetX.animateTo(0f, spring(stiffness = 400f))
                                onDisconnect()
                                kotlinx.coroutines.delay(400)
                                isDisconnecting = false
                            }
                        }
                        ConnectionState.CONNECTING -> {
                            coroutineScope.launch {
                                offsetX.animateTo(0f, spring(stiffness = 400f))
                                onDisconnect()
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            when {
                isDisconnecting || state == ConnectionState.CONNECTING -> {
                    CircularProgressIndicator(
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                    )
                }
                state == ConnectionState.DISCONNECTED -> {
                    Icon(
                        imageVector = Icons.Filled.PowerSettingsNew,
                        contentDescription = "Slide to connect",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
                state == ConnectionState.CONNECTED -> {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Connected",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

/**
 * Full-screen design-aware atmosphere shared by the two selectable UI skins.
 * Pixel UI uses restrained tonal shapes; the current design remains visually clean.
 */
@Composable
fun StyleAtmospherePosterOverlay(
    modifier: Modifier = Modifier,
) {
    val neu = currentNeumorphicColors
    val style = neu.designStyle
    val accent = neu.accent
    val isDark = neu.isDark

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 4f || h <= 4f) return@Canvas

        when (style) {
            FoxyUiDesignStyle.SAFETY_SURF -> {
                // The reference design uses a vivid blue field with calm, layered connection waves.
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            accent.copy(alpha = if (isDark) 0.26f else 0.18f),
                            Color.Transparent,
                            Color.White.copy(alpha = if (isDark) 0.035f else 0.10f),
                        ),
                    ),
                )
                val center = Offset(w * 0.50f, h * 0.34f)
                listOf(0.24f, 0.38f, 0.52f, 0.68f).forEachIndexed { index, scale ->
                    drawCircle(
                        color = Color.White.copy(alpha = (0.10f - index * 0.018f).coerceAtLeast(0.025f)),
                        radius = w * scale,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.2f),
                    )
                }
            }
            FoxyUiDesignStyle.PIXEL_GOOGLE -> {
                // Pixel UI uses a quiet wallpaper-like tonal atmosphere rather than a poster skin.
                val tonal = accent.copy(alpha = if (isDark) 0.06f else 0.08f)
                drawCircle(
                    color = tonal,
                    radius = (w * 0.72f).coerceAtLeast(120f),
                    center = Offset(w * 0.92f, h * 0.06f),
                )
                drawCircle(
                    color = accent.copy(alpha = if (isDark) 0.035f else 0.05f),
                    radius = (w * 0.52f).coerceAtLeast(96f),
                    center = Offset(w * 0.08f, h * 0.86f),
                )
            }
            FoxyUiDesignStyle.CLAYMORPHISM -> {
                // Keep background clean without full-screen colored tint
            }
            FoxyUiDesignStyle.DARK_LUXURY -> {
                val gold = Color(0xFFE5B84B)
                val step = 64.dp.toPx().coerceAtLeast(32f)
                val lineColor = gold.copy(alpha = 0.03f)
                var x = -h
                while (x < w + h) {
                    drawLine(
                        color = lineColor,
                        start = Offset(x, 0f),
                        end = Offset(x + h, h),
                        strokeWidth = 1f,
                    )
                    drawLine(
                        color = lineColor,
                        start = Offset(x + h, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1f,
                    )
                    x += step
                }
            }
            FoxyUiDesignStyle.NEO_BRUTALISM -> {
                val spacing = 26.dp.toPx().coerceAtLeast(16f)
                val dotColor = if (isDark) Color.White.copy(alpha = 0.07f) else Color(0xFF0B0F19).copy(alpha = 0.07f)
                var y = spacing
                while (y < h) {
                    var x = spacing
                    while (x < w) {
                        drawCircle(
                            color = dotColor,
                            radius = 1.4.dp.toPx(),
                            center = Offset(x, y),
                        )
                        x += spacing
                    }
                    y += spacing
                }
            }
            FoxyUiDesignStyle.BENTO_UI -> {
                val gridStep = 84.dp.toPx().coerceAtLeast(48f)
                val gridColor = if (isDark) Color.White.copy(alpha = 0.025f) else Color.Black.copy(alpha = 0.03f)
                var x = gridStep
                while (x < w) {
                    drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                    x += gridStep
                }
                var y = gridStep
                while (y < h) {
                    drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                    y += gridStep
                }
            }
            FoxyUiDesignStyle.EDITORIAL_DESIGN -> {
                val ruleColor = if (isDark) Color.White.copy(alpha = 0.04f) else Color(0xFF18181B).copy(alpha = 0.05f)
                val margin = 16.dp.toPx()
                drawLine(ruleColor, Offset(margin, 0f), Offset(margin, h), strokeWidth = 1.2f)
                drawLine(ruleColor, Offset(w - margin, 0f), Offset(w - margin, h), strokeWidth = 1.2f)
                drawLine(ruleColor, Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), strokeWidth = 0.8f)
            }
            FoxyUiDesignStyle.NEUMORPHISM -> {
                // Keep background 100% pure & untinted like the Deklik Dark/Light reference screenshots
            }
        }
    }
}

/**
 * Standard Telegram-style Theme & Appearance Icon (Half Sun/Moon + Theme Palette Rays).
 */
@Composable
fun TelegramThemeAppearanceIcon(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = minOf(w, h) * 0.28f

        // Outer ring
        drawCircle(
            color = tint,
            radius = r,
            center = Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = minOf(w, h) * 0.095f),
        )

        // Half-filled crescent/hemisphere inside (Telegram theme/appearance symbol)
        drawArc(
            color = tint,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2f, r * 2f),
        )

        // 8 crisp surrounding rays like Telegram's theme switcher icon
        val rayInner = minOf(w, h) * 0.37f
        val rayOuter = minOf(w, h) * 0.47f
        val strokeW = minOf(w, h) * 0.09f
        for (i in 0 until 8) {
            val angleRad = Math.toRadians((i * 45.0))
            val sx = cx + (kotlin.math.cos(angleRad) * rayInner).toFloat()
            val sy = cy + (kotlin.math.sin(angleRad) * rayInner).toFloat()
            val ex = cx + (kotlin.math.cos(angleRad) * rayOuter).toFloat()
            val ey = cy + (kotlin.math.sin(angleRad) * rayOuter).toFloat()
            drawLine(
                color = tint,
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = strokeW,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
    }
}

/**
 * High-resolution Vector/Canvas Speedometer Gauge Icon matching the user's reference image:
 * - 6 progressive-thickness semicircle gauge segments separated by crisp radial gaps
 * - Tapered high-speed needle pointing upper-right (~65°) with a hollow circular pivot base
 * - Automatically adapts its colors to the active Color Palette (FoxyColorTheme) and Dark/Light mode
 */
@Composable
fun FoxySpeedometerIcon(
    modifier: Modifier = Modifier,
    tint: Color? = null,
    needleTint: Color? = null,
    speedBytesPerSec: Long? = null,
    isTesting: Boolean = false,
) {
    val neu = currentNeumorphicColors
    val statusColors = LocalFoxyStatusColors.current

    val liveStats by com.vauth.foxyvpn.vpn.FoxyVpnService.vpnLiveStats.collectAsState()
    val vpnState by com.vauth.foxyvpn.vpn.FoxyVpnService.state.collectAsState()

    val effectiveSpeedBps = (speedBytesPerSec ?: (liveStats.rxRateBytesPerSec + liveStats.txRateBytesPerSec)).coerceAtLeast(0L)
    val isConnected = vpnState == com.vauth.foxyvpn.data.model.ConnectionState.CONNECTED
    val isConnecting = vpnState == com.vauth.foxyvpn.data.model.ConnectionState.CONNECTING

    // Smooth sweep animation when actively testing ping or negotiating tunnel
    val infiniteTransition = rememberInfiniteTransition(label = "speedometer-sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 200f,
        targetValue = 346f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 680, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "speedometer-sweep-deg",
    )

    // Subtle alive micro-oscillation when connected so the gauge feels responsive even between bursts
    val connectedMicroFlutter by infiniteTransition.animateFloat(
        initialValue = -2.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 920, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "speedometer-flutter",
    )

    // Logarithmic mapping of real speed (0 B/s .. 12 MB/s+) to gauge angle (190° .. 355°)
    // Even light traffic (e.g., 8 KB/s - 150 KB/s) visibly lifts the needle into the mid-zone,
    // while high speed (1 MB/s - 10+ MB/s) drives the needle into the upper-right turbo zone.
    val targetNeedleDeg = when {
        isTesting || isConnecting -> sweepAngle
        effectiveSpeedBps > 0L -> {
            val kbPerSec = effectiveSpeedBps / 1024.0
            val maxKbPerSec = 10_240.0 // 10 MB/s full-scale deflection
            val logNorm = (kotlin.math.ln(1.0 + kbPerSec * 1.8) / kotlin.math.ln(1.0 + maxKbPerSec * 1.8))
                .toFloat()
                .coerceIn(0.06f, 1.0f)
            (196f + logNorm * 158f + connectedMicroFlutter).coerceIn(192f, 355f)
        }
        isConnected -> (208f + connectedMicroFlutter).coerceIn(202f, 216f)
        else -> 192f
    }

    val animatedNeedleDeg by animateFloatAsState(
        targetValue = targetNeedleDeg,
        animationSpec = spring(
            dampingRatio = 0.66f,
            stiffness = 210f,
        ),
        label = "speedometer-needle-deg",
    )

    // Palette-aware & Dark/Light-aware default colors
    val primaryColor = tint ?: neu.accent
    val resolvedNeedleColor = needleTint ?: when {
        isConnected || effectiveSpeedBps > 0L || isTesting -> statusColors.connected
        isConnecting -> statusColors.connecting
        tint != null -> tint
        else -> statusColors.connected
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 2f || h <= 2f) return@Canvas

        // Gauge center is placed near the bottom-center so the semicircle fills the icon frame cleanly
        val cx = w * 0.50f
        val cy = h * 0.78f
        val outerR = minOf(w * 0.46f, h * 0.68f)

        // 7 progressive-thickness gauge arc segments (clockwise from left 178° to right 362° in screen coords)
        data class GaugeSeg(
            val startDeg: Float,
            val endDeg: Float,
            val innerRatioStart: Float,
            val innerRatioEnd: Float,
            val alpha: Float,
        )

        // Dynamic gap around the live needle so the needle cleanly slices through the arc
        val segments = listOf(
            GaugeSeg(178f, 196f, 0.94f, 0.87f, 0.80f),
            GaugeSeg(201f, 226f, 0.86f, 0.80f, 0.85f),
            GaugeSeg(231f, 256f, 0.79f, 0.73f, 0.90f),
            GaugeSeg(261f, 284f, 0.72f, 0.68f, 0.94f),
            GaugeSeg(289f, 312f, 0.67f, 0.64f, 0.97f),
            GaugeSeg(317f, 340f, 0.63f, 0.59f, 1.00f),
            GaugeSeg(345f, 362f, 0.59f, 0.56f, 1.00f),
        )

        val stepsPerSeg = 12
        segments.forEachIndexed { idx, seg ->
            val segPath = Path()
            for (s in 0..stepsPerSeg) {
                val frac = s.toFloat() / stepsPerSeg
                val deg = seg.startDeg + (seg.endDeg - seg.startDeg) * frac
                val rad = Math.toRadians(deg.toDouble())
                val x = cx + (kotlin.math.cos(rad) * outerR).toFloat()
                val y = cy + (kotlin.math.sin(rad) * outerR).toFloat()
                if (s == 0) segPath.moveTo(x, y) else segPath.lineTo(x, y)
            }
            for (s in stepsPerSeg downTo 0) {
                val frac = s.toFloat() / stepsPerSeg
                val deg = seg.startDeg + (seg.endDeg - seg.startDeg) * frac
                val innerRatio = seg.innerRatioStart + (seg.innerRatioEnd - seg.innerRatioStart) * frac
                val innerR = outerR * innerRatio
                val rad = Math.toRadians(deg.toDouble())
                val x = cx + (kotlin.math.cos(rad) * innerR).toFloat()
                val y = cy + (kotlin.math.sin(rad) * innerR).toFloat()
                segPath.lineTo(x, y)
            }
            segPath.close()

            val segMidDeg = (seg.startDeg + seg.endDeg) * 0.5f
            val isLitBySpeed = seg.startDeg <= animatedNeedleDeg + 6f
            val litBoost = if (isLitBySpeed) {
                1f
            } else {
                0.38f
            }
            val baseSegColor = if (idx >= 4 && isLitBySpeed && (effectiveSpeedBps > 64 * 1024L || isTesting)) {
                resolvedNeedleColor
            } else if (segMidDeg <= animatedNeedleDeg && (effectiveSpeedBps > 0L || isConnected || isTesting)) {
                primaryColor
            } else {
                primaryColor
            }
            drawPath(
                path = segPath,
                color = baseSegColor.copy(alpha = (seg.alpha * litBoost).coerceIn(0.24f, 1f)),
            )
        }

        // Tapered Speedometer Needle animated by real speed (animatedNeedleDeg)
        val needleDeg = animatedNeedleDeg.toDouble()
        val needleRad = Math.toRadians(needleDeg)
        val perpRad = needleRad + Math.PI / 2.0

        val pivotX = cx - outerR * 0.02f
        val pivotY = cy - outerR * 0.04f
        val baseRadius = outerR * 0.145f
        val tipRadius = outerR * 0.026f
        val needleLen = outerR * 1.10f

        val tipX = pivotX + (kotlin.math.cos(needleRad) * needleLen).toFloat()
        val tipY = pivotY + (kotlin.math.sin(needleRad) * needleLen).toFloat()

        val cosP = kotlin.math.cos(perpRad).toFloat()
        val sinP = kotlin.math.sin(perpRad).toFloat()
        val cosN = kotlin.math.cos(needleRad).toFloat()
        val sinN = kotlin.math.sin(needleRad).toFloat()

        val outerNeedlePath = Path().apply {
            moveTo(pivotX - cosP * baseRadius, pivotY - sinP * baseRadius)
            lineTo(tipX - cosP * tipRadius, tipY - sinP * tipRadius)
            quadraticTo(
                tipX + cosN * (tipRadius * 1.4f),
                tipY + sinN * (tipRadius * 1.4f),
                tipX + cosP * tipRadius,
                tipY + sinP * tipRadius,
            )
            lineTo(pivotX + cosP * baseRadius, pivotY + sinP * baseRadius)
            val backCtrlDist = baseRadius * 1.42f
            quadraticTo(
                pivotX - cosN * backCtrlDist + cosP * (baseRadius * 0.45f),
                pivotY - sinN * backCtrlDist + sinP * (baseRadius * 0.45f),
                pivotX - cosN * baseRadius,
                pivotY - sinN * baseRadius,
            )
            quadraticTo(
                pivotX - cosN * backCtrlDist - cosP * (baseRadius * 0.45f),
                pivotY - sinN * backCtrlDist - sinP * (baseRadius * 0.45f),
                pivotX - cosP * baseRadius,
                pivotY - sinP * baseRadius,
            )
            close()
        }

        val holeRadius = baseRadius * 0.50f
        val holePath = Path().apply {
            addOval(
                androidx.compose.ui.geometry.Rect(
                    left = pivotX - holeRadius,
                    top = pivotY - holeRadius,
                    right = pivotX + holeRadius,
                    bottom = pivotY + holeRadius,
                )
            )
        }

        val finalNeedlePath = Path().apply {
            op(outerNeedlePath, holePath, androidx.compose.ui.graphics.PathOperation.Difference)
        }

        drawPath(
            path = finalNeedlePath,
            color = resolvedNeedleColor,
        )
    }
}
