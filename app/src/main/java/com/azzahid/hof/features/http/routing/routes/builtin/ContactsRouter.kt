package com.azzahid.hof.features.http.routing.routes.builtin

import android.Manifest
import android.provider.ContactsContract
import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond

internal fun io.ktor.server.routing.Route.addContactsRoute(route: Route) {
    get(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext

        if (!call.requirePermission(
                context,
                Manifest.permission.READ_CONTACTS,
                "Contacts permission required. Grant READ_CONTACTS permission in device settings."
            )
        ) {
            return@get
        }

        val limit = (call.request.queryParameters["limit"]?.toIntOrNull() ?: 2_000)
            .coerceIn(1, 10_000)
        val search = call.request.queryParameters["q"]?.lowercase()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val contacts = mutableListOf<Map<String, String>>()
        try {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (cursor.moveToNext() && contacts.size < limit) {
                    val name = cursor.getString(nameIndex) ?: continue
                    val phone = cursor.getString(numberIndex) ?: continue

                    if (search.isNullOrBlank() ||
                        name.lowercase().contains(search) ||
                        phone.contains(search)
                    ) {
                        contacts.add(mapOf("name" to name, "phone" to phone))
                    }
                }
            }
        } catch (e: SecurityException) {
            call.respond(
                HttpStatusCode.Forbidden,
                Failure(error = "Contacts permission denied: ${e.message}")
            )
            return@get
        }

        call.respond(Success(data = mapOf("count" to contacts.size, "contacts" to contacts)))
    }
}
