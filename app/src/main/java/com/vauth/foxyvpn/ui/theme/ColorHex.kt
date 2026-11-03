package com.vauth.foxyvpn.ui.theme

import java.util.Locale

/** Parse a user-entered #RRGGBB color as an opaque Android ARGB value. */
internal fun parseOpaqueHexColor(value: String): Long? {
    val hex = value.trim().removePrefix("#")
    if (hex.length != 6 || hex.any { it !in '0'..'9' && it.uppercaseChar() !in 'A'..'F' }) return null
    val rgb = hex.toLongOrNull(16) ?: return null
    return 0xFF000000L or (rgb and 0x00FFFFFFL)
}

internal fun formatOpaqueHexColor(argb: Long): String =
    String.format(Locale.US, "#%06X", argb and 0x00FFFFFFL)
