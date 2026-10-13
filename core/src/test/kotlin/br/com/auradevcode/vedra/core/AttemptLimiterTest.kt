package br.com.auradevcode.vedra.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AttemptLimiterTest {
    private var now = 0L

    @Test
    fun `quinto erro impoe 30 segundos`() {
        val limiter = AttemptLimiter({ now })
        repeat(4) { assertEquals(0, limiter.onFailure()) }
        assertEquals(30_000, limiter.onFailure())
        assertTrue(limiter.isLockedOut())
        now += 30_001
        assertFalse(limiter.isLockedOut())
    }

    @Test
    fun `decimo erro impoe 5 minutos e decimo quinto 15`() {
        val limiter = AttemptLimiter({ now })
        repeat(9) { limiter.onFailure() }
        assertEquals(5 * 60_000L, limiter.onFailure())
        repeat(4) { limiter.onFailure() }
        assertEquals(15 * 60_000L, limiter.onFailure())
        assertEquals(15 * 60_000L, limiter.onFailure())
    }

    @Test
    fun `acerto zera a contagem`() {
        val limiter = AttemptLimiter({ now })
        repeat(5) { limiter.onFailure() }
        limiter.onSuccess()
        assertEquals(0, limiter.failures)
        assertFalse(limiter.isLockedOut())
    }

    @Test
    fun `estado restaurado mantem a espera restante`() {
        val limiter = AttemptLimiter({ now }, AttemptState(5, 20_000))
        assertEquals(20_000, limiter.remainingMs())
        assertEquals(AttemptState(5, 20_000), limiter.snapshot())
    }
}
