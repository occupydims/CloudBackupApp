package com.example.cloudbackupapp

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Simple "cloud" abstraction using Storage Access Framework.
 * User picks a folder (can be Google Drive or Dropbox provider), we persist the tree URI,
 * and then browse/read files from it.
 */
class CloudDocRepo(private val context: Context) {

    fun rootFromTree(treeUri: Uri?): DocumentFile? {
        if (treeUri == null) return null
        return DocumentFile.fromTreeUri(context, treeUri)
    }

    suspend fun ensureAppFolders(root: DocumentFile) = withContext(Dispatchers.IO) {
        // Create category folders if missing
        listOf("Photos", "Videos", "SMS", "CallLogs", "Contacts").forEach { name ->
            root.findFile(name) ?: root.createDirectory(name)
        }
    }

    suspend fun listDirs(dir: DocumentFile): List<DocumentFile> = withContext(Dispatchers.IO) {
        dir.listFiles().filter { it.isDirectory }.sortedBy { it.name?.lowercase() ?: "" }
    }

    suspend fun listFiles(dir: DocumentFile): List<DocumentFile> = withContext(Dispatchers.IO) {
        dir.listFiles().filter { it.isFile }
    }
}
