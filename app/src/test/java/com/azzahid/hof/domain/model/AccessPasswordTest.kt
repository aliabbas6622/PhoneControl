package com.azzahid.hof.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccessPasswordTest {

    @Test
    fun `rejects passwords shorter than minimum length`() {
        assertEquals(
            AccessPasswordError.TOO_SHORT,
            AccessPassword.validate("short")
        )
    }

    @Test
    fun `rejects passwords longer than maximum length`() {
        assertEquals(
            AccessPasswordError.TOO_LONG,
            AccessPassword.validate("a".repeat(AccessPassword.MAX_LENGTH + 1))
        )
    }

    @Test
    fun `rejects control characters`() {
        assertEquals(
            AccessPasswordError.INVALID_CHARACTERS,
            AccessPassword.validate("password\twith tab")
        )
        assertEquals(
            AccessPasswordError.INVALID_CHARACTERS,
            AccessPassword.validate("password\u007Fwith delete")
        )
    }

    @Test
    fun `accepts spaces inside the password`() {
        assertNull(AccessPassword.validate("correct horse battery"))
    }

    @Test
    fun `accepts minimum and maximum length`() {
        assertNull(AccessPassword.validate("a".repeat(AccessPassword.MIN_LENGTH)))
        assertNull(AccessPassword.validate("a".repeat(AccessPassword.MAX_LENGTH)))
    }

    @Test
    fun `normalize trims surrounding whitespace`() {
        assertEquals("my password", AccessPassword.normalize("  my password  "))
    }

    @Test
    fun `normalize then validate treats padded short value as short`() {
        assertEquals(
            AccessPasswordError.TOO_SHORT,
            AccessPassword.validate(AccessPassword.normalize("  short  "))
        )
    }

    @Test
    fun `normalize then validate accepts padded valid value`() {
        assertNull(AccessPassword.validate(AccessPassword.normalize("  long enough password  ")))
    }
}
