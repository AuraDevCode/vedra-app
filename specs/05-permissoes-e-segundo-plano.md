# 05 · Permissões e segundo plano

Princípio: **pedir o mínimo, explicar antes, validar sempre.** Toda permissão tem três partes: por que pedimos, como pedimos e como o app verifica sozinho se ela está ok.

## Permissões e acessos

| # | Acesso | Tipo | Obrigatório? | Por que |
|---|---|---|---|---|
| P1 | Serviço de acessibilidade (`BIND_ACCESSIBILITY_SERVICE`) | Ativado pelo usuário nas Configurações | **Sim** | Saber, na hora, que um app protegido abriu e desenhar a cortina |
| P2 | Biometria (`USE_BIOMETRIC`) | Normal (concedida na instalação) | Não | Desbloqueio por digital |
| P3 | Notificações (`POST_NOTIFICATIONS`, Android 13+) | Em tempo de execução | Recomendado | Avisar "Proteção desligada" e o estado de pausa |
| P4 | Sem otimização de bateria | Configuração do usuário | **Sim na Samsung** | Impedir que o sistema suspenda o Vedra |
| P5 | Visibilidade de apps | `<queries>` com `MAIN/LAUNCHER` | Sim (automático) | Listar os apps instalados **sem** `QUERY_ALL_PACKAGES` |
| P6 | `VIBRATE` | Normal | Não | Vibração ao errar o PIN |

**Não usamos:** `INTERNET`, `SYSTEM_ALERT_WINDOW`, `QUERY_ALL_PACKAGES`, `PACKAGE_USAGE_STATS` (só no plano de reserva), `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` (usamos a tela de lista, que não exige permissão e não tem restrição na Play), `FOREGROUND_SERVICE`, `RECEIVE_BOOT_COMPLETED`, administrador do dispositivo (estudo na v2).

## Como validar cada uma

| # | Verificação | API | Estados possíveis |
|---|---|---|---|
| P1a | Serviço **habilitado** | `AccessibilityManager.getEnabledAccessibilityServiceList(FEEDBACK_ALL_MASK)` contém `VedraAccessibilityService` | Habilitado / Desabilitado |
| P1b | Serviço **rodando** de fato | Flag em memória definida em `onServiceConnected()` e limpa em `onUnbind()`, lida pela UI no mesmo processo | Rodando / Habilitado mas parado (bug conhecido de alguns aparelhos) |
| P1c | Configuração restrita (Android 13+) | `getInstallSourceInfo().installingPackageName != "com.android.vending"` e serviço desabilitado | Mostra o tutorial "Permitir configurações restritas" |
| P2 | Biometria disponível | `BiometricManager.canAuthenticate(BIOMETRIC_STRONG)` | OK / Sem digital cadastrada / Sem hardware / Indisponível |
| P3 | Notificações | `NotificationManagerCompat.areNotificationsEnabled()` | Ok / Negada |
| P4a | Isenção de otimização | `PowerManager.isIgnoringBatteryOptimizations(pkg)` | Isento / Otimizado |
| P4b | Restrição de segundo plano | `ActivityManager.isBackgroundRestricted()` | Livre / Restrito |
| P4c | Listas da Samsung ("Apps em suspensão", "suspensão profunda", "nunca suspensos") | **Não há API pública** | Item manual: o usuário segue o guia e marca "Já fiz" |
| P5 | Lista de apps | `queryIntentActivities(MAIN/LAUNCHER)` devolve mais de 1 item | Ok / Vazia (falha) |

A UI revalida tudo em `onResume()` de cada tela e ao voltar de qualquer tela do sistema.

## Painel de saúde

Selo da tela inicial:

| Selo | Condição |
|---|---|
| 🟢 **Protegido** | P1a e P1b ok, P4a ok, P4b livre, pelo menos 1 app protegido |
| 🟡 **Atenção** | Proteção funcionando, mas falta um item recomendado: P3 negada, P4c não confirmada, biometria indisponível |
| 🔴 **Desprotegido** | P1a ou P1b falhou, P4b restrito, ou nenhum app protegido |

Cada linha mostra o ícone, o título, uma explicação em uma frase e o botão **Corrigir**. Abaixo, uma linha informativa: **"Último bloqueio: WhatsApp, há 3 min · cortina em 4 ms"**, lida do log local. É a prova de que está funcionando.

## Deep links para corrigir

Tentar na ordem e usar o primeiro que `resolveActivity()` encontrar:

