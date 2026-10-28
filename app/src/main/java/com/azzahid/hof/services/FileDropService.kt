package com.azzahid.hof.services

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.azzahid.hof.data.storage.FileDropStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileDropService(private val context: Context) {

    fun directory(): File = FileDropStorage.directory(context)

    suspend fun importUris(uris: List<Uri>): List<String> = withContext(Dispatchers.IO) {
        val directory = directory()

        uris.mapNotNull { uri ->
            val displayName = resolveDisplayName(uri)
            val target = FileDropStorage.uniqueFile(
                directory,
                FileDropStorage.sanitizeFileName(displayName)
            )

            val copied = runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                    true
                } ?: false
            }.getOrDefault(false)

            if (copied) target.name else {
                target.delete()
                null
            }
        }
    }

    private fun resolveDisplayName(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index >= 0) cursor.getString(index) else null
                    } else {
                        null
                    }
                }
        }.getOrNull() ?: uri.lastPathSegment
    }
}
