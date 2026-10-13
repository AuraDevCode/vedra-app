# Vedra — especificação

> **Vedra** (de *vedar*: fechar, impedir o acesso). Um app Android da AuraDev que bloqueia outros apps com PIN ou digital, sem deixar o conteúdo aparecer antes do desbloqueio.

- **Status:** especificação (nenhum código escrito ainda)
- **Dono do produto:** Wander (AuraDev)
- **Pacote:** `br.com.auradevcode.vedra`
- **Repositório:** `AuraDevCode/vedra-app` (privado)
- **Primeiro aparelho:** Samsung do Wander (One UI). Depois: Play Store, conta de organização AuraDevCode.
- **Criado em:** 13/10/2026

## Documentos

| # | Documento | Para que serve |
|---|---|---|
| 01 | [Visão do produto](01-visao-do-produto.md) | Problema, público, objetivos, o que está fora do escopo |
| 02 | [Requisitos funcionais](02-requisitos-funcionais.md) | O que o app faz, com critérios de aceite |
| 03 | [Requisitos não funcionais](03-requisitos-nao-funcionais.md) | Velocidade, bateria, segurança, compatibilidade, com números |
| 04 | [Arquitetura](04-arquitetura.md) | Stack, módulos, componentes, máquina de estados e dados |
| 05 | [Permissões e segundo plano](05-permissoes-e-segundo-plano.md) | Cada permissão: por que, como pedir e como validar. Particularidades da Samsung |
| 06 | [Segurança e ameaças](06-seguranca-e-ameacas.md) | Como alguém tentaria burlar o bloqueio e o que fazemos contra |
| 07 | [UX, fluxos e telas](07-ux-fluxos-e-telas.md) | Primeiro uso, telas, textos e estados |
| 08 | [Identidade visual](08-identidade-visual.md) | Nome, logo, cores, tipografia e ícones Android |
| 09 | [Play Store e conformidade](09-play-store-e-conformidade.md) | Políticas do Google, declarações, segurança dos dados e LGPD |
| 10 | [Qualidade e testes](10-qualidade-e-testes.md) | Estratégia de testes, matriz de aparelhos e como medir a velocidade |
| 11 | [Roadmap](11-roadmap.md) | Fases, marcos e critério de pronto de cada fase |
| 12 | [Decisões (ADR)](12-decisoes.md) | Decisões tomadas e por quê |
| 13 | [Dúvidas em aberto](13-duvidas-em-aberto.md) | O que ainda depende do Wander |

## Decisões já confirmadas pelo Wander (13/10/2026)

1. Nome **Vedra**.
2. Rebloqueio **configurável**: imediato ao sair do app, após um tempo, ou só ao apagar a tela.
3. **Sem recuperação** de PIN por e-mail ou pergunta: a digital redefine o PIN. Sem PIN nem digital, não há como entrar.
4. **Zero internet**: o app não pede a permissão `INTERNET`.
5. Linguagem **Kotlin nativo** (Jetpack Compose).

## Glossário

| Termo | Significado |
|---|---|
| App protegido | App que o usuário marcou para bloquear (ex.: WhatsApp) |
| Cortina | Janela opaca desenhada pelo serviço de acessibilidade sobre o app protegido, antes de qualquer conteúdo aparecer |
| Tela de bloqueio | Tela do Vedra que pede PIN ou digital |
| Sessão liberada | Período em que um app protegido, depois de desbloqueado, não pede senha de novo |
| Credencial mestra | O PIN (ou senha) do Vedra. A digital é um atalho para ela |
| Saúde do app | Conjunto de verificações (permissões, bateria, serviço ativo) que indica se a proteção está funcionando |
