# 08 · Identidade visual

## Nome

**Vedra**, de *vedar*: fechar, impedir a passagem. Curto (5 letras, 2 sílabas), fácil de falar em português e inglês, e segue a família AuraDev (Finora, Kalenda, Movase): palavra inventada, terminada em vogal.

- Pacote: `br.com.auradevcode.vedra`
- Nome na loja: **Vedra: bloqueio de apps** (inglês: *Vedra: App Lock*)
- Slogan: **"Seus apps, só para você."** (inglês: *Your apps, only for you.*)
- Linha da marca: "Vedra · by AuraDev"

> ⚠️ Antes da Play Store: busca de marca no INPI (classe 9) e checagem na loja. Ver [13](13-duvidas-em-aberto.md).

## Logo

O símbolo é um **V** cujas hastes abraçam uma **fechadura**: o nome e a função num único sinal. Fica dentro de um quadrado arredondado com o gradiente da AuraDev (roxo → ciano), o mesmo DNA do ícone do Finora.

| Arquivo | Uso |
|---|---|
| [`brand/vedra-icon.svg`](../brand/vedra-icon.svg) | Ícone principal (quadrado arredondado) |
| [`brand/vedra-icon-fullbleed.svg`](../brand/vedra-icon-fullbleed.svg) | Base para o ícone de 512 px da Play (o Google aplica a máscara) |
| [`brand/vedra-glyph-mono.svg`](../brand/vedra-glyph-mono.svg) | Camada monocromática do ícone adaptativo (Android 13+), ícone de notificação |
| [`brand/vedra-glyph-gradient.svg`](../brand/vedra-glyph-gradient.svg) | Símbolo sobre fundo claro |
| [`brand/vedra-logo-horizontal.svg`](../brand/vedra-logo-horizontal.svg) | Símbolo + "Vedra" (Montserrat 800). Converter o texto em curvas antes de usar fora da web |
| [`brand/preview.html`](../brand/preview.html) | Página de visualização dos tamanhos |

**Regras**
- Área livre ao redor = altura da fechadura.
- Tamanho mínimo: 24 px (o V continua legível, e a fechadura vira detalhe).
- Não girar, não trocar as cores do gradiente, não pôr sombra no símbolo.

## Cores

Base da AuraDev (site auradevcode.com.br: preto profundo, roxo aura, ciano digital, verde neon), adaptada para app.

### Marca

| Token | Hex | Uso |
|---|---|---|
| `aura-purple` | `#8A2CE2` | Primária: botões, chaves ligadas, foco |
| `aura-purple-light` | `#B07CFF` | Primária no tema escuro sobre superfícies (texto e ícones) |
| `digital-cyan` | `#00E5FF` | Destaque, brilho da aura, links |
| `gradient-icon` | `#A13BFF → #6A3DF0 → #00C8E0` (135°) | Ícone, selo do painel, aura da tela de bloqueio |

### Tema escuro (padrão)

| Token | Hex |
|---|---|
| `background` | `#0A0A0A` |
| `surface` | `#141414` |
| `surface-variant` | `#1E1E1E` |
| `outline` | `#2A2A2A` |
| `on-background` | `#F2F2F2` |
| `on-surface-muted` | `#9A9A9A` |

### Tema claro

| Token | Hex |
|---|---|
| `background` | `#F6F7FB` |
| `surface` | `#FFFFFF` |
| `outline` | `#E3E5EC` |
| `on-background` | `#0B0B12` |
| `primary` | `#7A22D6` |

### Estados (painel de saúde)

| Estado | Escuro | Claro |
|---|---|---|
| Protegido | `#3DDC84` | `#1E9E5A` |
| Atenção | `#FFB020` | `#B87400` |
| Desprotegido | `#FF4D5E` | `#D92D3F` |

Todos os pares de texto e fundo devem passar no **WCAG AA** (≥ 4,5:1). Validar no Compose com um teste de contraste.

## Tipografia

| Papel | Fonte | Peso |
|---|---|---|
| Títulos e marca | **Montserrat** (empacotada em `res/font`, só os pesos 600, 700 e 800) | 700/800 |
| Texto | **Roboto** (padrão do Android, zero custo) | 400/500 |
| Números de diagnóstico | **JetBrains Mono** (opcional, só na tela de diagnóstico) | 400 |
| Dígitos do PIN | Montserrat | 600 |

Mesma combinação do Finora (Montserrat + Roboto), coerente com o site da AuraDev (Montserrat + JetBrains Mono).

## Formas e movimento

- Raio: cartões de 20 dp, botões de 14 dp, chips de 10 dp.
- Elevação por cor (superfícies mais claras), não por sombra, no tema escuro.
- **Aura:** brilho radial roxo e ciano, com 20% de opacidade e desfoque, atrás do selo e na tela de bloqueio. Respira (pulso de 4 s) só no selo "Protegido".
- Movimentos curtos (120 a 250 ms), curva *emphasized* do Material 3. Tudo desligado se "Remover animações" estiver ativo.

## Ícones Android (o que gerar na implementação)

| Recurso | Especificação |
|---|---|
| Ícone adaptativo | `ic_launcher_background`: gradiente vetorial. `ic_launcher_foreground`: símbolo branco dentro da zona segura de 66 dp (de 108 dp). `ic_launcher_monochrome`: o mesmo símbolo |
| Ícone de notificação | `ic_stat_vedra`: símbolo branco, 24 dp, só o alfa |
| Play Store | Ícone PNG de 512×512 (32 bits, sem cantos, o Google arredonda). Gráfico de recursos de 1024×500: fundo `#0A0A0A`, aura e logo horizontal + slogan |
| Splash (`SplashScreen` API) | Fundo `#0A0A0A`, símbolo em gradiente, sem marca abaixo |
