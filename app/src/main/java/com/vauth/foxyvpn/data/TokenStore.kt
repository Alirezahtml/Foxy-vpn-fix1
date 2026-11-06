package com.vauth.foxyvpn.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.vauth.foxyvpn.data.model.ProxyPass
import com.vauth.foxyvpn.data.model.RuntimeAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "TokenStore"

data class SavedFoxyAccount(
    val email: String,
    val accessToken: String,
    val refreshToken: String?,
    val expiresAtEpochSeconds: Long,
    val quotaRemaining: Long? = null,
    val quotaMax: Long? = null,
    val isUnlimited: Boolean = false,
    val lastUpdatedEpochSeconds: Long = System.currentTimeMillis() / 1000,
) {
    val usedPercent: Int
        get() {
            if (isUnlimited) return 0
            val max = quotaMax ?: return 0
            val rem = quotaRemaining ?: return 0
            if (max <= 0L) return 0
            val used = (max - rem).coerceAtLeast(0L)
            return ((used.toDouble() / max.toDouble()) * 100.0).toInt().coerceIn(0, 100)
        }
}

class TokenStore(context: Context) {

    private val prefs: SharedPreferences = createSafeEncryptedPrefs(context.applicationContext, "foxyvpn_tokens")
    val accountsRevision: StateFlow<Int> get() = Companion.accountsRevision

    var activeAccountEmail: String?
        get() = runCatching { prefs.getString(KEY_ACTIVE_EMAIL, null)?.takeIf { it.isNotBlank() } }.getOrNull()
        set(value) {
            runCatching {
                if (!value.isNullOrBlank()) {
                    prefs.edit().putString(KEY_ACTIVE_EMAIL, value.trim().lowercase()).apply()
                } else {
                    prefs.edit().remove(KEY_ACTIVE_EMAIL).apply()
                }
                _accountsRevision.value++
            }
        }

    var pendingAccountEmail: String?
        get() = runCatching { prefs.getString(KEY_PENDING_EMAIL, null)?.takeIf { it.isNotBlank() } }.getOrNull()
        set(value) {
            runCatching {
                if (!value.isNullOrBlank()) {
                    prefs.edit().putString(KEY_PENDING_EMAIL, value.trim().lowercase()).apply()
                } else {
                    prefs.edit().remove(KEY_PENDING_EMAIL).apply()
                }
            }
        }

    fun loadEmail(): String? = activeAccountEmail

    fun saveAuth(email: String, auth: RuntimeAuth) = saveAuth(auth = auth, emailHint = email)

    fun saveAuth(auth: RuntimeAuth, emailHint: String? = null) {
        runCatching {
            val resolvedEmail = (emailHint ?: pendingAccountEmail ?: activeAccountEmail ?: "account@foxyvpn.invalid")
                .trim()
                .lowercase()
            prefs.edit()
                .putString(KEY_ACCESS_TOKEN, auth.accessToken)
                .putString(KEY_REFRESH_TOKEN, auth.refreshToken)
                .putLong(KEY_EXPIRES_AT, auth.expiresAtEpochSeconds)
                .putString(KEY_SESSION_TOKEN, auth.sessionToken)
                .putString(KEY_ACTIVE_EMAIL, resolvedEmail)
                .apply()
            upsertAccountInternal(
                SavedFoxyAccount(
                    email = resolvedEmail,
                    accessToken = auth.accessToken,
                    refreshToken = auth.refreshToken,
                    expiresAtEpochSeconds = auth.expiresAtEpochSeconds,
                )
            )
            _accountsRevision.value++
        }.onFailure {
            AppLogger.w(TAG, "could not persist session", it)
        }
    }

    fun loadAuth(): RuntimeAuth? = runCatching {
        val access = prefs.getString(KEY_ACCESS_TOKEN, null)?.takeIf { it.isNotBlank() } ?: return@runCatching null
        val refresh = prefs.getString(KEY_REFRESH_TOKEN, null)?.takeIf { it.isNotBlank() }
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        val session = prefs.getString(KEY_SESSION_TOKEN, null)?.takeIf { it.isNotBlank() }
        RuntimeAuth(
            accessToken = access,
            refreshToken = refresh,
            expiresAtEpochSeconds = expiresAt,
            sessionToken = session,
        )
    }.onFailure {
        AppLogger.w(TAG, "could not read the stored session", it)
    }.getOrNull()

