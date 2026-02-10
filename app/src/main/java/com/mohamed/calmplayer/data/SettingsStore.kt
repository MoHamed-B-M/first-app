package com.mohamed.calmplayer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeConfig {
    LIGHT, DARK, SYSTEM
}

class SettingsStore(private val context: Context) {
    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val BLOCKED_FOLDERS_KEY = stringSetPreferencesKey("blocked_folders")
    }

    val themeFlow: Flow<ThemeConfig> = context.dataStore.data.map { preferences ->
        val themeName = preferences[THEME_KEY] ?: ThemeConfig.SYSTEM.name
        ThemeConfig.valueOf(themeName)
    }

    val blockedFoldersFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[BLOCKED_FOLDERS_KEY] ?: emptySet()
    }

    suspend fun setTheme(theme: ThemeConfig) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    suspend fun addBlockedFolder(path: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[BLOCKED_FOLDERS_KEY] ?: emptySet()
            preferences[BLOCKED_FOLDERS_KEY] = current + path
        }
    }

    suspend fun removeBlockedFolder(path: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[BLOCKED_FOLDERS_KEY] ?: emptySet()
            preferences[BLOCKED_FOLDERS_KEY] = current - path
        }
    }
}
