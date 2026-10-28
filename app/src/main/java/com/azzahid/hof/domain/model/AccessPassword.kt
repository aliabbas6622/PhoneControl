package com.azzahid.hof.domain.model

enum class AccessPasswordError {
    TOO_SHORT,
    TOO_LONG,
    INVALID_CHARACTERS
}

/**
 * Rules for a user-chosen access password. Internally the password is stored as the
 * existing access token, so these rules are shared by the phone UI and the web portal.
 */
object AccessPassword {

    const val MIN_LENGTH = 8
    const val MAX_LENGTH = 128

    fun normalize(raw: String): String = raw.trim()

    fun validate(value: String): AccessPasswordError? = when {
        value.length < MIN_LENGTH -> AccessPasswordError.TOO_SHORT
        value.length > MAX_LENGTH -> AccessPasswordError.TOO_LONG
        value.any { it.code < 0x20 || it.code == 0x7F } -> AccessPasswordError.INVALID_CHARACTERS
        else -> null
    }
}
