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
    private val chrome = "com.android.chrome"

    @BeforeEach
    fun setUp() {
        engine = LockEngine { now }
        engine.protectedPackages = setOf(whatsapp)
        engine.launcherPackages = setOf(launcher)
        engine.policy = RelockPolicy(RelockMode.IMMEDIATE)
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
    fun `rebloqueio imediato pede de novo ao voltar de outro app`() {
        engine.onForeground(whatsapp)
        engine.onUnlocked(whatsapp)
        engine.onForeground(chrome)
        assertEquals(Decision.Lock(whatsapp), engine.onForeground(whatsapp))
    }

    @Test
    fun `espiar os recentes e voltar rapido nao pede o PIN no modo imediato`() {
        engine.onForeground(whatsapp)
        engine.onUnlocked(whatsapp)
        engine.onForeground(launcher)
        now += 3_000
        assertEquals(Decision.Allow, engine.onForeground(whatsapp))
    }

    @Test
    fun `ficar no launcher alem da tolerancia pede o PIN no modo imediato`() {
        engine.onForeground(whatsapp)
        engine.onUnlocked(whatsapp)
        engine.onForeground(launcher)
        now += LockEngine.QUICK_RETURN_MS + 1
        assertEquals(Decision.Lock(whatsapp), engine.onForeground(whatsapp))
    }

    @Test
    fun `passar pelo launcher e abrir outro app encerra a tolerancia`() {
        engine.onForeground(whatsapp)
        engine.onUnlocked(whatsapp)
        engine.onForeground(launcher)
        engine.onForeground(chrome)
        engine.onForeground(launcher)
        now += 1_000
        assertEquals(Decision.Lock(whatsapp), engine.onForeground(whatsapp))
    }

    @Test
    fun `padrao e um minuto fora do app`() {
        val fresh = LockEngine { now }
        fresh.protectedPackages = setOf(whatsapp)
        fresh.onForeground(whatsapp)
        fresh.onUnlocked(whatsapp)
        fresh.onForeground(chrome)
        now += 50_000
        assertEquals(Decision.Allow, fresh.onForeground(whatsapp))
        fresh.onForeground(chrome)
        now += 61_000
        assertEquals(Decision.Lock(whatsapp), fresh.onForeground(whatsapp))
    }

    @Test
    fun `rebloqueio por tempo respeita a carencia`() {
        engine.policy = RelockPolicy(RelockMode.AFTER_TIMEOUT, 60_000)
        engine.onForeground(whatsapp)
        engine.onUnlocked(whatsapp)
        engine.onForeground(chrome)
        now += 30_000
        assertEquals(Decision.Allow, engine.onForeground(whatsapp))
        engine.onForeground(chrome)
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
