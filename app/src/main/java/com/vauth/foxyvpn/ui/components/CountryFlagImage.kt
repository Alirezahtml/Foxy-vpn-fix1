package com.vauth.foxyvpn.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.R

@Composable
fun CountryFlagImage(
    countryCode: String?,
    modifier: Modifier = Modifier,
    fallbackFontSize: TextUnit = 11.sp,
) {
    if (countryCode.isNullOrBlank()) return
    val resource = when (countryCode?.trim()?.uppercase()) {
        "AR" -> R.drawable.flag_ar
        "AT" -> R.drawable.flag_at
        "AU" -> R.drawable.flag_au
        "BE" -> R.drawable.flag_be
        "BG" -> R.drawable.flag_bg
        "BR" -> R.drawable.flag_br
        "CA" -> R.drawable.flag_ca
        "CL" -> R.drawable.flag_cl
        "CO" -> R.drawable.flag_co
        "DE" -> R.drawable.flag_de
        "DK" -> R.drawable.flag_dk
        "ES" -> R.drawable.flag_es
        "FI" -> R.drawable.flag_fi
        "FR" -> R.drawable.flag_fr
        "GB", "UK" -> R.drawable.flag_gb
        "GH" -> R.drawable.flag_gh
        "IE" -> R.drawable.flag_ie
        "IR", "IRN", "IRAN" -> R.drawable.flag_ir
        "IT" -> R.drawable.flag_it
        "JP" -> R.drawable.flag_jp
        "KR" -> R.drawable.flag_kr
        "MX" -> R.drawable.flag_mx
        "MY" -> R.drawable.flag_my
        "NL" -> R.drawable.flag_nl
        "NO" -> R.drawable.flag_no
        "NZ" -> R.drawable.flag_nz
        "PE" -> R.drawable.flag_pe
        "PH" -> R.drawable.flag_ph
        "PT" -> R.drawable.flag_pt
        "SE" -> R.drawable.flag_se
        "SG" -> R.drawable.flag_sg
        "TH" -> R.drawable.flag_th
        "US" -> R.drawable.flag_us
        "ZA" -> R.drawable.flag_za
        else -> 0
    }
    val shape = RoundedCornerShape(4.dp)
    if (resource != 0) {
        Image(
            painter = painterResource(resource),
            contentDescription = countryCode?.let { "$it flag" } ?: "Country flag",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape),
        )
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = countryCode?.trim()?.uppercase()?.take(2).orEmpty().ifBlank { "—" },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = fallbackFontSize,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}
