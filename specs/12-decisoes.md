# 12 · Decisões (ADR)

Formato: contexto → decisão → consequências. Status: ✅ aceita · 🟡 proposta · ❌ substituída.

## ADR-001 · Kotlin nativo com Jetpack Compose ✅

**Contexto:** o núcleo do produto é API de sistema (acessibilidade, janelas, Keystore e biometria). Flutter e React Native exigiriam escrever essa parte em Kotlin do mesmo jeito.
**Decisão:** Kotlin + Compose, sem framework multiplataforma.
**Consequências:** um código só e o caminho mais curto até o sistema. Sem iOS (que também não permitiria esse tipo de app).

## ADR-002 · Detecção por AccessibilityService, com UsageStats como reserva ✅

**Contexto:** `UsageStatsManager` exige polling e deixa o conteúdo aparecer. A acessibilidade avisa na hora.
**Decisão:** `AccessibilityForegroundSource` na v1, atrás da interface `ForegroundSource`. A variante `playsafe` (UsageStats + `SYSTEM_ALERT_WINDOW`) só será construída se a Play recusar.
**Consequências:** precisa do aviso em destaque e da declaração na Play, e exige a configuração restrita no APK. Risco no Android 17 com Proteção Avançada.

## ADR-003 · Cortina por overlay de acessibilidade + Activity de desafio ✅

**Contexto:** uma Activity sozinha é lenta (80 a 250 ms). Um overlay sozinho não suporta `BiometricPrompt`.
**Decisão:** `TYPE_ACCESSIBILITY_OVERLAY` pré-criado e mostrado de forma síncrona no evento. `LockActivity` para PIN e digital.
**Consequências:** não precisa de `SYSTEM_ALERT_WINDOW`. Coordenar a remoção da cortina com o `finish()` da Activity, para não piscar.

## ADR-004 · Zero internet ✅ (decisão do Wander, 13/10/2026)

**Decisão:** sem `INTERNET` no manifesto, verificado no CI.
**Consequências:** sem relatórios remotos de falha. A depuração usa o log local exportado manualmente (RF-14). Formulário de segurança dos dados: "não coleta".

## ADR-005 · DI manual ✅

**Contexto:** o app é pequeno, e a partida do serviço deve ser leve.
**Decisão:** `AppContainer` criado em `VedraApp`, sem Hilt ou Koin.
**Consequências:** menos geração de código e build mais rápido. Reavaliar se passar de cerca de 30 dependências injetadas.

## ADR-006 · Sem serviço em primeiro plano na v1 🟡

**Contexto:** o serviço de acessibilidade já é vinculado pelo sistema. A notificação permanente incomoda, e o tipo `specialUse` exige justificativa.
**Decisão:** não usar. Reavaliar com o resultado do teste de 7 dias no Samsung.
**Consequências:** se a Samsung suspender o serviço, adicionar como opção (desligada por padrão ou ligada só na Samsung).

## ADR-007 · Sem recuperação de PIN ✅ (decisão do Wander, 13/10/2026)

**Decisão:** só a digital redefine o PIN. Sem e-mail nem pergunta de segurança.
**Consequências:** quem esquecer o PIN e não tiver digital precisa limpar os dados do app (perdendo a configuração). Avisar isso claramente no primeiro uso.

## ADR-008 · Rebloqueio configurável ✅ (decisão do Wander, 13/10/2026)

**Decisão:** imediato (padrão), por tempo (30 s a 30 min) ou só ao apagar a tela. Configuração global na v1 e por app na F3. Apagar a tela sempre encerra as sessões.

## ADR-009 · Estado de sessão só em memória ✅

**Decisão:** sessões liberadas nunca são gravadas em disco.
**Consequências:** se o processo morrer, tudo volta a bloquear (falha segura). Custo: um desbloqueio extra raro.

## ADR-010 · PIN com PBKDF2 + pepper no Keystore ✅

**Contexto:** a biblioteca `security-crypto` (EncryptedSharedPreferences) foi descontinuada. Um PIN tem pouca entropia e é fácil de quebrar offline.
**Decisão:** PBKDF2-HMAC-SHA256 calibrado em cerca de 150 ms + HMAC com chave não exportável do Keystore.
**Consequências:** ataque offline inviável sem o aparelho. Sem dependências externas de cripto.

## ADR-011 · Multimódulo enxuto (`app`, `core`, `data`, `guard`) ✅

**Decisão:** quatro módulos. `core` é Kotlin puro, para testar as regras sem Android.
**Consequências:** builds incrementais e regras isoladas. Não criar módulos por feature enquanto o app for pequeno.
