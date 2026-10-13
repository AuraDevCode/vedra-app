# 07 · UX, fluxos e telas

Tom de voz: **direto, calmo e confiante**. Frases curtas, sem jargão técnico e sem medo artificial ("Seu celular está em risco!" ❌ → "O WhatsApp está protegido." ✅). Sempre em "você".

## Mapa de telas

```
Primeiro uso ──► Início (painel de saúde) ──┬──► Apps protegidos
                                            ├──► Configurações ──► Trocar PIN · Digital · Rebloqueio · Tema · Diagnóstico · Sobre
                                            └──► Dicas de privacidade
Tela de bloqueio (aberta pelo serviço, fora da navegação)
Cortina (overlay, sem interação)
```

## Fluxo de primeiro uso

| # | Tela | Conteúdo | Ação principal |
|---|---|---|---|
| 1 | Boas-vindas | Logo animada, "Seus apps, só para você." e três pontos: rápido, privado, sem internet | **Começar** |
| 2 | Criar PIN | Teclado próprio, 6 pontos, link "Usar 4 a 8 dígitos" | (automático ao completar) |
| 3 | Confirmar PIN | Igual. Se não bater, tremida e volta ao passo 2 | — |
| 4 | Digital | "Desbloqueie com a digital?" Teste real com `BiometricPrompt` | **Usar digital** / Agora não |
| 5 | Aviso de acessibilidade | Texto obrigatório (abaixo) | **Concordo e continuar** / Agora não |
| 6 | Ativar o serviço | Ilustração passo a passo do caminho no Samsung. No Android 13+ por APK, inclui antes "Permitir configurações restritas" | **Abrir configurações** |
| 7 | Bateria (Samsung) | Dois passos: "Sem restrições" e "Apps nunca suspensos", cada um com o botão de atalho e uma caixa "Já fiz" | **Continuar** |
| 8 | Notificações | "Avisamos se a proteção cair." | **Permitir** / Pular |
| 9 | Escolher apps | Lista com a seção "Sugeridos" no topo, já pré-selecionada (WhatsApp, Galeria, bancos detectados) | **Proteger N apps** |
| 10 | Pronto | "Tudo pronto. Abra o WhatsApp para testar." | **Ir para o início** |

### Texto do aviso de acessibilidade (passo 5)

> **Por que o Vedra usa a Acessibilidade**
>
> Para bloquear um app no instante em que ele abre, o Vedra precisa saber **qual app está na tela**. O Android só oferece isso de forma imediata pelo serviço de Acessibilidade.
>
> **O que o Vedra vê:** apenas o nome do app que abriu (ex.: "WhatsApp").
> **O que o Vedra não vê:** mensagens, textos, fotos, senhas, toques ou qualquer conteúdo da tela.
> **O que é guardado ou enviado:** nada. O Vedra não tem acesso à internet.
>
> Você pode desligar quando quiser em Configurações → Acessibilidade.
>
> [ Concordo e continuar ]   [ Agora não ]

## Início (painel de saúde)

```
┌──────────────────────────────┐
│ Vedra                    ⚙️  │
│                              │
│      ╭──────────────╮        │
│      │   🛡 (V)      │  aura  │  ← selo animado (glow suave) verde, amarelo ou vermelho
│      ╰──────────────╯        │
│        Protegido             │
│   7 apps · rebloqueio imediato│
│                              │
│  ✓ Acessibilidade ativa      │
│  ✓ Bateria sem restrições    │
│  ! Notificações desligadas  [Corrigir]
│                              │
│  Último bloqueio: WhatsApp,  │
│  há 3 min · 4 ms             │
│                              │
│ [  Gerenciar apps  ]         │
└──────────────────────────────┘
```

## Apps protegidos

- Busca fixa no topo e filtros: Todos · Protegidos · Sugeridos.
- Item: ícone, nome, chave. Toque longo abre o rebloqueio por app (P1).
- Ao ligar a primeira chave sem o serviço ativo, mostra um aviso para ativar a proteção.

## Tela de bloqueio

```
┌──────────────────────────────┐
│                              │   fundo #0A0A0A com aura em gradiente (roxo → ciano), desfocada
│        [ícone WhatsApp]      │   (ou o logo Vedra, se "ocultar nome" estiver ligado)
│          WhatsApp            │
│        ● ● ● ○ ○ ○           │
│                              │
│      1     2     3           │
│      4     5     6           │
│      7     8     9           │
│     [👆]   0     ⌫           │
│                              │
│        Esqueci o PIN         │
└──────────────────────────────┘
```

- O pedido de digital abre **automaticamente** por cima. Cancelar mostra o teclado.
- Erro: tremida horizontal de 300 ms + vibração curta.
- Espera por tentativas: teclado desabilitado, contagem regressiva "Tente em 0:28" e a digital continua disponível.

## Cortina

- Fundo sólido `#0A0A0A` + logo Vedra centralizado (escala 0,9 → 1 em 120 ms, só se o desafio demorar mais de 150 ms).
- **Sem** conteúdo dinâmico pesado: é desenhada em menos de 1 quadro.

## Estados de erro e de vazio

| Situação | Mensagem |
|---|---|
| Nenhum app protegido | "Nenhum app protegido ainda. Escolha quais apps só você pode abrir." + botão |
| Serviço desligado | Selo vermelho: "Proteção desligada. Ative a Acessibilidade para o Vedra voltar a proteger seus apps." |
| Digital invalidada | "Uma nova digital foi cadastrada no aparelho. Por segurança, digite seu PIN." |
| Sem digital cadastrada | "Cadastre uma digital no Android para desbloquear mais rápido." |

## Acessibilidade da própria UI

- Contraste AA em todos os textos (ver as cores em [08](08-identidade-visual.md)).
- Teclado de PIN com `contentDescription` em cada tecla. TalkBack anuncia "3 de 6 dígitos".
- Áreas de toque ≥ 48 dp. Suporte a fonte em 200%.
- Respeitar "Remover animações" do sistema.
