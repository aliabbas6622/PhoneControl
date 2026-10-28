package com.azzahid.hof.data.storage

import android.content.Context
import java.io.File

object FileDropStorage {
    private const val DIRECTORY_NAME = "FileDrop"

    fun directory(context: Context): File =
        File(context.getExternalFilesDir(null) ?: context.filesDir, DIRECTORY_NAME)
            .apply { mkdirs() }

    fun sanitizeFileName(raw: String?): String {
        val fallback = "file-${System.currentTimeMillis()}"
        val name = (raw ?: fallback)
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .trim()

        val cleaned = buildString {
            for (character in name) {
                append(
                    if (character.isISOControl() || character in "\\/:*?\"<>|") '_' else character
                )
            }
        }.trim()

        return cleaned
            .takeIf { it.isNotEmpty() && it != "." && it != ".." }
            ?.take(200)
            ?: fallback
    }

    fun uniqueFile(directory: File, fileName: String): File {
        val candidate = File(directory, fileName)
        if (!candidate.exists()) return candidate

        val dotIndex = fileName.lastIndexOf('.')
        val base = if (dotIndex > 0) fileName.substring(0, dotIndex) else fileName
        val extension = if (dotIndex > 0) fileName.substring(dotIndex) else ""

        for (index in 1 until 10_000) {
            val next = File(directory, "$base ($index)$extension")
            if (!next.exists()) return next
        }

        return File(directory, "${System.currentTimeMillis()}-$fileName")
    }

    fun resolve(directory: File, relativePath: String): File? {
        val root = directory.canonicalFile
        val target = File(root, relativePath).canonicalFile
        val contained = target == root || target.path.startsWith(root.path + File.separator)
        return target.takeIf { contained }
    }
}
