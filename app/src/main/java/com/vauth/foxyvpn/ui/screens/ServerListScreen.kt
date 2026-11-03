package com.vauth.foxyvpn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import com.vauth.foxyvpn.data.ProxyStateStore
import com.vauth.foxyvpn.data.RECOMMENDED_COUNTRY_CODE
import com.vauth.foxyvpn.data.ServerListClient
import com.vauth.foxyvpn.data.model.VpnCountry
import com.vauth.foxyvpn.ui.components.CountryFlagImage
import com.vauth.foxyvpn.ui.theme.AppLanguage
import com.vauth.foxyvpn.ui.theme.LocalFoxyStatusColors
import com.vauth.foxyvpn.ui.theme.NeumorphicCard
import com.vauth.foxyvpn.ui.theme.NeumorphicIconButton
import com.vauth.foxyvpn.ui.theme.currentFoxyStrings
import com.vauth.foxyvpn.ui.theme.currentNeumorphicColors
import com.vauth.foxyvpn.ui.theme.formatPingLocalized
import com.vauth.foxyvpn.ui.theme.localizeCountryName
import com.vauth.foxyvpn.ui.theme.localizeDigits
import com.vauth.foxyvpn.ui.theme.neumorphicSunken
import com.vauth.foxyvpn.ui.theme.neumorphicTouchBounce
import com.vauth.foxyvpn.ui.theme.staggeredEntrance
import com.vauth.foxyvpn.vpn.PingUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed class LocationRow {
    data class RecommendedRow(val country: VpnCountry) : LocationRow()
    data class CountryHeader(val country: VpnCountry) : LocationRow()
    data class CityRow(val country: VpnCountry, val cityIndex: Int) : LocationRow()
}

