# 03 · Requisitos não funcionais

## Desempenho (o requisito número 1)

| ID | Requisito | Meta | Como medir |
|---|---|---|---|
| RNF-01 | Do evento `TYPE_WINDOW_STATE_CHANGED` até a cortina adicionada ao `WindowManager` | p95 ≤ 16 ms | `SystemClock.elapsedRealtimeNanos()` no log local |
| RNF-02 | Nenhum quadro do app protegido visível | 0 quadros | Gravação de tela a 60 fps + análise quadro a quadro (ver [10](10-qualidade-e-testes.md)) |
| RNF-03 | Do toque no ícone até o pedido de digital na tela | p95 ≤ 300 ms | Gravação de tela |
| RNF-04 | PIN correto até o app liberado | p95 ≤ 150 ms | Log local |
| RNF-05 | Abrir o Vedra (frio) | ≤ 800 ms | Macrobenchmark |
| RNF-06 | Lista de apps com mais de 200 apps | Rolagem a 60 fps, ícones carregados sob demanda | Macrobenchmark |

**Regras de código que garantem o RNF-01**
- A decisão "bloquear ou não" é uma consulta em memória (`Set<String>` imutável, trocado por referência atômica). Sem banco, disco ou corrotina no caminho do evento.
- A view da cortina é criada uma vez, quando o serviço conecta, e só é adicionada ou removida (ou alterna a visibilidade). Nunca é inflada no evento.
- O caminho crítico roda na main thread do serviço, sem alocação e sem log síncrono em disco (o log vai para uma fila).

## Bateria e memória

| ID | Requisito | Meta |
|---|---|---|
| RNF-07 | Bateria atribuída ao Vedra em 24 h de uso normal | ≤ 1% |
| RNF-08 | Sem wakelock, alarme repetitivo ou polling em segundo plano | 0 |
| RNF-09 | Memória do processo do serviço em repouso | ≤ 40 MB PSS |

O serviço de acessibilidade é orientado a eventos: não consome nada enquanto não há troca de janela.

## Confiabilidade

| ID | Requisito |
|---|---|
| RNF-10 | O serviço volta a funcionar sozinho depois de reiniciar o aparelho (o sistema reconecta serviços de acessibilidade ativos) |
| RNF-11 | Se o processo morrer, o estado de sessão é perdido de propósito: tudo volta a ficar **bloqueado** (falha segura) |
| RNF-12 | Se o Vedra detectar que o serviço foi desligado, mostra uma notificação "Proteção desligada" (ver [05](05-permissoes-e-segundo-plano.md)) |
| RNF-13 | Nenhuma exceção no caminho do evento pode derrubar o serviço: tudo envolto em `runCatching`, e o erro vai para o log local |

## Segurança e privacidade

| ID | Requisito |
|---|---|
| RNF-14 | Sem a permissão `INTERNET` no manifesto final (verificado no CI com `aapt dump permissions`) |
| RNF-15 | PIN só como hash PBKDF2-HMAC-SHA256 com salt aleatório e ≥ 210.000 iterações, ou Argon2id. Comparação em tempo constante |
| RNF-16 | Dados sensíveis cifrados com chave do Android Keystore (com StrongBox quando houver) |
| RNF-17 | `android:allowBackup="false"` e `dataExtractionRules` excluindo tudo: a configuração não migra para outro aparelho |
| RNF-18 | `FLAG_SECURE` em todas as telas do Vedra: nada de captura de tela nem miniatura nos recentes |
| RNF-19 | O serviço de acessibilidade lê **só** o `packageName` e o `className` do evento. Não lê texto, nós da árvore nem conteúdo (`canRetrieveWindowContent="false"`) |
| RNF-20 | Release com R8 (minify + ofuscação) e sem logs de depuração |

## Compatibilidade

| ID | Requisito |
|---|---|
| RNF-21 | `minSdk 26` (Android 8.0), `targetSdk 36` (Android 16), `compileSdk 36` |
| RNF-22 | Prioridade de testes: Samsung One UI 7 e 8 (Android 15 e 16), depois Pixel e Motorola, depois Xiaomi (HyperOS) |
| RNF-23 | Funciona com tela dividida, janela pop-up, Modo DeX (só a tela do celular na v1), modo de um toque e gestos ou botões de navegação |
| RNF-24 | Funciona com fonte grande (até 200%), modo escuro e TalkBack na tela de bloqueio |
| RNF-25 | Compatível com o Android 17 (ver risco do Advanced Protection Mode em [06](06-seguranca-e-ameacas.md)) |

## Manutenção

| ID | Requisito |
|---|---|
| RNF-26 | Kotlin com o estilo oficial, ktlint e detekt no CI |
| RNF-27 | Cobertura de testes unitários ≥ 80% no módulo `core` (regra de sessão, tentativas, cripto) |
| RNF-28 | Toda versão gerada pelo CI, com `versionCode` igual ao número de commits (mesmo padrão do Finora) |
| RNF-29 | Tamanho do AAB ≤ 8 MB |
