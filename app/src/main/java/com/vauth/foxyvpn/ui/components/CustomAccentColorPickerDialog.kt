package com.vauth.foxyvpn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CustomAccentColorPickerDialog(
    initialArgb: Long,
    title: String,
    description: String,
    applyLabel: String,
    cancelLabel: String,
    onDismiss: () -> Unit,
    onApply: (Long) -> Unit,
) {
    val initial = Color(initialArgb)
    var red by remember(initialArgb) { mutableFloatStateOf(initial.red * 255f) }
    var green by remember(initialArgb) { mutableFloatStateOf(initial.green * 255f) }
    var blue by remember(initialArgb) { mutableFloatStateOf(initial.blue * 255f) }
    val preview = Color(red / 255f, green / 255f, blue / 255f, 1f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(description)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(preview),
                )
                ColorChannelSlider("R", red, Color(0xFFE53935)) { red = it }
                ColorChannelSlider("G", green, Color(0xFF43A047)) { green = it }
                ColorChannelSlider("B", blue, Color(0xFF1E88E5)) { blue = it }
                Spacer(Modifier.height(2.dp))
                Text(String.format(Locale.US, "#%06X", preview.toArgb() and 0x00FFFFFF))
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(preview.toArgb().toLong() and 0xFFFFFFFFL) }) {
                Text(applyLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(cancelLabel) }
        },
    )
}

@Composable
private fun ColorChannelSlider(
    label: String,
    value: Float,
    tint: Color,
    onValueChange: (Float) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.size(width = 18.dp, height = 20.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..255f,
            steps = 254,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = tint, activeTrackColor = tint),
        )
        Text(value.roundToInt().toString().padStart(3, '0'), modifier = Modifier.size(width = 30.dp, height = 20.dp))
    }
}
