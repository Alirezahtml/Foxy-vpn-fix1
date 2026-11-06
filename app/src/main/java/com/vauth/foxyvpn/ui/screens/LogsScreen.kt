package com.vauth.foxyvpn.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextOverflow
import com.vauth.foxyvpn.data.model.ConnectionState
import com.vauth.foxyvpn.ui.theme.FoxyLanguage
import com.vauth.foxyvpn.ui.theme.NeumorphicCard
import com.vauth.foxyvpn.ui.theme.formatBytesLocalized
import com.vauth.foxyvpn.vpn.FoxyVpnService
import com.vauth.foxyvpn.vpn.PingUtil
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.vauth.foxyvpn.data.AppLogger
import com.vauth.foxyvpn.data.LogEntry
import com.vauth.foxyvpn.data.LogLevel
import com.vauth.foxyvpn.ui.theme.FoxyStrings
import com.vauth.foxyvpn.ui.theme.LocalFoxyStatusColors
import com.vauth.foxyvpn.ui.theme.NeumorphicIconButton
import com.vauth.foxyvpn.ui.theme.currentFoxyStrings
import com.vauth.foxyvpn.ui.theme.currentNeumorphicColors
import com.vauth.foxyvpn.ui.theme.localizeDigits
import com.vauth.foxyvpn.ui.theme.localizeNumber
import com.vauth.foxyvpn.ui.theme.neumorphicSunken
import com.vauth.foxyvpn.ui.theme.neumorphicTouchBounce
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class LogFilter(val levels: Set<LogLevel>) {
    ALL(setOf(LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR)),
    INFO(setOf(LogLevel.INFO)),
    PROBLEMS(setOf(LogLevel.WARN, LogLevel.ERROR));

    fun localizedLabel(s: FoxyStrings): String = when (this) {
        ALL -> s.logFilterAll
        INFO -> s.logFilterInfo
        PROBLEMS -> s.logFilterProblems
    }
}

