package br.com.auradevcode.vedra.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LockEngineTest {
    private var now = 1_000L
    private lateinit var engine: LockEngine

    private val whatsapp = "com.whatsapp"
    private val launcher = "com.sec.android.app.launcher"

    @BeforeEach
    fun setUp() {
        engine = LockEngine { now }
        engine.protectedPackages = setOf(whatsapp)
    }

    @Test
    fun `app livre e permitido`() {
        assertEquals(Decision.Allow, engine.onForeground(launcher))
    }

    @Test
    fun `app protegido sem sessao bloqueia`() {
        assertEquals(Decision.Lock(whatsapp), engine.onForeground(whatsapp))
    }

    @Test
    fun `depois de desbloquear continua liberado enquanto esta no app`() {
        engine.onForeground(whatsapp)
        engine.onUnlocked(whatsapp)
        assertEquals(Decision.Allow, engine.onForeground(whatsapp))
    }

    @Test
    fun `janela passageira nao conta como saida`() {
        engine.onForeground(whatsapp)
        engine.onUnlocked(whatsapp)
        assertEquals(Decision.Ignore, engine.onForeground("com.samsung.android.honeyboard", transient = true))
        assertEquals(Decision.Allow, engine.onForeground(whatsapp))
    }

    @Test
    fun `rebloqueio imediato pede de novo ao voltar`() {
        engine.onForeground(whatsapp)
        engine.onUnlocked(whatsapp)
        engine.onForeground(launcher)
        assertEquals(Decision.Lock(whatsapp), engine.onForeground(whatsapp))
    }

    @Test
    fun `rebloqueio por tempo respeita a carencia`() {
        engine.policy = RelockPolicy(RelockMode.AFTER_TIMEOUT, 60_000)
        engine.onForeground(whatsapp)
        engine.onUnlocked(whatsapp)
        engine.onForeground(launcher)
        now += 30_000
        assertEquals(Decision.Allow, engine.onForeground(whatsapp))
        engine.onForeground(launcher)
        now += 70_000
        assertEquals(Decision.Lock(whatsapp), engine.onForeground(whatsapp))
    }

    @Test
    fun `rebloqueio ao apagar a tela libera ate a tela apagar`() {
        engine.policy = RelockPolicy(RelockMode.SCREEN_OFF)
        engine.onForeground(whatsapp)
        engine.onUnlocked(whatsapp)
        engine.onForeground(launcher)
        now += 3_600_000
        assertEquals(Decision.Allow, engine.onForeground(whatsapp))
        assertEquals(whatsapp, engine.onScreenOff())
        assertEquals(Decision.Lock(whatsapp), engine.onForeground(whatsapp))
    }

    @Test
    fun `tela apagada encerra todas as sessoes em qualquer regra`() {
        engine.policy = RelockPolicy(RelockMode.AFTER_TIMEOUT, 600_000)
        engine.onForeground(whatsapp)
        engine.onUnlocked(whatsapp)
        engine.onForeground(launcher)
        assertNull(engine.onScreenOff())
        assertFalse(engine.isUnlocked(whatsapp))
    }

    @Test
    fun `app removido da lista deixa de bloquear`() {
        engine.protectedPackages = emptySet()
        assertEquals(Decision.Allow, engine.onForeground(whatsapp))
    }

    @Test
    fun `isUnlocked reflete a sessao`() {
        engine.onForeground(whatsapp)
        assertFalse(engine.isUnlocked(whatsapp))
        engine.onUnlocked(whatsapp)
        assertTrue(engine.isUnlocked(whatsapp))
    }
}
