# 13 · Dúvidas em aberto

Respostas do Wander atualizam este arquivo e, quando for o caso, viram ADR em [12](12-decisoes.md).

## Bloqueiam o início da implementação

| # | Pergunta | Por que importa | Proposta |
|---|---|---|---|
| D1 | Qual é o **modelo do Samsung** e a **versão do One UI / Android**? (Configurações → Sobre o telefone → Informações do software) | Define o alvo dos testes, os deep links da bateria e o comportamento de suspensão | — |
| D2 | **Android Studio** está instalado? (o SDK 36 e o JDK 21 já estão nesta máquina) | Para abrir o projeto e depurar. Com o CLI dá para compilar e instalar mesmo sem ele | Instalar a versão estável |
| D3 | O Samsung pode ficar com a **depuração USB** ligada durante o desenvolvimento? | Instalar e testar com `adb` direto deste PC | Sim, só durante o desenvolvimento |

## Produto

| # | Pergunta | Proposta |
|---|---|---|
| D4 | **Monetização** na Play: grátis para sempre, grátis + Pro (pagamento único) ou doação? | Grátis na v1. Decidir na F4. Se houver Pro, usar Play Billing, o que **exige internet só para a biblioteca da Play**: rever o ADR-004 |
| D5 | PIN numérico basta, ou também quer **senha alfanumérica** e **padrão (desenho)**? | PIN na v1, senha na F3, padrão não |
| D6 | Proteger **Configurações e Play Store** deve vir **pré-selecionado** nos sugeridos? | Sugerido mas **desligado**. Bloquear Configurações pode irritar no dia a dia |
| D7 | A cortina deve mostrar o **ícone e o nome** do app bloqueado ou só a marca Vedra? | Mostrar por padrão, com a opção "ocultar" |
| D8 | Quer as funções de "uau" já na v1 (foto do intruso, disfarce de ícone)? | Não. Ficam na F5 |
| D9 | Idiomas na loja: pt-BR + en? Espanhol? | pt-BR + en na F4 |

## Empresa e publicação

| # | Pergunta | Proposta |
|---|---|---|
| D10 | **Razão social, CNPJ e e-mail de contato** da AuraDev para a política de privacidade e a ficha da loja | Usar os mesmos do Finora |
| D11 | Hospedar a política em `auradevcode.com.br/vedra/privacidade`? Quem publica no site? | Sim. Criar a página quando chegar a F4 |
| D12 | Fazer a **busca de marca "Vedra"** no INPI (classe 9) antes da loja? | Sim, na F4 |
| D13 | Usar o **quadro AuraDev no Trello** com uma etiqueta "Vedra", como o Finora? | Sim: criar a etiqueta e os cards das fases F0 a F4 |
| D14 | Chave de upload em `%OneDrive%\Develop\keystores\vedra\`, igual ao Finora? | Sim |

## Respondidas

| # | Pergunta | Resposta | Data |
|---|---|---|---|
| R1 | Nome | **Vedra** | 13/10/2026 |
| R2 | Rebloqueio | Configurável: imediato, por tempo ou ao apagar a tela | 13/10/2026 |
| R3 | Recuperação de PIN | Nenhuma além da digital | 13/10/2026 |
| R4 | Internet | Zero | 13/10/2026 |
| R5 | Linguagem | Kotlin nativo | 13/10/2026 |
| R6 | Repositório | GitHub `AuraDevCode`, privado | 13/10/2026 |
