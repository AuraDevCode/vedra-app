# 10 · Qualidade e testes

## Pirâmide

| Nível | Ferramenta | O que cobre | Meta |
|---|---|---|---|
| Unidade (JVM) | JUnit 5 + Turbine | `core`: máquina de estados da sessão, regra de saída, rebloqueio, limitador de tentativas, regras do PIN | ≥ 80% de linhas em `core` |
| Unidade Android | Robolectric | `data`: DataStore, hasher do PIN (sem Keystore real), catálogo de apps | Caminhos principais |
| UI | Compose UI Test | Primeiro uso, teclado de PIN, lista de apps, painel de saúde | Fluxos P0 |
| Instrumentado | AndroidX Test (aparelho ou emulador) | Keystore real, biometria (emulador com digital simulada), serviço de acessibilidade ligado via `adb` | Fluxos P0 |
| Desempenho | Macrobenchmark + log local | RNF-01 a RNF-06 | Metas de [03](03-requisitos-nao-funcionais.md) |
| Manual | Roteiro abaixo | Samsung real, casos de borda | Antes de cada versão |

## Build de depuração e `FLAG_SECURE`

No build de depuração (`br.com.auradevcode.vedra.dev`), o `FLAG_SECURE` fica **desligado** nas telas e na cortina, senão capturas e gravações saem pretas. O release sempre liga (RNF-18).

## Como provar que "nada aparece antes" (RNF-02)

1. Ativar "Mostrar toques" e gravar a tela a 60 fps (`adb shell screenrecord --bit-rate 20M` ou o gravador do Samsung).
2. Abrir o app protegido 20 vezes por caminhos diferentes (ícone, recentes, notificação, compartilhar).
3. `python tools/frame-audit.py gravacao.mp4 folha.png` (OpenCV): lista cada quadro com brilho e fração escura e monta uma folha de contato para conferir que só aparecem launcher → cortina → tela de PIN.
4. **Critério:** 0 quadros com conteúdo do app protegido em 20 de 20 aberturas.

## Matriz de casos (manual, Samsung)

| # | Caso | Esperado |
|---|---|---|
| T1 | Abrir o WhatsApp pelo ícone | Cortina → digital → libera |
| T2 | Abrir pelos recentes | Igual ao T1 |
| T3 | Tocar numa notificação do WhatsApp | Igual ao T1 |
| T4 | Galeria → Compartilhar → WhatsApp | Bloqueia a tela de compartilhar do WhatsApp |
| T5 | Desbloquear, puxar a barra de notificações e voltar | Continua liberado |
| T6 | Desbloquear, digitar (teclado aparece) | Continua liberado |
| T7 | Desbloquear, ir ao Início e voltar (rebloqueio imediato) | Pede de novo |
| T8 | Igual ao T7 com rebloqueio de 1 min, voltando em 30 s e depois em 70 s | Libera; depois pede |
| T9 | Apagar e acender a tela | Pede de novo, em qualquer regra |
| T10 | WhatsApp em janela pop-up e em tela dividida | Cortina na tela inteira |
| T11 | 5 PINs errados | Espera de 30 s. Mudar a hora do sistema não pula a espera |
| T12 | Reiniciar o aparelho durante a espera | A espera continua |
| T13 | Cadastrar uma nova digital no Android | O Vedra exige o PIN |
| T14 | Desligar o serviço nas Configurações | Notificação "Proteção desligada" em menos de 1 s |
| T15 | Reiniciar o aparelho | O serviço volta, e o bloqueio funciona sem abrir o Vedra |
| T16 | Otimização automática da Samsung (reinício noturno) | Igual ao T15 |
| T17 | Deixar o aparelho 8 h parado e abrir o WhatsApp | Bloqueia (o serviço não foi suspenso) |
| T18 | Proteger o app Configurações e tentar desligar o serviço | Pede o PIN antes |
| T19 | Fonte em 200% + TalkBack na tela de bloqueio | Usável, tudo anunciado |
| T20 | Botão Voltar na tela de bloqueio | Vai para o Início, e o app continua bloqueado |
| T21 | Abrir a câmera de dentro do WhatsApp e voltar | Continua liberado (regra de saída P1) |
| T22 | Ligação recebida com o WhatsApp bloqueado aberto | A ligação aparece normalmente. Depois, volta ao bloqueio |
| T23 | Vídeo (TikTok, YouTube) parado na tela sem tocar, além do tempo de apagar a tela | A tela **não** apaga. No build de depuração: abrir `KeepScreenOnTestActivity` e conferir `dumpsys window \| grep mHoldScreenWindow` |

