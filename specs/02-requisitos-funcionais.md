# 02 · Requisitos funcionais

Prioridades: **P0** = sem isso não existe v1 · **P1** = entra na v1 · **P2** = depois da v1.

## RF-01 · Primeiro uso e consentimento (P0)

O primeiro uso guia o usuário em etapas: boas-vindas → criar PIN → digital (opcional) → aviso de acessibilidade → ativar o serviço → bateria → escolher apps.

**Aceite**
- O aviso de acessibilidade é uma tela própria, separada de qualquer outro aviso. Explica o que é lido (só o nome do pacote do app em primeiro plano), para que serve, e diz que nada é guardado nem enviado. Tem botões **"Concordo e continuar"** e **"Agora não"** (exigência da Play Store, ver [09](09-play-store-e-conformidade.md)).
- O app só abre as configurações de acessibilidade depois do "Concordo".
- Se o usuário sair no meio, o onboarding retoma da etapa pendente.
- Ao voltar das configurações do sistema, o app detecta sozinho se a permissão foi concedida e avança.

## RF-02 · Credencial mestra (P0)

- PIN numérico de **4 a 8 dígitos** (padrão: 6). Opção P1: senha alfanumérica.
- Confirmação digitando duas vezes.
- Guardado só como hash (ver [06](06-seguranca-e-ameacas.md)). Nunca em texto.

**Aceite**: PIN fraco (`1234`, `0000`, `123456`, sequência ou repetição) mostra um alerta, mas permite continuar (P1: bloquear de vez com opção nas configurações).

## RF-03 · Desbloqueio por biometria (P0)

- Usa `BiometricPrompt` com classe **BIOMETRIC_STRONG** (digital nos Samsung).
- A biometria é atalho, não substituto: o PIN sempre funciona.
- Se uma nova digital for cadastrada no aparelho, a biometria do Vedra é invalidada e o próximo desbloqueio exige o PIN. Depois disso, o Vedra oferece reativar a digital.

**Aceite**: com a digital cadastrada, abrir um app protegido mostra o pedido de digital automaticamente, sem toque extra.

## RF-04 · Escolher apps protegidos (P0)

- Lista os apps com ícone no launcher: ícone, nome e chave liga/desliga.
- Busca por nome. Ordem: protegidos primeiro, depois em ordem alfabética.
- Seção **"Sugeridos"**, com os apps sensíveis que estiverem instalados: WhatsApp, WhatsApp Business, Telegram, Galeria (Samsung e Google Fotos), Gmail, Samsung Notes, Meus Arquivos, bancos conhecidos, Play Store e Configurações.
- Não lista o próprio Vedra (ele é sempre protegido, RF-10) nem o launcher.

**Aceite**: ativar a chave protege o app na hora, sem reiniciar o serviço.

## RF-05 · Bloqueio na abertura (P0)

Quando um app protegido vai para o primeiro plano sem sessão liberada:
1. A **cortina** cobre a tela imediatamente (fundo da marca, logo e ícone do app protegido).
2. A tela de bloqueio aparece por cima (PIN e digital).
3. Se o desbloqueio der certo, a cortina some e o app fica liberado conforme a regra de rebloqueio.
4. Voltar ou ir para o Início sem desbloquear leva para a tela inicial, e o app continua protegido.

**Aceite**
- Nenhum quadro do conteúdo do app protegido fica visível (validado por gravação de tela a 60 fps, ver [10](10-qualidade-e-testes.md)).
- Vale ao abrir pelo ícone, pelos recentes, por notificação, por atalho, por link ou por outro app (ex.: compartilhar → WhatsApp).
- Vale em tela dividida e em janela pop-up (Samsung): nesses casos, a cortina cobre a tela inteira.

## RF-06 · Desbloqueio por PIN (P0)

- Teclado numérico próprio (não usa o teclado do sistema, para evitar teclados que gravam o que é digitado).
- Libera assim que o PIN completo é digitado, sem botão "OK".
- Vibração curta e animação de tremida quando o PIN está errado.

## RF-07 · Regra de rebloqueio (P0)

Configuração **global**, com opção **por app** (P1):

| Opção | Comportamento |
|---|---|
| **Imediato** | Saiu do app, pede de novo ao voltar (com 5 s de tolerância só para recentes e Início, ADR-015) |
| **Após 10 s / 30 s / 1 / 5 / 15 / 30 min** (padrão: **1 min**) | Fica liberado esse tempo depois de sair do app |
| **Só ao apagar a tela** | Fica liberado até a tela apagar |

- Apagar a tela **sempre** encerra todas as sessões, em qualquer opção.
- Alternar para o próprio teclado, um seletor de arquivos ou a câmera chamada pelo app protegido **não** conta como sair (ver a lista de exceções em [04](04-arquitetura.md#regra-de-saída)).

## RF-08 · Tentativas erradas (P0)

| Erros seguidos | Efeito |
|---|---|
| 5 | Espera de 30 s |
| 10 | Espera de 5 min |
| 15 ou mais | Espera de 15 min a cada nova tentativa errada |

- A contagem sobrevive ao reinício do app e do aparelho, e usa o relógio monotônico (`elapsedRealtime`) para que mudar a hora do sistema não burle a espera.
- A digital continua disponível durante a espera (o sistema já tem o próprio limite para ela).

## RF-09 · Proteção do próprio Vedra (P0)

- Abrir o Vedra sempre pede a credencial.
- Mudar a configuração, desativar a proteção ou trocar o PIN pede a credencial de novo, mesmo dentro do app.

## RF-10 · Painel de saúde (P0)

A tela inicial mostra um selo grande: **"Protegido"** (verde), **"Atenção"** (amarelo) ou **"Desprotegido"** (vermelho). Embaixo, a lista de verificações, cada uma com um botão **"Corrigir"** que abre a tela certa do sistema. Detalhes em [05](05-permissoes-e-segundo-plano.md#painel-de-saude).

## RF-11 · Pausar a proteção (P1)

Pausa por 5, 15 ou 60 minutos, ou até reativar. Exige credencial. Durante a pausa, uma notificação persistente avisa e oferece reativar.

## RF-12 · Configurações (P0/P1)

- Trocar o PIN (P0), ligar ou desligar a digital (P0), regra de rebloqueio (P0).
- Vibração ao errar (P1), embaralhar o teclado numérico (P1), ocultar o nome do app protegido na cortina (P1).
- Tema: escuro (padrão), claro ou do sistema (P1).
- Diagnóstico: exportar o log local pelo menu compartilhar (P1, ver RF-14).
- Sobre: versão, política de privacidade (texto local) e licenças.

## RF-13 · App novo instalado (P2)

Quando um app é instalado, mostra uma notificação "Proteger o <app>?" com a ação "Proteger".

## RF-14 · Log local de diagnóstico (P1)

- Guarda localmente os últimos 7 dias de eventos técnicos: serviço ligado ou desligado, bloqueio exibido, tempo até a cortina, falha.
- **Nunca** guarda o PIN nem o conteúdo de telas. O nome do pacote é guardado só para os apps protegidos.
- Exportado só por ação do usuário (compartilhar), já que não temos internet. É assim que vamos depurar o Samsung.

## RF-15 · Idioma (P0)

pt-BR na v1. Todos os textos ficam em `strings.xml` desde o primeiro dia, para traduzir para inglês e espanhol na fase da loja.
