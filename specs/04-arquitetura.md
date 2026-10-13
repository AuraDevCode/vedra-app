# 04 · Arquitetura

## Stack

| Camada | Escolha | Por quê |
|---|---|---|
| Linguagem | **Kotlin** (versão estável mais recente na criação) | Acesso direto às APIs de sistema: acessibilidade, janelas, biometria e Keystore |
| UI | **Jetpack Compose + Material 3** | Padrão atual, tema da marca fácil de aplicar |
| Build | Gradle (Kotlin DSL) + version catalog (`libs.versions.toml`) | Versões centralizadas |
| Persistência | **DataStore** (Proto) | Leve, transacional, sem SQL para poucos dados |
| Cripto | Android Keystore + PBKDF2 (JCA) | Sem dependência externa. `security-crypto` foi descontinuada |
| Biometria | `androidx.biometric` (`BiometricPrompt` + `CryptoObject`) | Detecta novas digitais cadastradas (RF-03) |
| DI | Manual (`AppContainer`) | Partida mais rápida e menos geração de código. O app é pequeno (ver ADR-005) |
| Concorrência | Coroutines + Flow | Fora do caminho crítico do evento |
| Testes | JUnit 5, Turbine, Robolectric, Compose UI Test, Macrobenchmark | Ver [10](10-qualidade-e-testes.md) |
| Qualidade | ktlint, detekt, Android Lint | Rodam no CI |

**Sem bibliotecas de rede, analytics ou anúncios** (RNF-14).

## Módulos

```
vedra-app/
├── app/            → Activities, telas Compose, tema, navegação, AppContainer
├── core/           → Kotlin puro (JVM): SessionPolicy, AttemptLimiter, PinRules, modelos. 100% testável sem Android
├── data/           → DataStore, Keystore, PinHasher, BiometricKeyManager, AppCatalog (PackageManager)
└── guard/          → VedraAccessibilityService, CurtainController, ForegroundDetector, LockLauncher
```

O `core` não depende do Android. É onde ficam as regras de decisão, testadas por unidade.

## Componentes e fluxo

```
┌───────────────────────── processo br.com.auradevcode.vedra ─────────────────────────┐
│                                                                                    │
│  VedraAccessibilityService (guard)                                                 │
│    onAccessibilityEvent(TYPE_WINDOW_STATE_CHANGED)                                 │
│        │ pkg, className, elapsedRealtimeNanos                                      │
│        ▼                                                                           │
│    ForegroundDetector ──► filtra ruído (systemui, teclado, o próprio Vedra)          │
│        │ ForegroundChanged(pkg)                                                    │
│        ▼                                                                           │
│    LockEngine (core) ── consulta ──► ProtectedAppsSnapshot (AtomicReference<Set>)  │
│        │                  └────────► SessionStore (em memória)                     │
│        │ Decision: ALLOW | LOCK(pkg)                                               │
│        ▼                                                                           │
│    CurtainController.show(pkg)  ── TYPE_ACCESSIBILITY_OVERLAY (síncrono, < 16 ms)  │
│    LockLauncher.start(pkg)      ── startActivity(LockActivity)                     │
│                                                                                    │
│  LockActivity (app) ── PIN / BiometricPrompt ──► LockEngine.unlock(pkg)            │
│        └─ finish() ──► CurtainController.hide() no próximo quadro                  │
│                                                                                    │
│  ScreenStateReceiver (ACTION_SCREEN_OFF, dinâmico no serviço) ──► LockEngine.lockAll()│
└────────────────────────────────────────────────────────────────────────────────────┘
```

### Por que cortina + Activity (e não só uma das duas)

- **Só Activity:** abrir uma Activity leva de 80 a 250 ms. Nesse intervalo, o app protegido aparece. ❌
- **Só overlay:** é instantâneo, mas o `BiometricPrompt` exige uma `FragmentActivity`, e um overlay não recebe bem o foco do teclado nem o botão Voltar. ❌
- **Cortina (overlay) + Activity:** a cortina tapa na hora, e a Activity traz a interação completa. ✅

A cortina é desenhada por um `AccessibilityService` com `TYPE_ACCESSIBILITY_OVERLAY`. Esse tipo de janela **não precisa** da permissão "Sobrepor a outros apps" e fica acima das janelas de apps.

A `LockActivity` pode ser aberta a partir do serviço porque **serviços de acessibilidade são isentos** das restrições de abrir Activity em segundo plano.

### LockActivity

- `launchMode="singleInstance"`, `taskAffinity` própria, `excludeFromRecents="true"`, `noHistory`, sem animação de entrada.
- `FLAG_SECURE`.
- Voltar ou Início sem desbloquear: `performGlobalAction(GLOBAL_ACTION_HOME)`. A cortina fica até o launcher aparecer e então some.
- Recebe o `pkg` por extra e mostra o ícone e o nome do app protegido (ou não, se a opção de ocultar estiver ligada).