## Teste de resistência (critério da fase 2)

**7 dias de uso real no Samsung do Wander**, com o log local ativo. Ao fim, exportar o log e conferir:
- 0 aberturas de app protegido sem cortina.
- Nenhuma desconexão do serviço sem notificação.
- p95 da cortina ≤ 16 ms.
- Bateria ≤ 1% por dia (Configurações → Bateria).

## Aparelhos

| Prioridade | Aparelho | Por quê |
|---|---|---|
| 1 | Emulador `finora_test` (Pixel 6, API 36) | Primeiro ciclo de testes, sem depender do aparelho |
| 2 | **Galaxy S20** (depuração USB) | Primeiro Samsung real: One UI, suspensão, teclado Samsung, gravação a 60 fps |
| 3 | **Galaxy S22 Ultra** (uso diário, sem depuração) | Teste de 7 dias com APK instalado manualmente |
| 4 | Emulador com API 26 | Limite inferior da faixa suportada |

## CI (GitHub Actions)

Em todo push ou PR:
1. `./gradlew ktlintCheck detekt lint`
2. `./gradlew test` (unidade + Robolectric) com relatório de cobertura (Kover)
3. `./gradlew assembleDebug`
4. **Guarda de privacidade:** `aapt2 dump permissions` no APK de release falha se aparecer `android.permission.INTERNET`
5. Artefato: APK de depuração para baixar e instalar no Samsung

Em tag `v*`: `bundleRelease` assinado (segredos do GitHub), anexado à release. O envio para a Play fica manual até haver a conta de serviço.

## Definição de pronto (para todo card)

- Critérios de aceite do RF atendidos e testados.
- Testes de unidade para qualquer regra nova em `core`.
- CI verde.
- Textos em `strings.xml`.
- Testado no Samsung quando tocar no serviço, na cortina ou na tela de bloqueio.

## Resultados

### 13/10/2026 · Emulador Pixel 6, API 36, build de depuração

| Caso | Resultado |
|---|---|
| T1 abrir pelo ícone (frio e quente) | ✅ 0 quadros vazados em gravação (launcher → cortina → PIN) |
| T7 rebloqueio imediato | ✅ |
| T9 apagar e acender a tela | ✅ volta bloqueado |
| T20 Voltar na tela de bloqueio | ✅ vai para o Início e continua bloqueado |
| PIN errado e certo | ✅ |
| RNF-01 cortina | 1,3 a 13 ms (`updateViewLayout`). Precisa ser medido no S20 |
| RNF-03 toque → PIN | ⚠️ ~700 ms no emulador em depuração (Compose em debug é lento). Medir em release no S20 |

**Bug encontrado e corrigido:** neste Android, o evento da janela da `LockActivity` chega com `className = FrameLayout`. O serviço achava que o usuário tinha saído para o Vedra e fechava o desafio. Agora toda janela do próprio pacote é passageira (ADR-012).

### 14/10/2026 · Samsung do Wander (APK de depuração)

| Caso | Resultado |
|---|---|
| Bloqueio funcionando | ✅ |
| **T23 tela apagando durante vídeo (TikTok)** | ❌ → corrigido. A cortina invisível era `OPAQUE` e anulava o "manter tela acesa" dos outros apps. Agora é `TRANSLUCENT`. No emulador, `mHoldScreenWindow` voltou a apontar para a tela de teste com o serviço ligado, e a gravação seguiu com 0 quadros vazados |

### 21/10/2026 · Rebloqueio (ADR-015), emulador Pixel 6 API 36

| Caso | Resultado |
|---|---|
| Recentes → tocar no mesmo app | ✅ não pede o PIN |
| Início por 8 s → voltar (padrão 1 min) | ✅ não pede o PIN |
| Outro app por 65 s → voltar (padrão 1 min) | ✅ pede o PIN |
| Unidade: volta rápida no modo imediato, tolerância expirada, launcher → outro app | ✅ (20 testes no `core`) |
