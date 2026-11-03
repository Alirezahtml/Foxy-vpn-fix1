package com.vauth.foxyvpn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.vauth.foxyvpn.data.AppPaletteRole
import com.vauth.foxyvpn.data.CustomAppPalette
import com.vauth.foxyvpn.ui.theme.AppLanguage
import java.util.Locale

@Composable
fun CustomAppPaletteEditorDialog(
    initialPalette: CustomAppPalette,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onApply: (CustomAppPalette) -> Unit,
) {
    var draft by remember(initialPalette) { mutableStateOf(initialPalette) }
    var editingRole by remember { mutableStateOf<AppPaletteRole?>(null) }
    val role = editingRole
    if (role != null) {
        CustomAccentColorPickerDialog(
            initialArgb = draft.color(role),
            title = roleTitle(role, language),
            description = when (language) {
                AppLanguage.FA -> "رنگ را با کانال‌های RGB تنظیم کنید. تغییر تا زمان اعمال پالت ذخیره نمی‌شود."
                AppLanguage.AR -> "اضبط اللون عبر قنوات RGB. لن يُحفظ التغيير حتى تطبيق اللوحة."
                AppLanguage.EN -> "Adjust the RGB channels. The change is not saved until you apply the palette."
            },
            applyLabel = when (language) {
                AppLanguage.FA -> "ثبت رنگ"
                AppLanguage.AR -> "حفظ اللون"
                AppLanguage.EN -> "Set color"
            },
            cancelLabel = when (language) {
                AppLanguage.FA -> "لغو"
                AppLanguage.AR -> "إلغاء"
                AppLanguage.EN -> "Cancel"
            },
            onDismiss = { editingRole = null },
            onApply = { draft = draft.withColor(role, it); editingRole = null },
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when (language) {
                    AppLanguage.FA -> "ساخت پالت رنگ دلخواه"
                    AppLanguage.AR -> "إنشاء لوحة ألوان مخصصة"
                    AppLanguage.EN -> "Build a custom color palette"
                },
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    when (language) {
                        AppLanguage.FA -> "رنگ‌های اصلی و ثانویه و سطح‌های روشن و تاریک را جداگانه بسازید."
                        AppLanguage.AR -> "خصص اللونين الأساسي والثانوي والأسطح الفاتحة والداكنة كلٌّ على حدة."
                        AppLanguage.EN -> "Set primary and secondary colors plus separate light and dark surfaces."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                AppPaletteRole.entries.forEach { paletteRole ->
                    val color = Color(draft.color(paletteRole))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { editingRole = paletteRole }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color),
                        )
                        Text(
                            text = roleTitle(paletteRole, language),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = String.format(Locale.US, "#%06X", color.toArgb() and 0x00FFFFFF),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(draft) }) {
                Text(when (language) {
                    AppLanguage.FA -> "اعمال پالت"
                    AppLanguage.AR -> "تطبيق اللوحة"
                    AppLanguage.EN -> "Apply palette"
                })
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(when (language) {
                    AppLanguage.FA -> "لغو"
                    AppLanguage.AR -> "إلغاء"
                    AppLanguage.EN -> "Cancel"
                })
            }
        },
    )
}

private fun roleTitle(role: AppPaletteRole, language: AppLanguage): String = when (role) {
    AppPaletteRole.PRIMARY -> when (language) {
        AppLanguage.FA -> "رنگ اصلی"
        AppLanguage.AR -> "اللون الرئيسي"
        AppLanguage.EN -> "Primary"
    }
    AppPaletteRole.SECONDARY -> when (language) {
        AppLanguage.FA -> "رنگ ثانویه"
        AppLanguage.AR -> "اللون الثانوي"
        AppLanguage.EN -> "Secondary"
    }
    AppPaletteRole.LIGHT_BACKGROUND -> when (language) {
        AppLanguage.FA -> "پس‌زمینه روشن"
        AppLanguage.AR -> "خلفية الوضع الفاتح"
        AppLanguage.EN -> "Light background"
    }
    AppPaletteRole.LIGHT_SURFACE -> when (language) {
        AppLanguage.FA -> "سطح روشن"
        AppLanguage.AR -> "سطح الوضع الفاتح"
        AppLanguage.EN -> "Light surface"
    }
    AppPaletteRole.LIGHT_ELEVATED -> when (language) {
        AppLanguage.FA -> "سطح برجسته روشن"
        AppLanguage.AR -> "سطح بارز فاتح"
        AppLanguage.EN -> "Light elevated surface"
    }
    AppPaletteRole.DARK_BACKGROUND -> when (language) {
        AppLanguage.FA -> "پس‌زمینه تاریک"
        AppLanguage.AR -> "خلفية الوضع الداكن"
        AppLanguage.EN -> "Dark background"
    }
    AppPaletteRole.DARK_SURFACE -> when (language) {
        AppLanguage.FA -> "سطح تاریک"
        AppLanguage.AR -> "سطح الوضع الداكن"
        AppLanguage.EN -> "Dark surface"
    }
    AppPaletteRole.DARK_ELEVATED -> when (language) {
        AppLanguage.FA -> "سطح برجسته تاریک"
        AppLanguage.AR -> "سطح بارز داكن"
        AppLanguage.EN -> "Dark elevated surface"
    }
}