    fun loadAccounts(): List<SavedFoxyAccount> = runCatching {
        val raw = prefs.getString(KEY_ACCOUNTS_JSON, null)
        val parsed = mutableListOf<SavedFoxyAccount>()
        if (!raw.isNullOrBlank()) {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val email = obj.optString("email", "").trim().lowercase()
                val access = obj.optString("accessToken", "")
                if (email.isBlank() || access.isBlank()) continue
                val refresh = obj.optString("refreshToken", "").ifBlank { null }
                val expiresAt = obj.optLong("expiresAt", 0L)
                val qRem = if (obj.has("quotaRemaining")) obj.optLong("quotaRemaining", -1L).takeIf { it >= 0L } else null
                val qMax = if (obj.has("quotaMax")) obj.optLong("quotaMax", -1L).takeIf { it >= 0L } else null
                val unlimited = obj.optBoolean("isUnlimited", false)
                val updated = obj.optLong("lastUpdated", 0L)
                parsed.add(
                    SavedFoxyAccount(
                        email = email,
                        accessToken = access,
                        refreshToken = refresh,
                        expiresAtEpochSeconds = expiresAt,
                        quotaRemaining = qRem,
                        quotaMax = qMax,
                        isUnlimited = unlimited,
                        lastUpdatedEpochSeconds = updated,
                    )
                )
            }
        }
        // Migrate existing single-account session if not yet in list
        val currentAuth = loadAuth()
        if (currentAuth != null && parsed.isEmpty()) {
            val fallbackEmail = activeAccountEmail ?: "account@foxyvpn.invalid"
            val migrated = SavedFoxyAccount(
                email = fallbackEmail,
                accessToken = currentAuth.accessToken,
                refreshToken = currentAuth.refreshToken,
                expiresAtEpochSeconds = currentAuth.expiresAtEpochSeconds,
            )
            parsed.add(migrated)
            saveAccountsListInternal(parsed)
        }
        parsed
    }.getOrDefault(emptyList())

    private fun saveAccountsListInternal(accounts: List<SavedFoxyAccount>) {
        val arr = JSONArray()
        accounts.forEach { acc ->
            val obj = JSONObject().apply {
                put("email", acc.email)
                put("accessToken", acc.accessToken)
                if (acc.refreshToken != null) put("refreshToken", acc.refreshToken)
                put("expiresAt", acc.expiresAtEpochSeconds)
                if (acc.quotaRemaining != null) put("quotaRemaining", acc.quotaRemaining)
                if (acc.quotaMax != null) put("quotaMax", acc.quotaMax)
                put("isUnlimited", acc.isUnlimited)
                put("lastUpdated", acc.lastUpdatedEpochSeconds)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_ACCOUNTS_JSON, arr.toString()).apply()
    }

    private fun upsertAccountInternal(account: SavedFoxyAccount) {
        val current = loadAccounts().toMutableList()
        val idx = current.indexOfFirst { it.email.equals(account.email, ignoreCase = true) }
        if (idx >= 0) {
            val existing = current[idx]
            current[idx] = account.copy(
                quotaRemaining = account.quotaRemaining ?: existing.quotaRemaining,
                quotaMax = account.quotaMax ?: existing.quotaMax,
                isUnlimited = account.isUnlimited || existing.isUnlimited,
            )
        } else {
            current.add(account)
        }
        saveAccountsListInternal(current)
    }

    fun updateActiveAccountQuota(
        remaining: Long?,
        max: Long?,
        unlimited: Boolean,
    ) = updateActiveAccountQuota(
        emailHint = null,
        quotaRemaining = remaining,
        quotaMax = max,
        isUnlimited = unlimited,
    )

    fun updateActiveAccountQuota(
        emailHint: String? = null,
        quotaRemaining: Long?,
        quotaMax: Long?,
        isUnlimited: Boolean,
    ) {
        runCatching {
            val targetEmail = (emailHint ?: activeAccountEmail)?.trim()?.lowercase() ?: return
            if (!emailHint.isNullOrBlank()) {
                activeAccountEmail = targetEmail
            }
            val current = loadAccounts().toMutableList()
            val idx = current.indexOfFirst { it.email.equals(targetEmail, ignoreCase = true) }
            if (idx >= 0) {
                current[idx] = current[idx].copy(
                    quotaRemaining = quotaRemaining,
                    quotaMax = quotaMax,
                    isUnlimited = isUnlimited,
                    lastUpdatedEpochSeconds = System.currentTimeMillis() / 1000,
                )
                saveAccountsListInternal(current)
            } else {
                val auth = loadAuth() ?: return
                current.add(
                    SavedFoxyAccount(
                        email = targetEmail,
                        accessToken = auth.accessToken,
                        refreshToken = auth.refreshToken,
                        expiresAtEpochSeconds = auth.expiresAtEpochSeconds,
                        quotaRemaining = quotaRemaining,
                        quotaMax = quotaMax,
                        isUnlimited = isUnlimited,
                    )
                )
                saveAccountsListInternal(current)
            }
            _accountsRevision.value++
        }
    }

    fun switchToAccount(email: String): Boolean = runCatching {
        val target = loadAccounts().firstOrNull { it.email.equals(email.trim(), ignoreCase = true) } ?: return false
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, target.accessToken)
            .putString(KEY_REFRESH_TOKEN, target.refreshToken)
            .putLong(KEY_EXPIRES_AT, target.expiresAtEpochSeconds)
            .putString(KEY_ACTIVE_EMAIL, target.email)
            .apply()
        clearProxyPass()
        _accountsRevision.value++
        true
    }.getOrDefault(false)

    fun removeAccount(email: String) {
        runCatching {
            val clean = email.trim().lowercase()
            val remaining = loadAccounts().filterNot { it.email.equals(clean, ignoreCase = true) }
            saveAccountsListInternal(remaining)
            if (activeAccountEmail.equals(clean, ignoreCase = true)) {
                val next = remaining.firstOrNull()
                if (next != null) {
                    switchToAccount(next.email)
                } else {
                    clear()
                }
            } else {
                _accountsRevision.value++
            }
        }
    }

    fun findHealthyFailoverAccount(thresholdPercent: Int): SavedFoxyAccount? {
        val active = activeAccountEmail?.lowercase()
        return loadAccounts().firstOrNull { acc ->
            !acc.email.equals(active, ignoreCase = true) &&
                (acc.isUnlimited || acc.quotaMax == null || acc.usedPercent < thresholdPercent)
        }
    }

    fun hasValidAccessToken(): Boolean {
        val auth = loadAuth() ?: return false
        if (auth.expiresAtEpochSeconds <= 0L) return false
        val nowSeconds = System.currentTimeMillis() / 1000
        return auth.expiresAtEpochSeconds - nowSeconds > CLOCK_SKEW_TOLERANCE_SECONDS
    }

    fun hasStoredSession(): Boolean = loadAuth() != null

    fun hasRefreshToken(): Boolean = loadAuth()?.refreshToken != null

    var pendingSessionToken: String?
        get() = runCatching { prefs.getString(KEY_PENDING_SESSION_TOKEN, null)?.takeIf { it.isNotBlank() } }.getOrNull()
        set(value) {
            runCatching {
                if (value != null) {
                    prefs.edit().putString(KEY_PENDING_SESSION_TOKEN, value).apply()
                } else {
                    prefs.edit().remove(KEY_PENDING_SESSION_TOKEN).apply()
                }
            }
        }

    fun clear() {
        runCatching {
            prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_EXPIRES_AT)
                .remove(KEY_PENDING_SESSION_TOKEN)
                .remove(KEY_ACTIVE_EMAIL)
                .remove(KEY_PENDING_EMAIL)
                .apply()
            _accountsRevision.value++
        }
    }

    fun exportBackupJson(): JSONObject = JSONObject().apply {
        put("activeEmail", activeAccountEmail ?: JSONObject.NULL)
        val accountsJson = JSONArray()
        loadAccounts().forEach { account ->
            accountsJson.put(JSONObject().apply {
                put("email", account.email)
                put("accessToken", account.accessToken)
                if (account.refreshToken != null) put("refreshToken", account.refreshToken)
                put("expiresAt", account.expiresAtEpochSeconds)
                account.quotaRemaining?.let { put("quotaRemaining", it) }
                account.quotaMax?.let { put("quotaMax", it) }
                put("isUnlimited", account.isUnlimited)
                put("lastUpdated", account.lastUpdatedEpochSeconds)
            })
        }
        put("accounts", accountsJson)
    }

    fun importBackupJson(source: JSONObject): Int = runCatching {
        val imported = mutableListOf<SavedFoxyAccount>()
        val accounts = source.optJSONArray("accounts") ?: JSONArray()
        for (i in 0 until accounts.length()) {
            val item = accounts.optJSONObject(i) ?: continue
            val email = item.optString("email", "").trim().lowercase()
            val access = item.optString("accessToken", "")
            if (email.isBlank() || access.isBlank()) continue
            imported += SavedFoxyAccount(
                email = email,
                accessToken = access,
                refreshToken = item.optString("refreshToken", "").ifBlank { null },
                expiresAtEpochSeconds = item.optLong("expiresAt", 0L),
                quotaRemaining = item.optLong("quotaRemaining", -1L).takeIf { it >= 0L },
                quotaMax = item.optLong("quotaMax", -1L).takeIf { it >= 0L },
                isUnlimited = item.optBoolean("isUnlimited", false),
                lastUpdatedEpochSeconds = item.optLong("lastUpdated", 0L),
            )
        }
        if (imported.isNotEmpty()) {
            val merged = (loadAccounts() + imported).distinctBy { it.email.lowercase() }
            saveAccountsListInternal(merged)
            val active = source.optString("activeEmail", "").trim().lowercase()
            if (active.isNotBlank()) switchToAccount(active)
            _accountsRevision.value++
        }
        imported.size
    }.getOrDefault(0)

    fun saveProxyPass(pass: ProxyPass) {
        runCatching {
            prefs.edit()
                .putString(KEY_PROXY_PASS_TOKEN, pass.token)
                .putString(KEY_PROXY_PASS_EXPIRES_AT, pass.expiresAt)
                .putLong(KEY_PROXY_PASS_EXPIRES_EPOCH, pass.expiresEpochSeconds)
                .apply()
        }
    }

    fun loadProxyPass(): ProxyPass? {
        val token = prefs.getString(KEY_PROXY_PASS_TOKEN, null)?.takeIf { it.isNotBlank() } ?: return null
        val expiresEpoch = prefs.getLong(KEY_PROXY_PASS_EXPIRES_EPOCH, 0L)
        val expiresAt = prefs.getString(KEY_PROXY_PASS_EXPIRES_AT, null) ?: ""
        return ProxyPass(
            token = token,
            expiresAt = expiresAt,
            expiresEpochSeconds = expiresEpoch,
        )
    }

    fun clearProxyPass() {
        runCatching {
            prefs.edit()
                .remove(KEY_PROXY_PASS_TOKEN)
                .remove(KEY_PROXY_PASS_EXPIRES_AT)
                .remove(KEY_PROXY_PASS_EXPIRES_EPOCH)
                .apply()
        }
    }

    companion object {
        private const val TAG = "TokenStore"
        private val _accountsRevision = MutableStateFlow(0)
        val accountsRevision: StateFlow<Int> = _accountsRevision

        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_PENDING_SESSION_TOKEN = "pending_session_token"
        private const val KEY_ACTIVE_EMAIL = "active_account_email"
        private const val KEY_PENDING_EMAIL = "pending_account_email"
        private const val KEY_ACCOUNTS_JSON = "saved_accounts_json"
        private const val KEY_PROXY_PASS_TOKEN = "cached_proxy_pass_token"
        private const val KEY_PROXY_PASS_EXPIRES_AT = "cached_proxy_pass_expires_at"
        private const val KEY_PROXY_PASS_EXPIRES_EPOCH = "cached_proxy_pass_expires_epoch"

        private const val CLOCK_SKEW_TOLERANCE_SECONDS = 60L
    }
}

internal fun createSafeEncryptedPrefs(context: Context, fileName: String): SharedPreferences {
    val firstAttempt = runCatching {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            fileName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }
    if (firstAttempt.isSuccess) return firstAttempt.getOrThrow()

    runCatching { context.deleteSharedPreferences(fileName) }
    val secondAttempt = runCatching {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            fileName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }
    return secondAttempt.getOrElse {
        context.getSharedPreferences("${fileName}_fallback", Context.MODE_PRIVATE)
    }
}