## Máquina de estados (por app protegido)

```
              abriu sem sessão                         PIN/digital OK
  [Bloqueado] ───────────────────► [Desafio exibido] ─────────────────► [Liberado]
      ▲                                │  Voltar/Início                     │
      │                                ▼                                    │ saiu do app
      │                           [Bloqueado]                               ▼
      │                                                            [Em carência até T]
      │      tela apagou / T expirou / processo morreu                      │ voltou antes de T
      └─────────────────────────────────────────────────────────────────────┴──► [Liberado]
```

- O estado de sessão fica **só em memória**. Se o processo morrer, tudo volta a ser bloqueado (falha segura, RNF-11).
- `T` depende da regra de rebloqueio (RF-07): `0` = imediato, `∞` até a tela apagar.

### Regra de saída

Trocar de janela para um destes pacotes **não** conta como sair do app protegido:

| Categoria | Como identificar |
|---|---|
| O próprio Vedra | `packageName == BuildConfig.APPLICATION_ID` |
| Teclados | `InputMethodManager.enabledInputMethodList` |
| UI do sistema | `com.android.systemui` (cortina de notificações, volume), `com.samsung.android.app.cocktailbarservice` (painel Edge) |
| Diálogos de permissão | `com.google.android.permissioncontroller`, `com.android.permissioncontroller` |
| Seletores chamados pelo app (P1) | DocumentsUI, Photo Picker, câmera. Só se a troca acontecer em menos de X s e o próximo evento for de volta ao app protegido |

Abrir o launcher (que inclui os recentes na Samsung, `com.sec.android.app.launcher`) **conta** como sair.

## Dados persistidos (DataStore Proto)

```proto
message VedraSettings {
  repeated string protected_packages = 1;
  RelockPolicy default_relock = 2;            // IMMEDIATE | AFTER_SECONDS | SCREEN_OFF
  int32 relock_seconds = 3;
  map<string, RelockOverride> per_app = 4;    // P1
  bool biometric_enabled = 5;
  bool hide_app_name_on_curtain = 6;
  bool scramble_keypad = 7;
  ThemeMode theme = 8;
  bool onboarding_done = 9;
}

message Credential {                          // arquivo separado
  bytes salt = 1;
  bytes hash = 2;                             // HMAC_keystore(PBKDF2(pin, salt))
  int32 iterations = 3;
  int32 pin_length = 4;
  int32 version = 5;
}

message AttemptState {
  int32 consecutive_failures = 1;
  int64 lockout_remaining_ms = 2;             // recalculado no boot, ver 06
  int32 boot_count_at_write = 3;
}
```

- Ao salvar, `protected_packages` é publicado em `ProtectedAppsSnapshot` (`AtomicReference<Set<String>>`) para leitura O(1) no evento.
- O log de diagnóstico (RF-14) fica num arquivo circular separado, em `filesDir/diag/`, com no máximo 1 MB.

## Detecção de primeiro plano: estratégia plugável

```kotlin
interface ForegroundSource {           // em core
    val events: Flow<ForegroundChanged>
}
```

- **v1:** `AccessibilityForegroundSource`, por evento e instantâneo.
- **Reserva:** `UsageStatsForegroundSource` (polling de `UsageEvents` a cada 150 ms). Só será usado se a Play Store recusar a acessibilidade. É mais lento e pode deixar um quadro aparecer (ver ADR-002).

## Configuração do serviço (`res/xml/accessibility_service.xml`)

```xml
<accessibility-service
    android:accessibilityEventTypes="typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagDefault"
    android:canRetrieveWindowContent="false"
    android:notificationTimeout="0"
    android:isAccessibilityTool="false"
    android:description="@string/a11y_service_description"
    android:summary="@string/a11y_service_summary" />
```

- `notificationTimeout="0"`: o sistema não agrupa eventos (velocidade).
- `isAccessibilityTool="false"`: o Vedra **não** é ferramenta de acessibilidade, e declarar o contrário causa recusa na Play (ver [09](09-play-store-e-conformidade.md)).

## Pacotes do código (`app` + `guard`)

```
br.com.auradevcode.vedra
├── VedraApp.kt                 (Application + AppContainer)
├── ui/theme/                   (cores, tipografia, formas da marca)
├── ui/onboarding/
├── ui/home/                    (painel de saúde)
├── ui/apps/                    (lista de apps)
├── ui/lock/                    (LockActivity, teclado de PIN)
├── ui/settings/
└── guard/
    ├── VedraAccessibilityService.kt
    ├── CurtainController.kt
    ├── ForegroundDetector.kt
    ├── LockLauncher.kt
    └── ScreenStateReceiver.kt
```
