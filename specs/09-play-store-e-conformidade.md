# 09 · Play Store e conformidade

## Onde publicar

- Play Console da organização **AuraDevCode** (a mesma do Finora).
- Começar pelo **teste interno** (como o Finora), depois teste fechado e então produção.
- Conta de organização: não se aplica a regra de "12 testadores por 14 dias", que vale só para contas pessoais novas.

## O ponto crítico: política da API de Acessibilidade

O Google restringe o uso da `AccessibilityService` por apps que não são ferramentas para pessoas com deficiência. O Vedra **não é** uma ferramenta de acessibilidade, então:

| Exigência | Como cumprimos |
|---|---|
| **Não** declarar `isAccessibilityTool="true"` | Fica `false` (declarar em falso causa recusa e bloqueio pelo Play Protect) |
| **Aviso em destaque e consentimento** dentro do app, antes de ativar, separado de outros avisos, com ação afirmativa | Tela própria no passo 5 do primeiro uso (ver [07](07-ux-fluxos-e-telas.md#texto-do-aviso-de-acessibilidade-passo-5)), com "Concordo e continuar" |
| O aviso descreve que dado é lido, como é usado e se é compartilhado | "Só o nome do app em primeiro plano, não guardado, não enviado" |
| **Declaração de acessibilidade** no Play Console | Preencher: finalidade = bloquear apps escolhidos pelo usuário, sem coleta nem compartilhamento |
| Vídeo de demonstração (o Google costuma pedir) | Gravar: aviso → ativação → abrir o WhatsApp → bloqueio. Link no YouTube não listado |
| Uso determinístico, sem automação autônoma | O Vedra só **reage** à troca de janela. Não clica, não lê nem age no app protegido |

**Risco de recusa: médio.** Existem bloqueadores na Play que usam acessibilidade, mas cada revisão é individual.

**Plano de reserva (ADR-002):** variante `playsafe` usando `UsageStatsManager` (permissão "Acesso ao uso") + cortina com `SYSTEM_ALERT_WINDOW`. É mais lenta (polling de 150 ms) e pode deixar o conteúdo aparecer por um instante. Só vale se a variante principal for recusada.

> ⚠️ Atenção ao **Android 17 com Proteção Avançada** ligada: o sistema revoga a acessibilidade de apps que não são ferramentas de acessibilidade. A maioria dos usuários não liga esse modo, mas o Vedra precisa detectar e explicar (A19 em [06](06-seguranca-e-ameacas.md)).

## Outras políticas

| Tema | Situação |
|---|---|
| `QUERY_ALL_PACKAGES` | **Não usamos.** `<queries>` com `MAIN/LAUNCHER` basta para listar os apps com ícone |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | **Não usamos.** Abrimos a tela da lista do sistema, que não exige permissão |
| Serviço em primeiro plano | Não na v1. Se entrar (plano B), tipo `specialUse` com justificativa no Console |
| Administrador do dispositivo | Não na v1. Estudar a política antes da v2 |
| API alvo | `targetSdk 36`. Atualizar todo ano, conforme a exigência do Google (agosto) |
| Páginas de 16 KB | Sem código nativo, nada a fazer. Conferir se alguma dependência traz `.so` |
| Público-alvo | **18+** (evita a política de Famílias, que é mais rígida). Revisar se o produto virar controle parental |
| Classificação de conteúdo | Questionário IARC: utilitário, sem conteúdo sensível → Livre |
| Acesso ao app (para revisores) | Não há login: o revisor cria o próprio PIN no primeiro uso. Explicar isso no campo "Acesso ao app" |

## Segurança dos dados (formulário da Play)

| Pergunta | Resposta |
|---|---|
| O app coleta dados do usuário? | **Não.** Nada sai do aparelho (sem permissão de internet) |
| Compartilha dados com terceiros? | **Não** |
| Dados criptografados em trânsito? | Não se aplica (sem tráfego) |
| O usuário pode pedir a exclusão? | Desinstalar ou limpar os dados apaga tudo |

Observação: pela definição do Google, dado processado só no aparelho **não** é "coletado".

## Política de privacidade (obrigatória)

- Publicar em `https://auradevcode.com.br/vedra/privacidade` (pt-BR) e `/vedra/privacy` (en).
- Conteúdo: o que é lido (nome do app em primeiro plano), finalidade, sem armazenamento remoto, sem internet, sem terceiros, uso da biometria (o Vedra nunca recebe a digital, só o resultado do sistema), contato `contato@auradevcode.com.br` (confirmar), controlador AuraDev (dados LGPD: confirmar razão social e CNPJ).
- A mesma política em texto dentro do app (Configurações → Sobre).

## LGPD

Como nada é coletado nem tratado fora do aparelho, o impacto é mínimo. Mesmo assim:
- Política de privacidade clara (acima) e canal de contato.
- Registro interno da decisão "zero internet" como medida de privacidade desde a concepção (este documento).

## Assinatura e versões

- **Assinatura de apps do Google Play** ativada. Chave de upload em `%OneDrive%\Develop\keystores\vedra\`, no mesmo padrão do Finora (`LEIA-ME.md` + `.credentials.json`), **nunca** no repositório.
- `versionCode` = `git rev-list --count HEAD`. `versionName` = `1.0.<count>-<sha curto>`.
- AAB gerado no CI (ver [10](10-qualidade-e-testes.md)). Envio automático pela conta de serviço da Play: fase posterior.

## Ficha da loja (rascunho)

- **Título (30):** Vedra: bloqueio de apps
- **Descrição curta (80):** Bloqueie apps com PIN ou digital. Rápido, privado e sem internet.
- **Destaques da descrição longa:** bloqueio instantâneo sem "piscar" o conteúdo · digital · rebloqueio configurável · sem anúncios · sem internet · sem coleta de dados · feito no Brasil pela AuraDev.
- **Capturas:** painel protegido, lista de apps, tela de bloqueio, configurações de rebloqueio, aviso de privacidade.
