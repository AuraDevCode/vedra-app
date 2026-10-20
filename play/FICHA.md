# Vedra · Play Console (teste fechado)

> **Status (20/10/2026):** enviado para revisão do Google. Teste fechado "Alpha", versão 0.1.8 (versionCode 8), Brasil, testadores = lista "Finora - Wander".
> App no Console: `developers/4776757094705934860/app/4973697798366956468`.
> Vídeo da declaração de acessibilidade: https://auradevcode.github.io/vedra-app/demo-acessibilidade.mp4
> Contato público da ficha: aquinowander3@gmail.com · site https://auradevcode.com.br (trocar por um e-mail da AuraDev antes da produção, se quiser).
> "Detalhes do login": **Sim**, com instruções em inglês (o revisor cria o próprio PIN). Responder "Não" fez a verificação automática acusar erro.

Tudo o que vai para o Play Console, pronto para colar. Contexto e políticas em [`specs/09`](../specs/09-play-store-e-conformidade.md).

## App

| Campo | Valor |
|---|---|
| Conta | Organização **AuraDevCode** |
| Nome do app | Vedra: bloqueio de apps |
| Idioma padrão | Português (Brasil) – pt-BR |
| App ou jogo | App |
| Gratuito ou pago | Gratuito |
| Pacote | `br.com.auradevcode.vedra` |
| Política de privacidade | https://auradevcode.github.io/vedra-app/privacidade/ |
| Categoria | Ferramentas |
| E-mail de contato | (o mesmo da conta AuraDevCode) |
| Site | https://auradevcode.com.br |

## Ficha da loja (pt-BR)

**Nome (30):** `Vedra: bloqueio de apps`

**Descrição curta (80):**
```
Bloqueie apps com PIN ou digital. Rápido, privado e sem internet.
```

**Descrição completa:**
```
O Vedra protege os apps que você escolher, como WhatsApp, galeria, banco e e-mail, com PIN ou digital.

⚡ INSTANTÂNEO
Ao abrir um app protegido, o Vedra cobre a tela na hora, antes de qualquer conteúdo aparecer, e só então pede o seu PIN ou a sua digital.

🔒 PRIVADO DE VERDADE
• Sem permissão de internet: nada sai do seu celular.
• Sem conta, sem anúncios, sem rastreamento.
• O PIN é guardado só como hash criptográfico, protegido pelo Keystore do Android.

👆 PIN OU DIGITAL
A digital é um atalho; o PIN sempre funciona. Se alguém cadastrar uma digital nova no aparelho, o Vedra passa a exigir o PIN.

⏱️ REBLOQUEIO DO SEU JEITO
Peça de novo imediatamente ao sair do app, depois de 30 segundos a 30 minutos, ou só quando a tela apagar.

🩺 PAINEL DE SAÚDE
Mostra se a proteção está ativa e ensina a corrigir o que estiver errado: acessibilidade, bateria e uso em segundo plano.

🛡️ CONTRA TENTATIVAS
Depois de vários PINs errados, o Vedra impõe uma espera crescente, que não pode ser burlada mudando a hora do aparelho.

POR QUE O VEDRA USA A ACESSIBILIDADE
Para saber, no instante em que um app abre, se ele deve ser bloqueado. O Vedra lê apenas o nome do app em primeiro plano. Ele não lê mensagens, textos, fotos, senhas nem nada do que aparece na tela, e não age em outros apps.

Feito no Brasil pela AuraDev.
```

**Gráficos** (pasta [`graficos/`](graficos)):
- Ícone: `icone-512.png`
- Gráfico de recursos: `recurso-grafico-1024x500.png`
- Capturas do telefone: `captura-1.png` a `captura-4.png` (1080×1920)

## Conteúdo do app (Política → Conteúdo do app)

### Acesso ao app
> Todas as funcionalidades estão disponíveis sem acesso especial.

Instruções (se pedir): "Não há login. No primeiro uso, o próprio revisor cria um PIN de 4 a 8 dígitos. Para testar o bloqueio: ative o serviço Vedra em Configurações → Acessibilidade, escolha um app em 'Gerenciar apps' e abra esse app."

### Anúncios
> Não, o app não contém anúncios.

### Classificação do conteúdo (IARC)
- Categoria: **Utilitário, produtividade, comunicação ou outro**
- Todas as perguntas sobre violência, sexo, linguagem, drogas, jogos de azar: **Não**
- Interação entre usuários / compartilha localização / compras digitais: **Não**
- Resultado esperado: Livre

### Público-alvo
- Faixa etária: **18 anos ou mais**
- Pode atrair crianças sem querer: **Não**

### Segurança dos dados
| Pergunta | Resposta |
|---|---|
| O app coleta ou compartilha algum dos tipos de dados do usuário exigidos? | **Não** |

> Explicação (caso o formulário peça): o Vedra não tem permissão de internet. A lista de apps protegidos, o hash do PIN e as configurações ficam só no aparelho. Dados processados apenas no dispositivo não contam como "coletados" pela definição do Google.

### Apps governamentais / financeiros / saúde / notícias
> Não se aplica a nenhum.

### API de Acessibilidade (Permissões de apps → Acessibilidade)

**O app é uma ferramenta de acessibilidade?** Não (`isAccessibilityTool="false"`).

**Recurso principal que usa a API:**
```
Bloqueio de apps escolhidos pelo usuário. O Vedra usa o AccessibilityService apenas para receber o evento TYPE_WINDOW_STATE_CHANGED e ler o nome do pacote do app em primeiro plano. Quando esse app está na lista que o próprio usuário escolheu, o Vedra cobre a tela e pede o PIN ou a digital antes que o conteúdo apareça. O serviço não lê o conteúdo das janelas (canRetrieveWindowContent="false"), não executa ações em outros apps e não coleta nem transmite dados: o app não tem permissão de internet.
```

**Aviso em destaque:** antes de abrir as configurações de acessibilidade, o app mostra a tela "Por que o Vedra usa a Acessibilidade", separada de qualquer outro aviso, com "Concordo e continuar" e "Agora não".

**Vídeo:** [`demo-acessibilidade.mp4`](demo-acessibilidade.mp4). Mostra o aviso, a ativação do serviço, a escolha de um app e o bloqueio. Enviar ao YouTube como **não listado** e colar o link.

## Teste fechado

- Testar e lançar → Teste → **Teste fechado** → faixa "Alpha" (ou criar "Vedra · testadores").
- Testadores: lista de e-mails "Vedra · testadores" (começar com a conta Google do Wander).
- Países: Brasil.
- Conta de organização: **não** se aplica a regra de 12 testadores por 14 dias.

**Notas da versão (pt-BR):**
```
Primeira versão de teste do Vedra.
• Bloqueio instantâneo com PIN ou digital
• Rebloqueio configurável
• Painel de saúde da proteção
```
