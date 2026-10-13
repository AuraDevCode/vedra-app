# 06 · Segurança e ameaças

## Quem é o atacante

O atacante típico é **alguém com o celular desbloqueado na mão por alguns minutos**: um familiar, um colega ou um técnico. Ele é curioso, às vezes técnico, mas não tem ferramentas forenses. O Vedra **não** protege contra quem tem acesso root, ADB autorizado ou o aparelho por dias.

## Ameaças e respostas

| # | Ameaça | Risco | Resposta | Fase |
|---|---|---|---|---|
| A1 | Conteúdo aparece antes do bloqueio | Alto | Cortina síncrona (RNF-01), validada por gravação a 60 fps | v1 |
| A2 | Miniatura do app protegido na tela de recentes | Médio | Não controlamos o `FLAG_SECURE` de outros apps. Mitigação: ao abrir a tela de recentes com um app protegido na lista, a cortina cobre a tela de recentes (P1, com teste no One UI). Orientar o usuário a ativar a opção de privacidade do próprio app (ex.: WhatsApp → Privacidade → bloqueio do app) | v1 / P1 |
| A3 | Prévia de mensagens nas notificações | Médio | Fora do alcance do Vedra. A tela "Dicas" orienta a ocultar o conteúdo das notificações do app (Configurações → Notificações → Ocultar conteúdo) | v1 (dica) |
| A4 | Desligar o serviço de acessibilidade | Alto | Notificação imediata (`onUnbind`) e verificação periódica. P1: oferecer proteger o app Configurações (bloqueia quem tenta desligar) | v1 |
| A5 | Desinstalar o Vedra | Alto | P1: oferecer proteger Configurações e Play Store na lista de sugeridos. v2: estudar administrador do dispositivo, se a política da Play permitir | v1 / v2 |
| A6 | Forçar parada ou limpar dados do Vedra | Alto | Igual ao A5 (proteger Configurações). Limpar dados apaga a configuração, e a proteção some. Limitação aceita e documentada |
| A7 | Modo de segurança (Safe Mode) | Médio | Desliga apps de terceiros. Nenhum bloqueador resolve isso. Documentado |
| A8 | Força bruta no PIN | Alto | Atraso progressivo (RF-08) com relógio monotônico e contador que sobrevive ao reinício |
| A9 | Mudar a data e hora para pular a espera | Médio | Espera calculada com `elapsedRealtime`. Depois de reiniciar, o restante é retomado (gravado com `BOOT_COUNT`) |
| A10 | Cadastrar a própria digital e usar | Alto | Chave biométrica com `setInvalidatedByBiometricEnrollment(true)`. Uma nova digital invalida a chave e exige o PIN (RF-03) |
| A11 | Ver o PIN por cima do ombro | Médio | Teclado embaralhado opcional (RF-12) e sem mostrar os dígitos digitados |
| A12 | Teclado de terceiros gravando o PIN | Médio | Teclado próprio na tela de bloqueio (RF-06) |
| A13 | Captura de tela ou gravação da tela de bloqueio | Baixo | `FLAG_SECURE` em todas as telas do Vedra |
| A14 | Extrair o arquivo de dados e quebrar o hash fora do aparelho | Baixo | Hash PBKDF2 + HMAC com chave do Keystore que não sai do aparelho (pepper). Sem o aparelho, não dá para testar PINs offline |
| A15 | Backup e restauração para outro aparelho | Baixo | `allowBackup=false` e `dataExtractionRules` vazias |
| A16 | Abrir o app protegido por um caminho alternativo (atalho, link, compartilhar, widget) | Médio | O evento é de janela, não de launcher: qualquer caminho dispara o bloqueio. Coberto pela matriz de testes |
| A17 | Widget do app protegido na tela inicial mostrando conteúdo | Baixo | Fora do alcance. Dica para remover widgets de apps sensíveis |
| A18 | Pasta Segura e outros perfis | Baixo | Fora do escopo, documentado |
| A19 | O Android 17 com **Advanced Protection Mode** desliga serviços de acessibilidade que não são ferramentas de acessibilidade | Médio (futuro) | Detectar o modo e mostrar "Desprotegido" com a explicação. Plano de reserva: `UsageStatsForegroundSource` (ADR-002) |

## Criptografia

```
Criar PIN:
  salt      = SecureRandom(16 bytes)
  derived   = PBKDF2WithHmacSHA256(pin, salt, iterations = 210_000, 256 bits)
  stored    = HMAC-SHA256(keystoreKey "vedra_pin_pepper", derived)
  grava { salt, stored, iterations, version }

Verificar PIN:
  recalcula e compara com MessageDigest.isEqual (tempo constante)
```

- O número de iterações é calibrado no primeiro uso para **cerca de 150 ms** no aparelho (mínimo de 210.000).
- `vedra_pin_pepper`: chave HMAC no Android Keystore, não exportável, com StrongBox se disponível (`setIsStrongBoxBacked(true)` com fallback).
- Biometria: `BiometricPrompt` com `CryptoObject(Cipher)` usando a chave `vedra_bio`, com `setUserAuthenticationRequired(true)` e `setInvalidatedByBiometricEnrollment(true)`. O sucesso só vale se a operação de cifra também der certo.
- A digital **redefine o PIN** (decisão do produto): fluxo "Esqueci o PIN" → digital → criar PIN novo.

## Privacidade

- O serviço lê **apenas** o nome do pacote e da classe da janela em primeiro plano. Não lê texto, conteúdo, toques nem nada da tela (`canRetrieveWindowContent="false"`).
- Nada é enviado: o app não tem permissão de internet.
- O log local guarda só os eventos dos apps protegidos e expira em 7 dias.
- Política de privacidade curta e honesta, em pt-BR e inglês, publicada na página da AuraDev e copiada dentro do app (ver [09](09-play-store-e-conformidade.md)).
