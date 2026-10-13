# 01 · Visão do produto

## Problema

O celular fica desbloqueado na mão de outras pessoas: filho jogando, colega vendo uma foto, celular no conserto. Nesse momento, WhatsApp, banco, galeria e e-mail ficam abertos para qualquer um.

O Android não tem uma proteção nativa por app que seja simples:
- **Pasta Segura** (Samsung) e **Espaço privado** (Android 15) isolam apps, mas exigem duplicar o app, outra conta e outra rotina.
- **Apps de bloqueio da loja** costumam ter anúncios, pedir internet, coletar dados e deixar o conteúdo piscar antes do bloqueio.

## Proposta

O Vedra escolhe quais apps instalados ficam protegidos e exige PIN ou digital ao abri-los. Ele é:

- **Rápido:** o conteúdo do app protegido nunca aparece antes do desbloqueio.
- **Privado:** sem internet, sem conta, sem anúncio, sem rastreamento. Nada sai do aparelho.
- **Confiável:** mostra quando algo impede a proteção (permissão desligada, economia de bateria) e ensina a corrigir.
- **Premium:** visual AuraDev, escuro por padrão, animações discretas.

## Público

| Persona | Contexto | O que precisa |
|---|---|---|
| **Wander (usuário 1)** | Samsung próprio, usa WhatsApp e apps de banco | Proteção que funcione sempre, sem atrapalhar o uso |
| **Pai ou mãe** | Empresta o celular para a criança | Bloquear WhatsApp, galeria e compras, deixando os jogos livres |
| **Profissional** | Celular de trabalho com dados de clientes | Bloqueio rápido por digital, sem fricção |
| **Pessoa preocupada com privacidade** | Desconfia de apps de bloqueio "gratuitos" | Garantia de que não há internet nem coleta de dados |

## Objetivos (v1)

1. Bloquear qualquer app instalado com launcher, com desbloqueio por PIN e digital.
2. Cortina visível em **até 1 quadro (~16 ms)** depois do evento do sistema, sem vazamento visual do conteúdo (ver [03](03-requisitos-nao-funcionais.md)).
3. Rebloqueio configurável (imediato, por tempo ou ao apagar a tela).
4. Painel de **saúde** que diz, em linguagem simples, se a proteção está ativa e o que corrigir.
5. Funcionar de forma estável no Samsung do Wander por **7 dias seguidos** sem falha de bloqueio.
6. Estar pronto para a Play Store: políticas, declarações e textos.

## Indicadores de sucesso

| Indicador | Meta |
|---|---|
| Aberturas de app protegido sem bloqueio (falha) | 0 em 7 dias de uso real |
| Tempo do evento até a cortina (p95) | ≤ 16 ms |
| Tempo do toque no ícone até o pedido de digital (p95) | ≤ 300 ms |
| Consumo de bateria atribuído ao Vedra em 24 h | ≤ 1% |
| Falhas (crash) por 1.000 sessões | 0 (medido por log local) |

## Fora do escopo da v1

- iOS.
- Backend, conta de usuário, sincronização ou nuvem.
- Recuperação de PIN por e-mail ou pergunta de segurança (decisão do produto).
- Bloqueio de sites ou de partes de um app (ex.: um chat específico do WhatsApp).
- Controle parental com relatórios e limites de tempo.
- Monetização. A v1 é gratuita; o modelo será definido antes da Play Store (ver [13](13-duvidas-em-aberto.md)).

## Ideias para depois (v2+)

- Foto do intruso após N tentativas erradas (câmera frontal, só local).
- Disfarce: ícone e nome alternativos ou tela de "o app parou" falsa.
- Perfis (ex.: "Casa" e "Trabalho") e bloqueio por horário ou rede Wi-Fi.
- Bloqueio de Configurações e da Play Store para impedir desinstalação.
- Bloco rápido nas Configurações rápidas para pausar a proteção por X minutos.
- Widget de status.
