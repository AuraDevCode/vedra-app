# 11 · Roadmap

Cada fase termina com algo **instalável no Samsung** e um critério de pronto verificável.

## F0 · Fundação ✅ (13/10/2026) · pendente: CI, ktlint e detekt

- Projeto Gradle multimódulo (`app`, `core`, `data`, `guard`), version catalog, ktlint, detekt e CI.
- Tema Compose da marca (cores, tipografia, formas), ícone adaptativo e splash.
- `core`: máquina de estados da sessão e limitador de tentativas, com testes.

**Pronto quando:** o CI está verde, o APK de depuração instala no Samsung e abre com a marca Vedra.

## F1 · Prova do bloqueio (o maior risco primeiro) ✅ no emulador · ⏳ S20

> Já entregue além do previsto: PIN real com hash, digital, lista de apps, regra de rebloqueio e painel de saúde (adiantando parte da F2).

- `VedraAccessibilityService` + `CurtainController` + `LockActivity` só com o PIN fixo de teste.
- Lista de protegidos fixa no código (WhatsApp).
- Log de tempo da cortina.
- Roteiro de gravação a 60 fps (`tools/frame-audit.py`).

**Pronto quando:** T1, T2, T3, T4 e T20 passam, com 0 quadros vazados em 20 aberturas no Samsung. *Se falhar, revisamos a arquitetura antes de seguir.*

## F2 · App de verdade (MVP pessoal)

- Primeiro uso completo (PIN, digital, aviso, serviço, bateria e notificações).
- Credencial com hash + Keystore. Biometria com `CryptoObject`.
- Lista de apps com sugeridos. Regra de rebloqueio global.
- Painel de saúde com todas as verificações. Notificação de "Proteção desligada" + WorkManager.
- Tentativas erradas com espera.

**Pronto quando:** toda a matriz T1 a T20 passa e o **teste de 7 dias** é aprovado no Samsung.

## F3 · Acabamento

- Rebloqueio por app, pausa da proteção, teclado embaralhado, ocultar o nome do app.
- Tema claro, animações da aura, Dicas de privacidade.
- Regra de saída para câmera e seletores (T21).
- Exportar o log de diagnóstico.
- Acessibilidade da UI (TalkBack, fonte 200%).

**Pronto quando:** T21 e T22 passam e a revisão de UX com o Wander é aprovada.

## F4 · Play Store

- Busca de marca, política de privacidade publicada e ficha da loja (pt-BR e en).
- Tradução para o inglês.
- Assinatura de release, AAB no CI e teste interno.
- Declaração de acessibilidade + vídeo. Formulário de segurança dos dados.
- Teste fechado → produção.

**Pronto quando:** o app é aprovado na revisão e está disponível na loja.

## F5 · Futuro (estudos)

- Foto do intruso, disfarce de ícone, perfis e horários, bloco nas Configurações rápidas, widget.
- Administrador do dispositivo contra desinstalação (conforme a política da Play).
- Modelo de receita (ver [13](13-duvidas-em-aberto.md)).
- Variante `playsafe` (UsageStats) se a acessibilidade for recusada.

## Riscos do plano

| Risco | Impacto | Sinal | Resposta |
|---|---|---|---|
| A cortina não é rápida o bastante em algum caminho do One UI | Alto | Falha na F1 | Ajustar o caminho do evento e testar `TYPE_WINDOWS_CHANGED` como gatilho extra |
| A Samsung suspende o serviço | Alto | Falha no teste de 7 dias | Plano B: serviço em primeiro plano opcional (ADR-006) |
| A Play recusa o uso de acessibilidade | Alto | Revisão da F4 | Recurso com vídeo + variante `playsafe` |
| Proteção Avançada do Android 17 | Médio | Adoção do modo | Detectar e explicar. `playsafe` como alternativa |
