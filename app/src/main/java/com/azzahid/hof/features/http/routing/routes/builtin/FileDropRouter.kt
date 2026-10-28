package com.azzahid.hof.features.http.routing.routes.builtin

import com.azzahid.hof.data.storage.FileDropStorage
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.features.http.androidContext
import com.azzahid.hof.features.http.utils.DirectoryListingGenerator
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.defaultForFile
import io.ktor.server.http.content.LocalFileContent
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.yield
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

private const val MAX_UPLOAD_BYTES = 2L * 1024 * 1024 * 1024

@Serializable
private data class FileDropEntry(
    val name: String,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    val url: String
)

@Serializable
private data class FileDropListing(
    val success: Boolean = true,
    val path: String,
    val files: List<FileDropEntry>
)

internal fun io.ktor.server.routing.Route.addFileDropRoute(route: Route) {
    route(route.path, {
        description = route.description
    }) {
        get("/{path...}") {
            val context = call.application.androidContext
            val directory = FileDropStorage.directory(context)
            val relativePath = call.parameters.getAll("path")?.joinToString("/") ?: ""
            val target = FileDropStorage.resolve(directory, relativePath)

            if (target == null) {
                call.respond(HttpStatusCode.Forbidden, "Invalid path")
                return@get
            }

            when {
                !target.exists() -> call.respond(HttpStatusCode.NotFound, "Path not found")

                target.isFile -> {
                    call.response.headers.append(
                        HttpHeaders.CacheControl,
                        CacheControl.NoCache(null).toString()
                    )
                    call.respond(
                        LocalFileContent(target, ContentType.defaultForFile(target))
                    )
                }

                call.request.queryParameters["json"] == "1" -> {
                    respondFileDropListing(call, route.path, target, relativePath, asJson = true)
                }

                else -> {
                    respondFileDropListing(call, route.path, target, relativePath, asJson = false)
                }
            }
        }

        post("/{path...}") {
            val context = call.application.androidContext
            val directory = FileDropStorage.directory(context)
            val relativePath = call.parameters.getAll("path")?.joinToString("/") ?: ""
            val targetDir = FileDropStorage.resolve(directory, relativePath)

            if (targetDir == null || !targetDir.isDirectory) {
                call.respondText(
                    """{"success":false,"error":"Target folder not found"}""",
                    ContentType.Application.Json,
                    HttpStatusCode.NotFound
                )
                return@post
            }

            val uploaded = mutableListOf<String>()

            try {
                call.receiveMultipart().forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val target = FileDropStorage.uniqueFile(
                            targetDir,
                            FileDropStorage.sanitizeFileName(part.originalFileName)
                        )

                        try {
                            writFile(part, target)
                            uploaded.add(target.name)
                        } catch (e: IOException) {
                            target.delete()
                            throw e
                        }
                    }
                    part.dispose()
                }

                call.respondText(
                    Json.encodeToString(FileDropUploadResult(success = true, files = uploaded)),
                    ContentType.Application.Json
                )
            } catch (e: Exception) {
                call.respondText(
                    Json.encodeToString(
                        FileDropUploadResult(success = false, error = e.message ?: "Upload failed")
                    ),
                    ContentType.Application.Json,
                    HttpStatusCode.InternalServerError
                )
            }
        }

        delete("/{path...}") {
            val context = call.application.androidContext
            val directory = FileDropStorage.directory(context)
            val relativePath = call.parameters.getAll("path")?.joinToString("/") ?: ""
            val target = FileDropStorage.resolve(directory, relativePath)

            if (target == null || target == directory.canonicalFile) {
                call.respondText(
                    """{"success":false,"error":"Invalid path"}""",
                    ContentType.Application.Json,
                    HttpStatusCode.BadRequest
                )
                return@delete
            }

            val deleted = target.delete()
            call.respondText(
                Json.encodeToString(
                    FileDropUploadResult(
                        success = deleted,
                        files = if (deleted) listOf(target.name) else emptyList(),
                        error = if (deleted) null else "Could not delete (folder must be empty)"
                    )
                ),
                ContentType.Application.Json,
                if (deleted) HttpStatusCode.OK else HttpStatusCode.Conflict
            )
        }

        get {
            val context = call.application.androidContext
            val directory = FileDropStorage.directory(context)

            if (call.request.queryParameters["json"] == "1") {
                respondFileDropListing(call, route.path, directory, "", asJson = true)
            } else {
                call.respondRedirect("${route.path}/", permanent = false)
            }
        }
    }
}

private suspend fun respondFileDropListing(
    call: io.ktor.server.application.ApplicationCall,
    basePath: String,
    target: File,
    relativePath: String,
    asJson: Boolean
) {
    call.response.headers.append(
        HttpHeaders.CacheControl,
        CacheControl.NoCache(null).toString()
    )

    if (!asJson) {
        val listing = DirectoryListingGenerator.generate(
            target,
            basePath,
            relativePath,
            allowUpload = true
        )
        call.respondText(listing, ContentType.Text.Html)
        return
    }

    val entries = (target.listFiles() ?: emptyArray())
        .sortedWith(compareBy({ !it.isDirectory }, { it.name }))
        .map { file ->
            val entryPath = if (relativePath.isEmpty()) {
                file.name
            } else {
                "$relativePath/${file.name}"
            }
            FileDropEntry(
                name = file.name,
                size = if (file.isFile) file.length() else 0L,
                lastModified = file.lastModified(),
                isDirectory = file.isDirectory,
                url = "$basePath/$entryPath"
            )
        }

    call.respondText(
        Json.encodeToString(FileDropListing(path = relativePath, files = entries)),
        ContentType.Application.Json
    )
}

@Serializable
private data class FileDropUploadResult(
    val success: Boolean,
    val files: List<String> = emptyList(),
    val error: String? = null
)

private suspend fun writFile(part: PartData.FileItem, target: File) {
    var total = 0L
    target.outputStream().use { output ->
        val channel = part.provider()
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val read = channel.readAvailable(buffer, 0, buffer.size)
            if (read == -1) break
            if (read == 0) {
                yield()
                continue
            }
            total += read
            if (total > MAX_UPLOAD_BYTES) {
                throw IOException("File exceeds the 2 GB upload limit")
            }
            output.write(buffer, 0, read)
        }
    }
}