| Item | Intents (ordem de tentativa) |
|---|---|
| Acessibilidade | `Settings.ACTION_ACCESSIBILITY_DETAILS_SETTINGS` com o componente (Android 11+, abre direto o Vedra) → `ACTION_ACCESSIBILITY_SETTINGS` |
| Configurações restritas | `ACTION_APPLICATION_DETAILS_SETTINGS` (o usuário toca no ⋮ → "Permitir configurações restritas") + tutorial ilustrado |
| Bateria | `ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS` → `ACTION_APPLICATION_DETAILS_SETTINGS` |
| Samsung: limites de segundo plano | `com.samsung.android.lool / com.samsung.android.sm.battery.ui.BatteryActivity` → `com.samsung.android.sm_cn` (variações) → detalhes do app. **Testar no aparelho real**, porque os nomes mudam entre versões do One UI |
| Notificações | `Settings.ACTION_APP_NOTIFICATION_SETTINGS` |
| Digital | `Settings.ACTION_BIOMETRIC_ENROLL` (Android 11+) → `ACTION_SECURITY_SETTINGS` |

## Segundo plano: como o Vedra se mantém vivo

1. **O serviço de acessibilidade é vinculado pelo sistema.** Enquanto estiver habilitado, o sistema o recria depois de reiniciar o aparelho ou de o processo morrer. Não precisa de serviço em primeiro plano nem de `BOOT_COMPLETED`.
2. **A Samsung é o maior risco.** O One UI pode congelar apps em "suspensão profunda" mesmo com o serviço de acessibilidade ativo. Mitigação: guia obrigatório no primeiro uso, item P4c no painel e o teste de 7 dias (ver [10](10-qualidade-e-testes.md)).
3. **Alerta de desligamento:**
   - Em `onUnbind()` do serviço (o usuário desligou ou o sistema derrubou), posta na hora a notificação **"Vedra: proteção desligada. Toque para reativar"**.
   - **WorkManager** periódico de 15 min (o mínimo do sistema, custo desprezível) verifica P1a e P1b e posta a mesma notificação se algo estiver errado. É a rede de segurança para quando o processo foi morto sem `onUnbind()`.
4. **Plano B, se os testes no Samsung mostrarem que o serviço é morto:** adicionar um *foreground service* `specialUse` com notificação discreta "Proteção ativa", ligado nas configurações. Exige justificativa na Play. Só entra se o teste de 7 dias falhar (ver ADR-006).

## Instalação por APK: Play Protect

Constatado em 13/10/2026 no Samsung: o **Play Protect bloqueia a instalação** do APK baixado (navegador, WhatsApp, gerenciador de arquivos) com a mensagem de que o app "pede permissões demais". É a proteção antifraude do Google, ativa no Brasil, que barra apps de fora da loja que declaram **serviço de acessibilidade**.

| Caminho | Funciona? | Observação |
|---|---|---|
| `adb install` (cabo USB) | ✅ | A proteção não vale para o adb |
| Desligar "Verificar apps com o Play Protect" só durante a instalação | ✅ | Religar logo depois. O app continua funcionando |
| Teste interno da Play Store | ✅ | Solução definitiva: sem bloqueio e sem "configurações restritas" |

Por isso, a distribuição para aparelhos sem cabo deve ir para o **teste interno da Play** o quanto antes (antecipar parte da F4).

## Particularidades da Samsung (One UI)

| Tema | O que fazer |
|---|---|
| Configurações restritas | Por APK (fase 1, seu aparelho): obrigatório liberar uma vez. Pela Play Store: não aparece |
| Bateria | Configurações → Apps → Vedra → Bateria → **Sem restrições** |
| Suspensão | Configurações → Bateria → Limites de uso em segundo plano → **Apps nunca suspensos** → adicionar Vedra. Desativar "Colocar apps não usados em suspensão" ou garantir que o Vedra esteja na lista de exceção |
| Otimização automática | "Manutenção do dispositivo → Otimização automática" reinicia o aparelho. O serviço volta sozinho (testar) |
| Janela pop-up e tela dividida | A cortina deve cobrir a tela inteira. Testar com o WhatsApp em pop-up |
| Painel Edge | `cocktailbarservice` não conta como saída (regra de saída) |
| Pasta Segura | Apps clonados na Pasta Segura rodam em outro perfil de usuário. **Fora do escopo** (o serviço não vê o outro perfil) |
| Modo DeX | v1 cobre só a tela do celular. DeX fica para estudo |
| Teclado Samsung | `com.samsung.android.honeyboard` não conta como saída |
| Detecção | `Build.MANUFACTURER.equals("samsung", true)` + propriedade `ro.build.version.oneui` (se houver) para mostrar o guia certo |
