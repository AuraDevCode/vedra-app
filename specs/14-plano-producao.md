# 14 · Plano de subida para produção

Criado em 21/10/2026. Situação atual: teste fechado "Alpha" aprovado com a 0.1.8 e 0.1.12 em revisão. Testadores: Wander + lista "primeiros" (5 pessoas).

## Critério para ir para produção

Só publica em produção quando **tudo** abaixo for verdade:

| # | Critério | Como verificar |
|---|---|---|
| C1 | Nenhuma falha de bloqueio relatada pelos testadores por 7 dias | Issues no GitHub + conversa com os testadores |
| C2 | Android vitals sem falhas (crash) nem ANR acima do limite | Play Console → Android vitals. **Funciona sem a permissão de internet**: quem coleta é o próprio sistema e a Play |
| C3 | Testado em pelo menos 3 fabricantes (Samsung + 2 outros) | Lista de aparelhos dos testadores |
| C4 | Itens obrigatórios da v0.2 entregues (abaixo) | Roadmap |
| C5 | Declaração de acessibilidade aceita | Já aceita no teste fechado ✅ |
| C6 | Decisão de monetização tomada | D4 em [13](13-duvidas-em-aberto.md) |

## v0.2: o que entra antes da produção

| Prioridade | Item | Por quê |
|---|---|---|
| 🔥 | **Aviso "Proteção desligada"**: notificação quando o serviço cair (`onUnbind`) + WorkManager a cada 15 min | Na Samsung o sistema pode derrubar o serviço. Hoje o usuário só descobre quando um app abre sem bloqueio |
| 🔥 | **Guia de bateria da Samsung** no primeiro uso ("Sem restrições" e "Apps nunca suspensos") | Principal causa de "parou de funcionar" em bloqueadores |
| 🔥 | **Tela de bloqueio mais rápida**: tela de PIN pré-carregada e tema sem animação | Hoje passam ~0,7 s entre a cortina e o teclado (meta: 0,3 s) |
| Alta | **Proteger as Configurações do Android** como sugestão (desligada por padrão) | Impede que outra pessoa desligue o Vedra |
| Alta | **Permissão de notificação** (Android 13+) no primeiro uso | Necessária para o aviso acima |
| Média | Opção "desbloquear um libera todos" | Pedido comum em bloqueadores |
| Média | Rebloqueio por app (WhatsApp imediato, galeria 5 min) | Estava na F3 |
| Média | E-mail de contato da AuraDev na ficha (no lugar do pessoal) | O contato fica público na produção |

**Fica para depois da produção (v0.3+):** inglês e espanhol, foto do intruso, ícone disfarçado, bloco nas Configurações rápidas, perfis por horário ou Wi-Fi, tema claro, widget.

## Cronograma sugerido

| Data | Etapa |
|---|---|
| 21/10 | 0.1.12 no teste fechado (rebloqueio) + lista "primeiros" ✅ |
| 22–24/10 | Desenvolvimento da v0.2 (itens 🔥 e Alta) |
| 24/10 | v0.2 no teste fechado |
| 25/10–01/11 | Uso real pelos 6 testadores (7 dias). Coletar aparelhos, falhas e impressões |
| 02/11 | Revisão dos critérios C1–C6. Correções finais |
| 03/11 | **Produção: lançamento gradual** (abaixo) |

## Como será o lançamento em produção

1. Play Console → **Produção** → Países: **Brasil**. Expandir depois, junto com a tradução.
2. Promover a versão aprovada do teste fechado (mesmo AAB, sem recompilar).
3. **Lançamento gradual (staged rollout):** 10% → 2 dias → 50% → 3 dias → 100%. Pausar se o Android vitals piorar.
4. A conta de organização **não** exige os 14 dias de teste com 12 testadores (isso vale só para contas pessoais novas).
5. Revisão da produção: normalmente de algumas horas a poucos dias. A acessibilidade já foi aceita.

## Riscos

| Risco | Mitigação |
|---|---|
| Revisor da produção ser mais rígido com a acessibilidade | Mesmo texto e vídeo já aceitos. Plano B na [09](09-play-store-e-conformidade.md) |
| Avaliações ruins por "parou de funcionar" (Samsung, Xiaomi) | Aviso de proteção desligada + guia de bateria (v0.2) |
| Usuário esquecer o PIN sem digital cadastrada | Aviso claro no primeiro uso (já existe) e na ficha |
| Proteção Avançada do Android 17 | Detectar e explicar no painel (v0.3) |
