package br.com.auradevcode.vedra.core

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PinRulesTest {
    @Test
    fun `tamanho e digitos`() {
        assertTrue(PinRules.isValid("4821"))
        assertTrue(PinRules.isValid("48213907"))
        assertFalse(PinRules.isValid("482"))
        assertFalse(PinRules.isValid("482139071"))
        assertFalse(PinRules.isValid("48a1"))
    }

    @Test
    fun `pins fracos`() {
        listOf("0000", "1234", "654321", "2580", "111111").forEach { assertTrue(PinRules.isWeak(it), it) }
        listOf("4821", "739104").forEach { assertFalse(PinRules.isWeak(it), it) }
    }
}