private enum class PingState { PENDING, DONE, FAILED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
    serverListClient: ServerListClient,
    proxyStateStore: ProxyStateStore,
    onServerSelected: () -> Unit,
    onBack: () -> Unit,
) {
    var countries by remember { mutableStateOf<List<VpnCountry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortByPing by remember { mutableStateOf(false) }
    var isTestingAll by remember { mutableStateOf(false) }
    var isRefreshingServers by remember { mutableStateOf(false) }

    val selectedState by proxyStateStore.selectedProxyFlow.collectAsState()
    val pingResults = remember { mutableStateMapOf<String, PingUtil.PingMeasurement>() }
    val pingStates = remember { mutableStateMapOf<String, PingState>() }

    val neu = currentNeumorphicColors
    val statusColors = LocalFoxyStatusColors.current
    val haptics = LocalHapticFeedback.current
    val s = currentFoxyStrings
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val cached = withContext(Dispatchers.IO) { serverListClient.loadCachedCountries() }
        if (cached.isNotEmpty()) {
            countries = cached
            isLoading = false
        }
        runCatching { serverListClient.fetchCountries() }
            .onSuccess { fresh ->
                countries = fresh
                errorMessage = null
            }
            .onFailure {
                if (countries.isEmpty()) {
                    errorMessage = it.message ?: s.noActiveServers
                }
            }
        isLoading = false
    }

    val ordered = remember(countries, sortByPing, pingResults.size) {
        if (!sortByPing) {
            countries.sortedWith(compareBy({ it.code != RECOMMENDED_COUNTRY_CODE }, { it.name }))
        } else {
            countries.sortedWith(compareBy<VpnCountry> { it.code != RECOMMENDED_COUNTRY_CODE }
                .thenBy { country ->
                    country.cities.flatMap { it.servers }
                        .asSequence()
                        .filter { !it.quarantined }
                        .mapNotNull { server ->
                            val target = ServerListClient.defaultConnectTarget(server) ?: return@mapNotNull null
                            val cityCode = country.cities.firstOrNull { city -> city.servers.any { it === server } }?.code ?: return@mapNotNull null
                            pingResults["${country.code}:$cityCode:${target.first}:${target.second}"]?.latencyMs
                        }
                        .minOrNull() ?: Int.MAX_VALUE
                }
                .thenBy { it.name })
        }
    }

    val filteredCountries = remember(ordered, searchQuery) {
        val query = searchQuery.trim()
        if (query.isBlank()) ordered
        else ordered.filter { country ->
            country.name.contains(query, ignoreCase = true) ||
                country.code.contains(query, ignoreCase = true) ||
                country.cities.any { city ->
                    city.name.contains(query, ignoreCase = true) ||
                        city.code.contains(query, ignoreCase = true) ||
                        city.servers.any { server ->
                            if (server.quarantined) false else {
                                server.hostname.contains(query, ignoreCase = true) ||
                                    ServerListClient.defaultConnectTarget(server)?.first
                                        ?.contains(query, ignoreCase = true) == true
                            }
                        }
                }
        }
    }

    val rows = remember(filteredCountries, searchQuery) {
        buildList {
            for (country in filteredCountries) {
                if (country.code == RECOMMENDED_COUNTRY_CODE && searchQuery.isBlank()) {
                    add(LocationRow.RecommendedRow(country))
                    continue
                }
                add(LocationRow.CountryHeader(country))
                val query = searchQuery.trim()
                val countryMatches = query.isNotBlank() &&
                    (country.name.contains(query, ignoreCase = true) || country.code.contains(query, ignoreCase = true))
                val cityIndices = if (query.isBlank() || countryMatches) {
                    country.cities.indices.toList()
                } else {
                    country.cities.indices.filter { index ->
                        val city = country.cities[index]
                        city.name.contains(query, ignoreCase = true) ||
                            city.code.contains(query, ignoreCase = true) ||
                            city.servers.any { server ->
                                !server.quarantined && (
                                    server.hostname.contains(query, ignoreCase = true) ||
                                        ServerListClient.defaultConnectTarget(server)?.first
                                            ?.contains(query, ignoreCase = true) == true
                                    )
                            }
                    }
                }
                val sortedCityIndices = if (sortByPing) {
                    cityIndices.sortedBy { index ->
                        val target = country.cities[index].servers.asSequence()
                            .filter { !it.quarantined }
                            .mapNotNull(ServerListClient::defaultConnectTarget)
                            .firstOrNull()
                        val key = "${country.code}:${country.cities[index].code}:${target?.first}:${target?.second}"
                        pingResults[key]?.latencyMs ?: Int.MAX_VALUE
                    }
                } else cityIndices
                sortedCityIndices.forEach { index -> add(LocationRow.CityRow(country, index)) }
            }
        }
    }

    val availableServerCount = remember(countries) {
        countries.sumOf { country ->
            country.cities.sumOf { city ->
                city.servers.count { server ->
                    !server.quarantined && ServerListClient.defaultConnectTarget(server) != null
                }
            }
        }
    }

    Scaffold(
        containerColor = neu.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        s.locationsTitle,
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = neu.background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Sunken Neumorphic Search Bar
            val searchShape = RoundedCornerShape(18.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(vertical = 8.dp)
                    .neumorphicSunken(
                        shape = searchShape,
                        cornerRadius = 18.dp,
                        depth = 3.dp,
                        surfaceColor = neu.surfaceElevated,
                    )
                    .border(
                        width = 1.dp,
                        color = if (neu.isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.5f),
                        shape = searchShape,
                    )
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = neu.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                s.searchLocationsPlaceholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = neu.textSecondary.copy(alpha = 0.7f),
                            )
                        },
                        singleLine = true,
                        colors = androidx.compose.material3.TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = neu.textPrimary,
                            unfocusedTextColor = neu.textPrimary,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val pingButtonRotation by animateFloatAsState(
                    targetValue = if (isTestingAll) 360f else 0f,
                    animationSpec = tween(520),
                    label = "real-ping-refresh-rotation",
                )
                NeumorphicIconButton(
                    size = 42.dp,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        sortByPing = !sortByPing
                    },
                ) {
                    Icon(
                        Icons.Filled.Tune,
                        contentDescription = when (s.language) {
                            AppLanguage.FA -> "فیلتر و مرتب‌سازی سرورها"
                            AppLanguage.AR -> "تصفية وترتيب الخوادم"
                            AppLanguage.EN -> "Filter and sort servers"
                        },
                        tint = if (sortByPing) neu.accent else neu.textSecondary,
                    )
                }
                Text(
                    text = when (s.language) {
                        AppLanguage.FA -> "${s.language.localizeDigits(availableServerCount.toString())} سرور"
                        AppLanguage.AR -> "${s.language.localizeDigits(availableServerCount.toString())} خادم"
                        AppLanguage.EN -> "$availableServerCount servers"
                    },
                    color = neu.textSecondary,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                )
                IconButton(
                    enabled = !isRefreshingServers && !isTestingAll,
                    onClick = {
                        isRefreshingServers = true
                        scope.launch {
                            runCatching {
                                serverListClient.refreshFromOfficial()
                                countries = withContext(Dispatchers.IO) { serverListClient.loadCachedCountries() }
                            }.onFailure { errorMessage = it.message ?: s.noActiveServers }
                            isRefreshingServers = false
                        }
                    },
                ) {
                    if (isRefreshingServers) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = neu.accent)
                    } else {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = when (s.language) {
                                AppLanguage.FA -> "به‌روزرسانی فهرست سرورها"
                                AppLanguage.AR -> "تحديث قائمة الخوادم"
                                AppLanguage.EN -> "Refresh server list"
                            },
                            tint = neu.accent,
                        )
                    }
                }
                Text(
                    text = if (sortByPing) "${s.latencyLabel} • ${s.language.localizeDigits("LOW → HIGH")}"
                    else when (s.language) {
                        AppLanguage.FA -> "مرتب‌سازی و فیلتر"
                        AppLanguage.AR -> "الترتيب والتصفية"
                        AppLanguage.EN -> "Sort and filter"
                    },
                    color = if (sortByPing) neu.accent else neu.textSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    enabled = !isTestingAll,
                    onClick = {
                        isTestingAll = true
                        pingResults.clear()
                        pingStates.clear()
                        scope.launch {
                            val targets = countries.flatMap { country ->
                                country.cities.flatMap { city ->
                                    city.servers.filter { !it.quarantined }.mapNotNull { server ->
                                        ServerListClient.defaultConnectTarget(server)?.let { target -> Triple(country.code, city.code, target) }
                                    }
                                }
                            }.distinctBy { "${it.first}:${it.second}:${it.third.first}:${it.third.second}" }.take(24)
                            // Four concurrent probes improve responsiveness without flooding
                            // low-end devices or the user's network with 24 sockets at once.
                            targets.chunked(4).forEach { batch ->
                                batch.map { (countryCode, cityCode, target) ->
                                    async(Dispatchers.IO) {
                                        val measurement = PingUtil.measureServerLatency(target.first, target.second)
                                        val key = "$countryCode:$cityCode:${target.first}:${target.second}"
                                        key to measurement
                                    }
                                }.awaitAll().forEach { (key, measurement) ->
                                    if (measurement != null) {
                                        pingResults[key] = measurement
                                        pingStates[key] = PingState.DONE
                                    } else {
                                        pingStates[key] = PingState.FAILED
                                    }
                                }
                            }
                            sortByPing = true
                            isTestingAll = false
                        }
                    },
                ) {
                    if (isTestingAll) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = neu.accent)
                    else Icon(
                        painter = painterResource(com.vauth.foxyvpn.R.drawable.ic_real_ping),
                        contentDescription = when (s.language) {
                            AppLanguage.FA -> "تست پینگ واقعی همه سرورها"
                            AppLanguage.AR -> "اختبار زمن الوصول الحقيقي للخوادم"
                            AppLanguage.EN -> "Test real latency for all servers"
                        },
                        tint = neu.accent,
                        modifier = Modifier.size(22.dp).graphicsLayer { rotationZ = pingButtonRotation },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .weight(1f),
            ) {
                when {
                    isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = neu.accent,
                    )
                    errorMessage != null && countries.isEmpty() -> Text(
                        text = errorMessage.orEmpty(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(24.dp),
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 6.dp,
                            vertical = 10.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            rows,
                            key = { row ->
                                when (row) {
                                    is LocationRow.RecommendedRow -> "recommended:${row.country.code}"
                                    is LocationRow.CountryHeader -> "country:${row.country.code}"
                                    is LocationRow.CityRow ->
                                        "city:${row.country.code}:${row.country.cities[row.cityIndex].code}"
                                }
                            },
                        ) { row ->
                            val selected = selectedState
                            when (row) {
                                is LocationRow.RecommendedRow -> {
                                    val isSelected = row.country.code == selected?.countryCode
                                    NeumorphicCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        cornerRadius = 18.dp,
                                        onClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val candidates = ServerListClient.candidatesForCountry(countries, row.country.code)
                                            val chosen = candidates.minByOrNull { candidate ->
                                                val key = "${row.country.code}:${candidate.cityCode}:${candidate.host}:${candidate.port}"
                                                pingResults[key]?.latencyMs ?: Int.MAX_VALUE
                                            } ?: candidates.firstOrNull()
                                            if (chosen != null) {
                                                proxyStateStore.save(chosen)
                                                onServerSelected()
                                            } else {
                                                errorMessage = s.noActiveServers
                                            }
                                        },
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(18.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(neu.accent.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                com.vauth.foxyvpn.ui.theme.FoxySpeedometerIcon(
                                                    tint = neu.accent,
                                                    needleTint = if (isSelected) statusColors.connected else neu.textPrimary,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            }
                                            Spacer(Modifier.width(14.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    s.recommendedFastest,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = neu.textPrimary,
                                                )
                                                Spacer(Modifier.height(3.dp))
                                                Text(
                                                    s.recommendedSubtitle,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = neu.textSecondary,
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Filled.CheckCircle,
                                                    contentDescription = null,
                                                    tint = statusColors.connected,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                                is LocationRow.CountryHeader -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp, bottom = 4.dp, start = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        CountryFlagImage(
                                            countryCode = row.country.code,
                                            modifier = Modifier.size(width = 28.dp, height = 21.dp),
                                            fallbackFontSize = 9.sp,
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = s.language.localizeCountryName(row.country.name, row.country.code),
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp,
                                            ),
                                            color = neu.textSecondary,
                                        )
                                    }
                                }
                                is LocationRow.CityRow -> {
                                    val city = row.country.cities[row.cityIndex]
                                    val usableServers = city.servers.filter { server ->
                                        !server.quarantined && ServerListClient.defaultConnectTarget(server) != null
                                    }
                                    val query = searchQuery.trim()
                                    val searchedServer = if (query.isBlank()) null else usableServers.firstOrNull { server ->
                                        server.hostname.contains(query, ignoreCase = true) ||
                                            ServerListClient.defaultConnectTarget(server)?.first
                                                ?.contains(query, ignoreCase = true) == true
                                    }
                                    val displayServer = searchedServer ?: usableServers.firstOrNull()
                                    val displayTarget = displayServer?.let { ServerListClient.defaultConnectTarget(it) }
                                    val serverCount = usableServers.size
                                    val isSelected = row.country.code == selected?.countryCode && city.code == selected?.cityCode

                                    val pingKey = "${row.country.code}:${city.code}:${displayTarget?.first}:${displayTarget?.second}"
                                    val pingMeasurement = pingResults[pingKey]
                                    val pingState = pingStates[pingKey] ?: PingState.PENDING

                                    LaunchedEffect(pingKey) {
                                        if (pingStates.containsKey(pingKey)) return@LaunchedEffect
                                        if (displayTarget == null) {
                                            pingStates[pingKey] = PingState.FAILED
                                            return@LaunchedEffect
                                        }
                                        val result = PingUtil.measureServerLatency(displayTarget.first, displayTarget.second)
                                        if (result != null) {
                                            pingResults[pingKey] = result
                                            pingStates[pingKey] = PingState.DONE
                                        } else {
                                            pingStates[pingKey] = PingState.FAILED
                                        }
                                    }

                                    NeumorphicCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 12.dp)
                                            .staggeredEntrance(row.cityIndex % 8, delayPerItemMs = 35)
                                            .neumorphicTouchBounce(),
                                        cornerRadius = 16.dp,
                                        onClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val candidates = ServerListClient.candidatesForCity(countries, row.country.code, city.code)
                                            val chosen = candidates.firstOrNull { candidate ->
                                                query.isNotBlank() && candidate.host.contains(query, ignoreCase = true)
                                            } ?: candidates.firstOrNull { candidate ->
                                                candidate.host == displayTarget?.first
                                            } ?: candidates.firstOrNull()
                                            if (chosen != null) {
                                                proxyStateStore.save(chosen)
                                                onServerSelected()
                                            } else {
                                                errorMessage = s.noActiveServers
                                            }
                                        },
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 18.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = s.language.localizeDigits(city.code),
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = neu.textPrimary,
                                                )
                                                val primaryHostname = displayTarget?.first
                                                val serverLabel = when {
                                                    primaryHostname == null -> s.noActiveServers
                                                    serverCount > 1 -> "${s.language.localizeDigits(primaryHostname)} (${s.extraEdgesSuffix(serverCount - 1)})"
                                                    else -> s.language.localizeDigits(primaryHostname)
                                                }
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = serverLabel,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = neu.textSecondary,
                                                )
                                            }

                                            // Ping indicator badge with explicit test method (ICMP / TCP / TLS)
                                            val pingText = when {
                                                pingMeasurement != null -> "${pingMeasurement.method} • ${s.language.formatPingLocalized(pingMeasurement.latencyMs)}"
                                                pingState == PingState.FAILED -> "\u2014"
                                                else -> "\u2022\u2022\u2022"
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(neu.surfaceElevated)
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                            ) {
                                                AnimatedContent(
                                                    targetState = pingText,
                                                    transitionSpec = { fadeIn(tween(140)).togetherWith(fadeOut(tween(100))) },
                                                    label = "real-ping-result-transition",
                                                ) { displayedPing ->
                                                    Text(
                                                        text = displayedPing,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = if (pingMeasurement != null) statusColors.connected else neu.textSecondary,
                                                        maxLines = 1,
                                                    )
                                                }
                                            }

                                            if (isSelected) {
                                                Spacer(Modifier.width(10.dp))
                                                Icon(
                                                    Icons.Filled.CheckCircle,
                                                    contentDescription = null,
                                                    tint = statusColors.connected,
                                                    modifier = Modifier.size(22.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