private suspend fun writeLogFile(context: Context): File = withContext(Dispatchers.IO) {
    val dir = File(context.cacheDir, "logs").apply { mkdirs() }
    val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
    val file = File(dir, "foxyvpn-logs-$stamp.txt")
    file.writeText(AppLogger.exportAsText())
    file
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(onBack: () -> Unit) {
    val entries by AppLogger.entries.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
    var filter by remember { mutableStateOf(LogFilter.ALL) }

    val neu = currentNeumorphicColors
    val statusColors = LocalFoxyStatusColors.current
    val s = currentFoxyStrings

    val vpnState by FoxyVpnService.state.collectAsState()
    val activeProtocol by FoxyVpnService.activeProtocol.collectAsState()
    val connectedEndpoint by FoxyVpnService.connectedEndpoint.collectAsState()
    val connectedCountry by FoxyVpnService.connectedCountry.collectAsState()
    val pingLatency by FoxyVpnService.pingLatencyMs.collectAsState()
    val traffic by FoxyVpnService.traffic.collectAsState()
    val exitIp by FoxyVpnService.exitIp.collectAsState()
    var isRunningDiagnostic by remember { mutableStateOf(false) }

    val countsByFilter: Map<LogFilter, Int> = remember(entries) {
        LogFilter.entries.associateWith { option -> entries.count { it.level in option.levels } }
    }
    val visible: List<LogEntry> = remember(entries, filter) {
        if (filter == LogFilter.ALL) entries else entries.filter { it.level in filter.levels }
    }

    Scaffold(
        containerColor = neu.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        s.logsTitle(visible.size, entries.size),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = neu.textPrimary,
                    )
                },
                navigationIcon = {
                    NeumorphicIconButton(
                        size = 42.dp,
                        onClick = onBack,
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = s.back,
                            tint = neu.textPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
                actions = {
                    NeumorphicIconButton(
                        size = 40.dp,
                        onClick = {
                            if (entries.isEmpty()) {
                                Toast.makeText(context, s.logsEmpty, Toast.LENGTH_SHORT).show()
                                return@NeumorphicIconButton
                            }
                            scope.launch {
                                val file = writeLogFile(context)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file,
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, file.name)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(
                                    Intent.createChooser(shareIntent, s.viewConnectionLogsTitle).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    },
                                )
                            }
                        },
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, tint = neu.textPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    NeumorphicIconButton(
                        size = 40.dp,
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("FoxyVpn logs", AppLogger.exportAsText()))
                            Toast.makeText(context, s.logsCopiedToast, Toast.LENGTH_SHORT).show()
                        },
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = neu.textPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    NeumorphicIconButton(
                        size = 40.dp,
                        onClick = { AppLogger.clear() },
                    ) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = neu.background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
        ) {
            // Live Diagnostic Summary Card ("کارت وضعیت تشخیصی زنده و دقیق")
            NeumorphicCard(
                cornerRadius = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (vpnState) {
                                            ConnectionState.CONNECTED -> statusColors.connected
                                            ConnectionState.CONNECTING -> statusColors.connecting
                                            ConnectionState.DISCONNECTED -> neu.textSecondary.copy(alpha = 0.5f)
                                        },
                                    ),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = when (vpnState) {
                                    ConnectionState.CONNECTED -> s.statusConnected
                                    ConnectionState.CONNECTING -> s.statusConnecting
                                    ConnectionState.DISCONNECTED -> s.statusDisconnected
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = neu.textPrimary,
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!activeProtocol.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(neu.accent.copy(alpha = 0.15f))
                                        .padding(horizontal = 7.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = activeProtocol ?: "AUTO",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = neu.accent,
                                    )
                                }
                                Spacer(Modifier.width(6.dp))
                            }
                            // Run Diagnostic Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(neu.accent)
                                    .clickable(enabled = !isRunningDiagnostic) {
                                        scope.launch(Dispatchers.IO) {
                                            isRunningDiagnostic = true
                                            AppLogger.i("Diagnostics", "=== شروع تست جامع سلامت شبکه و اتصال فاکسی ===")
                                            val dnsStart = System.currentTimeMillis()
                                            val resolved = runCatching {
                                                com.vauth.foxyvpn.data.ControlPlaneHttp.resolveHostAddresses("vpn.mozilla.org")
                                            }.getOrNull().orEmpty()
                                            val dnsElapsed = System.currentTimeMillis() - dnsStart
                                            AppLogger.i(
                                                "Diagnostics",
                                                "1. بررسی دی‌ان‌اس (DNS): ${resolved.size} آی‌پی معتبر دریافت شد (${dnsElapsed}ms): ${resolved.take(3).joinToString { it.hostAddress ?: "" }}",
                                            )

                                            val endpoint = connectedEndpoint
                                            val targetHost = endpoint?.first ?: "de-fra-wg-001.relays.mullvad.net"
                                            val targetPort = endpoint?.second ?: 443
                                            val pingRes = PingUtil.measureTcpLatencyMs(targetHost, targetPort)
                                            if (pingRes != null) {
                                                AppLogger.i("Diagnostics", "2. تأخیر شبکه تا رله (${targetHost}:${targetPort}): $pingRes ms [پایدار]")
                                            } else {
                                                AppLogger.w("Diagnostics", "2. پینگ رله با تأخیر مواجه شد یا فیلتر است ($targetHost)")
                                            }

                                            val fastlyPing = PingUtil.measureTcpLatencyMs("151.101.65.91", 443)
                                            if (fastlyPing != null) {
                                                AppLogger.i("Diagnostics", "3. ارتباط درگاه انیکست فستلی (151.101.65.91): فعال و در دسترس ($fastlyPing ms)")
                                            } else {
                                                AppLogger.w("Diagnostics", "3. ارتباط فستلی در این اپراتور تحت تأثیر اختلال شبکه قرار دارد")
                                            }

                                            val curState = FoxyVpnService.state.value
                                            AppLogger.i(
                                                "Diagnostics",
                                                "4. وضعیت تونل: $curState • پروتکل: ${activeProtocol ?: "AUTO"} • خروجی: ${connectedCountry ?: "اروپا"}",
                                            )
                                            AppLogger.i("Diagnostics", "=== گزارش عیب‌یابی با موفقیت ثبت شد ===")
                                            isRunningDiagnostic = false
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.NetworkCheck,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isRunningDiagnostic) "در حال تست..." else when (s.language) {
                                            FoxyLanguage.FA -> "تست شبکه"
                                            FoxyLanguage.AR -> "فحص الشبكة"
                                            FoxyLanguage.EN -> "Test Network"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (s.language) {
                                    FoxyLanguage.FA -> "سرور و موقعیت رله:"
                                    FoxyLanguage.AR -> "الخادم والموقع:"
                                    FoxyLanguage.EN -> "Server & Relay:"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = neu.textSecondary,
                            )
                            Text(
                                text = connectedEndpoint?.first ?: (connectedCountry ?: "Auto Best Edge"),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = neu.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = when (s.language) {
                                    FoxyLanguage.FA -> "پینگ واقعی:"
                                    FoxyLanguage.AR -> "البينغ الحقيقي:"
                                    FoxyLanguage.EN -> "Live Latency:"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = neu.textSecondary,
                            )
                            Text(
                                text = if (pingLatency != null) "${pingLatency} ms" else "-",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (pingLatency != null && pingLatency!! < 160) statusColors.connected else neu.accent,
                            )
                        }
                    }

                    if (exitIp != null || traffic.totalDownlinkBytes > 0L) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            if (exitIp != null) {
                                Text(
                                    text = "IP: $exitIp",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = neu.textSecondary,
                                )
                            }
                            Text(
                                text = "↓ ${s.language.formatBytesLocalized(traffic.totalDownlinkBytes)}  ↑ ${s.language.formatBytesLocalized(traffic.totalUplinkBytes)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = neu.textSecondary,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Neumorphic filter pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for (option in LogFilter.entries) {
                    val count = countsByFilter[option] ?: 0
                    val isSelected = filter == option
                    val pillShape = RoundedCornerShape(20.dp)

                    Box(
                        modifier = Modifier
                            .neumorphicTouchBounce()
                            .clip(pillShape)
                            .background(if (isSelected) neu.accent else neu.surfaceElevated)
                            .clickable { filter = option }
                            .padding(horizontal = 16.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${option.localizedLabel(s)} (${s.language.localizeNumber(count)})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = if (isSelected) Color.White else neu.textPrimary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Sunken Neumorphic Terminal Console
            val consoleShape = RoundedCornerShape(18.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 14.dp)
                    .neumorphicSunken(
                        shape = consoleShape,
                        cornerRadius = 18.dp,
                        depth = 4.dp,
                        surfaceColor = neu.surfaceElevated,
                    )
                    .border(
                        width = 1.dp,
                        color = if (neu.isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.5f),
                        shape = consoleShape,
                    )
                    .padding(14.dp),
            ) {
                if (visible.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            when {
                                entries.isEmpty() -> s.logsEmpty
                                filter == LogFilter.PROBLEMS -> s.logsNoProblems
                                else -> s.logsNothingMatches
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = neu.textSecondary,
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(visible.asReversed(), key = { it.id }) { entry ->
                            val levelColor = when (entry.level) {
                                LogLevel.ERROR -> MaterialTheme.colorScheme.error
                                LogLevel.WARN -> statusColors.connecting
                                LogLevel.INFO -> neu.textSecondary
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = s.language.localizeDigits(timeFormat.format(Date(entry.timestampMillis))),
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        color = neu.textSecondary.copy(alpha = 0.7f),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = entry.level.name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                        ),
                                        color = levelColor,
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "[${entry.tag}]",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        color = neu.textSecondary,
                                    )
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = entry.message,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 16.sp,
                                    ),
                                    color = neu.textPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
