<p align="center">
  <img src="brand/vedra-icon.svg" width="112" alt="Vedra">
</p>

<h1 align="center">Vedra</h1>

<p align="center">
  <b>Seus apps, só para você.</b><br>
  Bloqueio de apps para Android com PIN ou digital. Rápido, privado e sem internet.
</p>

<p align="center">
  <img alt="Android 8+" src="https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.2-8A2CE2?logo=kotlin&logoColor=white">
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white">
  <img alt="Sem internet" src="https://img.shields.io/badge/internet-nenhuma-0A0A0A">
  <img alt="Status" src="https://img.shields.io/badge/status-em%20desenvolvimento-FFB020">
</p>

---

## O que é

O Vedra protege os apps que você escolher, como WhatsApp, galeria, banco e e-mail. Ao abrir um deles, o Vedra cobre a tela **antes** de qualquer conteúdo aparecer e pede seu PIN ou digital.

- ⚡ **Instantâneo:** a tela é coberta no mesmo instante em que o app abre. Nenhum quadro do conteúdo aparece.
- 🔒 **Privado de verdade:** sem permissão de internet, sem conta, sem anúncios e sem coleta de dados. Nada sai do aparelho.
- 👆 **PIN ou digital:** a digital é um atalho, e o PIN sempre funciona.
- ⏱️ **Rebloqueio do seu jeito:** pede de novo imediatamente, depois de um tempo ou só quando a tela apaga.
- 🩺 **Painel de saúde:** mostra se a proteção está ativa e ensina a corrigir o que estiver errado (permissões, bateria, segundo plano).

## Como funciona

```
Você abre o WhatsApp
        │
        ▼
Serviço de acessibilidade recebe "janela mudou: com.whatsapp"   ← só o nome do app, nada da tela
        │
        ▼
LockEngine (em memória): está protegido e sem sessão liberada?
        │ sim
        ▼
1. Cortina opaca sobre a tela, em milissegundos (overlay de acessibilidade)
2. Tela de PIN ou digital por cima
        │ desbloqueou
        ▼
Cortina sai, app liberado até a regra de rebloqueio mandar pedir de novo
```

Detalhes em [`specs/04-arquitetura.md`](specs/04-arquitetura.md).

### Privacidade

O serviço de acessibilidade é configurado com `canRetrieveWindowContent="false"` e lê **apenas** o nome do pacote do app em primeiro plano. Ele não lê textos, mensagens, toques nem conteúdo. O PIN é guardado só como hash (PBKDF2 + HMAC com chave do Android Keystore), e a digital é invalidada se uma nova digital for cadastrada no aparelho. Veja [`specs/06-seguranca-e-ameacas.md`](specs/06-seguranca-e-ameacas.md).

## Estrutura

| Módulo | Conteúdo |
|---|---|
| [`core/`](core) | Kotlin puro: regras de bloqueio e rebloqueio, limite de tentativas, regras do PIN. 100% testado por unidade |
| [`data/`](data) | DataStore, credencial com Keystore, chave biométrica, catálogo de apps instalados |
| [`guard/`](guard) | `VedraAccessibilityService` e a cortina (`TYPE_ACCESSIBILITY_OVERLAY`) |
| [`app/`](app) | Telas em Compose, tema da marca e tela de bloqueio |
| [`specs/`](specs/00-indice.md) | Especificação completa: produto, requisitos, arquitetura, permissões, segurança, UX, Play Store, testes e decisões |
| [`brand/`](brand) | Logo e ícones |
| [`tools/`](tools) | `frame-audit.py`: prova, por gravação de tela, que nenhum quadro do app protegido vaza |

## Compilar

Requisitos: JDK 17+ e Android SDK (platform 36). O Android Studio é opcional.

```bash
./gradlew :core:test          # testes das regras
./gradlew :app:assembleDebug  # APK de depuração (pacote br.com.auradevcode.vedra.dev)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

A política de privacidade está em [auradevcode.github.io/vedra-app/privacidade](https://auradevcode.github.io/vedra-app/privacidade/).

Depois de instalar: abra o Vedra, crie o PIN e ative o serviço em **Configurações → Acessibilidade → Vedra**. Em APKs instalados fora da Play Store no Android 13+, talvez seja preciso liberar antes em **Configurações → Apps → Vedra → ⋮ → Permitir configurações restritas**.

> No build de depuração, o `FLAG_SECURE` fica desligado para permitir capturas e gravações de teste. No release, ele está sempre ligado.

## Status

| Fase | Situação |
|---|---|
| F0 · Fundação | ✅ (CI, ktlint e detekt pendentes) |
| F1 · Prova do bloqueio | ✅ no emulador (0 quadros vazados) e no Samsung do Wander |
| F2 · MVP pessoal | 🚧 em andamento |
| F3 · Acabamento | — |
| F4 · Play Store | 🚧 teste fechado (0.1.8) enviado para revisão em 20/10/2026 |

Roadmap completo em [`specs/11-roadmap.md`](specs/11-roadmap.md).

## Stack

Kotlin · Jetpack Compose (Material 3) · Coroutines/Flow · DataStore · Android Keystore · `androidx.biometric` · AccessibilityService · Gradle (Kotlin DSL) com version catalog.

---

<p align="center">
  <img src="brand/vedra-icon.svg" width="40" alt="Vedra"><br>
  Um produto <a href="https://auradevcode.com.br">AuraDev</a>
</p>
