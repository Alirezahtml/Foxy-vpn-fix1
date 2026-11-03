package com.vauth.foxyvpn.data

import org.json.JSONObject

/** User-controlled JSON backup. Tokens are sensitive; the UI warns before exporting/sharing. */
object FoxyBackup {
    private const val SCHEMA = 1

    fun create(settings: SettingsStore, tokens: TokenStore): String = JSONObject().apply {
        put("schema", SCHEMA)
        put("createdAtEpochMs", System.currentTimeMillis())
        put("settings", settings.exportBackupJson())
        put("accounts", tokens.exportBackupJson())
    }.toString(2)

    fun restore(json: String, settings: SettingsStore, tokens: TokenStore): RestoreResult {
        val root = JSONObject(json)
        require(root.optInt("schema", -1) == SCHEMA) { "Unsupported backup version" }
        val settingsObject = root.optJSONObject("settings") ?: JSONObject()
        val accountsObject = root.optJSONObject("accounts") ?: JSONObject()
        settings.importBackupJson(settingsObject)
        val importedAccounts = tokens.importBackupJson(accountsObject)
        return RestoreResult(importedAccounts = importedAccounts)
    }

    data class RestoreResult(val importedAccounts: Int)
}
