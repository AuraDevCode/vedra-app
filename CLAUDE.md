# Vedra — instruções para o Claude Code

App Android (Kotlin + Compose) que bloqueia outros apps com PIN ou digital. A especificação em `specs/` é a fonte da verdade: leia `specs/00-indice.md` antes de implementar qualquer coisa.

## Regras do projeto

1. **Zero internet.** Nunca adicione `android.permission.INTERNET` nem bibliotecas de rede, analytics ou anúncios (ADR-004). O CI falha se a permissão aparecer.
2. **Caminho crítico do evento** (`guard/`): sem I/O, sem corrotina e sem alocação. A decisão de bloquear é uma consulta em memória (RNF-01).
3. **Falha segura:** em qualquer dúvida ou erro, bloqueie. Estado de sessão só em memória (ADR-009).
4. Regras de decisão ficam em `core/` (Kotlin puro) e sempre têm teste de unidade.
5. Textos sempre em `strings.xml` (pt-BR). Nenhum texto fixo no código.
6. Nunca versionar keystores, `keystore.properties`, `.credentials.json` ou `local.properties`. A chave de upload fica em `%OneDrive%\Develop\keystores\vedra\`.
7. Mudou uma decisão? Atualize `specs/12-decisoes.md`. Respondeu uma dúvida? Atualize `specs/13-duvidas-em-aberto.md`.

## Ambiente desta máquina

- JDK: `%LOCALAPPDATA%/Android/jdk` (Temurin 21)
- SDK: `%LOCALAPPDATA%/Android/Sdk` (platform 36, build-tools 36.0.0)
- adb: `$ANDROID_HOME/platform-tools/adb`

## Aparelho principal

Samsung do Wander (One UI). Particularidades em `specs/05-permissoes-e-segundo-plano.md`.
