package com.example.cloudbackupapp

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

data class CloudConfig(
    val treeUri: Uri?,
    val label: String
)

class SettingsStore(private val appContext: Context) {

    private object Keys {
        val CLOUD_TREE_URI = stringPreferencesKey("cloud_tree_uri")
        val CLOUD_LABEL = stringPreferencesKey("cloud_label")
    }

    val cloudConfig: Flow<CloudConfig> = appContext.dataStore.data.map { prefs ->
        val uri = prefs[Keys.CLOUD_TREE_URI]?.let { runCatching { Uri.parse(it) }.getOrNull() }
        CloudConfig(
            treeUri = uri,
            label = prefs[Keys.CLOUD_LABEL] ?: "Backup Folder"
        )
    }

    suspend fun setCloudTree(uri: Uri, label: String = "Backup Folder") {
        appContext.dataStore.edit { prefs: Preferences.MutablePreferences ->
            prefs[Keys.CLOUD_TREE_URI] = uri.toString()
            prefs[Keys.CLOUD_LABEL] = label
        }
    }
}
