# Casos de uso — Backend e Frontend

## Finalidade e fonte de referência

Versão 1.0 — 24/10/2026. Especificação para revisão e desenvolvimento de backend e frontend. Os títulos UC01–UC35 podem ser usados diretamente como nomes das tasks do Jira. Este documento não cria tarefas no Jira.

Fonte: , na pasta Documentações do workspace Ergane, consultada integralmente em 24/10/2026. Foram encontrados 62 requisitos, com identificadores entre R01 e R70. R04, R18, R38, R43, R44, R54, R56 e R60 não constam na versão atual; não foram recriados nem renumerados. As histórias HU00–HU03 ajudam a organizar o produto, mas os requisitos atuais são a referência de escopo.

## Requisitos

A Ergane auxilia pequenos negócios a registrar vendas, organizar encomendas e compreender os valores informados. Pagamentos e devoluções acontecem fora do sistema. Registrar uma venda não confirma recebimento. Não há integração com adquirentes, envio de valores a maquininhas, Pix automático, emissão fiscal ou inferência de produto pelo preço neste escopo.

**Como interpretar:** os fluxos detalham os requisitos; contratos e subtarefas são propostas de implementação. Toda decisão que a fonte deixou aberta está identificada como D01–D12. Não interpretar essas propostas como decisão já aprovada. Um caso condicionado pode ter partes implementáveis, mas sua regra pendente precisa ser validada antes do aceite final.

## Atores, pré-condições e convenções comuns

**Vendedor:** pessoa autenticada com autorização para operar o negócio. **Responsável pelo negócio:** pessoa autorizada a configurar as opções; a distinção de permissões ainda depende de D01. **Sincronizador:** componente que envia operações locais ao servidor. O provedor de biometria do dispositivo é um ator técnico do UC01.

Em todos os casos, exceto o desbloqueio inicial, o usuário deve possuir contexto de negócio autorizado. Objetos relacionados devem pertencer ao mesmo negócio. Consultas, alterações, relatórios e exportações respeitam esse limite no backend e no armazenamento local. A interface não substitui a verificação de autorização.

Cada caso informa: objetivo e gatilho; requisitos; entradas e resultado; fluxo principal; alternativas e falhas; pós-condições; critérios de aceite; contrato funcional proposto; subtarefas de backend, frontend e testes; dependências. As regras comuns abaixo integram todos os critérios de aceite aplicáveis.

## Regras transversais de implementação e aceite

**RT01 — Autorização e isolamento (R03).** A identidade vem da autenticação validada, nunca de um campo de autor aceito cegamente do cliente. Consultar um identificador de outro negócio não revela dados nem permite alteração. Limpar ou separar caches e filtros ao mudar de contexto.

**RT02 — Histórico e autoria (R09, R63–R66).** Conservar identificadores estáveis, negócio, autor, instante do cadastro e ocorrência informada. Alterações relevantes registram valores anteriores/posteriores, autor e instante, sem apagar o registro original. Itens da venda guardam o nome, diferenciador, quantidade e preço praticados naquele momento. Data conhecida sem horário não vira meia-noite fictícia.

**RT03 — Idempotência e concorrência (R23, R25, R59, R67).** Operações repetidas com a mesma identificação e conteúdo produzem um único efeito e devolvem o mesmo resultado. Mesma chave com conteúdo diferente gera conflito explícito. Uma mudança de conteúdo após rejeição exige nova operação; um timeout sem confirmação exige consultar/repetir a operação original. Atualizações concorrentes não sobrescrevem silenciosamente versões mais recentes. Persistir registro, vínculos, auditoria e efeitos associados de maneira consistente.

**RT04 — Valores e cálculos.** Proposta técnica: valores monetários em representação decimal exata; contrato define moeda, escala e arredondamento (D02). Não confiar no total enviado pelo frontend: backend valida e calcula o total dos itens. Produto sem custo é diferente de custo zero. Saldo desconhecido é diferente de saldo zero. Quantidades fracionárias e produtos gratuitos dependem de D02.

**RT05 — Vendas, recebimentos e pendências (R32–R35, R45, R49).** Recebimento informado é declaração, não confirmação bancária. Não somar venda e recebimento como se fossem receitas adicionais. Encomenda convertida e venda vinculada representam a mesma obrigação financeira. Cancelar não executa devolução. Tratamento de excesso recebido, cancelamento e devoluções depende de D05.

**RT06 — Cobertura analítica (R40–R41, R45–R50).** Totais de vendas usam a ocorrência da venda; recebimentos usam sua ocorrência; pendências são apresentadas na data de consulta. Período e fuso devem aparecer. Contagem de itens/transações usa somente registros detalhados elegíveis. Registro agregado não gera produto, quantidade, cliente ou horário inferido. Métrica indisponível deve explicar o motivo; ausência de informação não é resultado zero.

**RT07 — Interface (R42, R68–R70).** Telas funcionam em celular, tablet e computador, com campos rotulados, ações visíveis e mensagens simples. Estados de carregamento, vazio, erro, salvamento e sucesso são distintos. Preservar preenchimento recuperável após falha. Filtros persistem na navegação da mesma sessão, dentro do negócio, até limpeza explícita. Não exigir campos opcionais no fluxo de venda.

**RT08 — Offline (R58–R59, R61–R62).** Garantia explícita da fonte: registro de vendas e consulta ao histórico disponível localmente. Não estender automaticamente edição de catálogo, encomendas, exportação ou configuração ao modo offline. Salvamento local precisa concluir antes da mensagem de sucesso local. Confirmação do servidor é necessária para marcar sincronizado. Sem conexão, informar cobertura e última sincronização; não simular dados completos.

**RT09 — Contratos compartilhados (proposta técnica).** Comandos e consultas abaixo são nomes funcionais, não rotas HTTP fechadas. Detalhar em contrato de API: tipos, campos obrigatórios/opcionais, precisão monetária, paginação/ordenação, autorização, versão, identificador de operação, erros estáveis e exemplos. Resposta de escrita informa identificador, versão e resultado; erros informam código, mensagem e campos envolvidos sem revelar outros negócios. Frontend e backend devem usar os mesmos exemplos de cálculo e estados.

**Aceite transversal:** testar tentativa de acesso a outro negócio; falha de persistência sem efeito parcial; repetição após timeout; edição concorrente; valores ausentes versus zero; datas sem horário; responsividade e preservação de filtros quando aplicáveis. Logs técnicos não substituem a trilha de negócio.

## Decisões a validar antes dos casos afetados

**D01 — Acesso e permissões.** A fonte exige alternativa à biometria e isolamento, mas não define cadastro, login, recuperação, papéis, criação/seleção de negócio ou duração de sessão offline. Definir esses contratos como pré-requisito; não inventar módulo de administração completo. Afeta UC01 e autorização dos demais.

**D02 — Valores, quantidades e datas.** Definir moeda, escala, arredondamento, aceitação de preço zero, unidade/fracionamento, fuso, início de semana e regras para datas futuras. Proposta inicial para validação: BRL com duas casas e fuso configurado por negócio; não impor esses parâmetros sem decisão. Afeta catálogo, vendas, encomendas e métricas.

**D03 — Estoque e disponibilidade.** Definir significado de disponibilidade sem quantidade, responsável pela política aviso/bloqueio, habilitação/desabilitação do acompanhamento, baixas, reposições, ajustes, cancelamentos e conflitos offline. R15–R17 não especificam integralmente essas movimentações e R60 não consta na fonte. Não assumir baixa automática como requisito já aprovado. Sem essa decisão, não há saldo atualizado confiável para reposição. Afeta UC04, UC08–UC09, UC13, UC28, UC32–UC35.

**D04 — Ciclo da encomenda.** Proposta para validação: aberta → em preparação → pronta → entregue; cancelamento antes da entrega; entrega gera/vincula uma única venda. A fonte não fecha estados, retornos, evento gerador de venda nem cálculo da venda quando só existe descrição/total. Afeta UC14–UC19.

**D05 — Recebimentos, devoluções e pendências.** Definir excesso recebido, ajuste do total, cancelamento com dinheiro já recebido, devolução parcial e eventual reabertura de saldo. Proposta: separar valor a receber de valor a devolver e vincular devolução aos recebimentos de origem; não criar dívida automaticamente em uma venda cancelada. R40 exige excluir devoluções do ranking, mas R35 não exige itens devolvidos: definir vínculo por item/quantidade ou regra de exclusão e cobertura. Não reduzir quantidade por aproximação monetária. Afeta UC12–UC13, UC17–UC26 e UC28–UC30.

**D06 — Complementação.** Definir as formas aprovadas em R37. Proposta: venda detalhada retroativa e resumo agregado do valor adicional ainda não lançado, nunca substituição implícita do total do dia. Evitar sobreposição e definir como detalhar posteriormente um resumo. Afeta UC23–UC25 e UC30.

**D07 — Correções.** Definir campos editáveis e tratamento de venda vinculada a encomenda ou recebimentos. Proposta: manter identificação lógica e trilha, com versões e efeito líquido único; não apagar/recriar como nova venda independente. Afeta UC12, UC16–UC17, UC20–UC21, UC24.

**D08 — Indicadores.** Definir ranking por quantidade ou valor, faixas de horário, desempates, cobertura mínima e tratamento de devoluções conforme D05. A fonte não fornece essas fórmulas. Afeta UC25–UC26 e UC30.

**D09 — Recomendações.** Definir janela histórica, mínimo de registros, método e parâmetros da reposição; definir suporte mínimo de combinações e validade da recomendação. Não inventar limiares como se fossem requisitos. Afeta UC28 e UC30.

**D10 — Alertas e metas.** Definir canais, tipos opcionais, repetição, periodicidade de avaliação, efeito de correções na meta e renovação do aviso. Proposta: deduplicar avisos do mesmo evento e manter mensagens operacionais essenciais visíveis. Afeta UC29, UC31–UC32.

**D11 — Política offline.** Definir retenção local, proteção dos dados, sessão autorizada offline, sequência de envio, resolução de conflitos e comportamento de estoque bloqueante sem visão global. Não prometer sincronização em cinco minutos: esse prazo não consta na fonte atual. Afeta UC01, UC11, UC33–UC35.

**D12 — Vínculo comercial opcional.** Decidir se o recurso será adotado e de onde vêm iniciativas disponíveis. Ausência ou expiração do vínculo não impede a gestão individual. Afeta UC02.

## Catálogo de casos de uso

Os casos estão agrupados por tema. Os IDs UC são independentes dos IDs R e devem permanecer estáveis após a criação das tasks. Casos com decisões pendentes devem levar essa dependência para o Jira. Regras transversais podem virar subtarefas técnicas compartilhadas vinculadas aos casos consumidores, sem duplicar a implementação.

---

## Acesso e organização

### UC01 — Acessar o negócio e desbloquear com biometria

- **Objetivo e gatilho:** Acessar dados do negócio autorizado; iniciado ao abrir uma área protegida.
- **Ator:** Vendedor; autenticação e biometria do dispositivo.
- **Requisitos:** R01, R03. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Conta e autorização existentes; autenticação alternativa definida em D01. Biometria exige dispositivo compatível e habilitação voluntária.
- **Entradas e resultado:** Entrada: credencial do mecanismo escolhido ou resposta da biometria nativa. Saída: sessão/contexto autorizado, sem dados biométricos brutos.

**Fluxo principal**

1. Usuário abre o aplicativo e escolhe a forma de acesso disponível.
2. Sistema solicita autenticação alternativa ou desafio biométrico nativo, quando previamente habilitado.
3. Sistema valida a sessão e a autorização para o negócio.
4. Interface apresenta somente os dados daquele negócio.

**Alternativas e falhas:** Biometria ausente, negada ou cancelada: oferecer alternativa sem desbloquear automaticamente. Sessão expirada: solicitar nova autenticação. Offline: somente acesso local previamente autorizado conforme D11; nunca presumir nova autorização.

**Pós-condições:** Sucesso: contexto autorizado ativo. Falha: dados protegidos permanecem inacessíveis.

**Critérios de aceite**

- CA1: Dado usuário sem biometria disponível, quando acessa, então encontra alternativa funcional.
- CA2: Dada biometria rejeitada, então nenhuma área protegida é liberada.
- CA3: Dado identificador de outro negócio, quando consultado diretamente, então o backend nega acesso e não retorna dados.

**Contrato funcional proposto:** ValidarSessao/ObterContextoAutorizado; integração local com biometria. O backend não recebe impressão digital ou imagem facial.

**Subtarefas sugeridas**

- Backend: Implementar validação de sessão e autorização por negócio conforme D01.
- Frontend: Integrar biometria nativa, alternativa e estados de acesso negado/expirado.
- Testes: Cobrir biometria indisponível, sessão expirada, isolamento e retorno offline.

**Dependências e decisões:** D01 e D11; pré-requisito para todos os casos protegidos.

### UC02 — Gerenciar vínculo opcional com iniciativa comercial

- **Objetivo e gatilho:** Associar o negócio a uma iniciativa se o recurso for adotado; iniciado nas configurações.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R02, R03. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Contexto autorizado e decisão D12 aprovada.
- **Entradas e resultado:** Entrada: iniciativa identificada ou opção sem vínculo. Saída: vínculo atual, sem alterar autorização para vendas.

**Fluxo principal**

1. Usuário consulta o vínculo atual.
2. Seleciona uma iniciativa disponível ou remove o vínculo.
3. Sistema valida a referência e salva a associação.
4. Interface confirma a configuração e mantém a gestão individual disponível.

**Alternativas e falhas:** Iniciativa inexistente: não salvar referência inválida. Consulta externa indisponível: informar falha sem bloquear vendas. Ausência de vínculo: configuração válida.

**Pós-condições:** Associação atualizada ou configuração anterior preservada após falha.

**Critérios de aceite**

- CA1: Dado negócio sem vínculo, quando registra venda, então a ausência não bloqueia a operação.
- CA2: Dada remoção do vínculo, então vendas e histórico continuam acessíveis.
- CA3: Dada iniciativa inválida, então a associação anterior não é sobrescrita.

**Contrato funcional proposto:** ConsultarVinculo/AlterarVinculo(negocio, iniciativaOpcional, versao).

**Subtarefas sugeridas**

- Backend: Persistir associação opcional com validação da origem definida.
- Frontend: Exibir vínculo, ausência de vínculo e edição opcional.
- Testes: Testar venda antes/depois da remoção e referência inválida.

**Dependências e decisões:** UC01; D12. Caso condicional, não requisito de cadastro obrigatório.

---

## Catálogo

### UC03 — Cadastrar produto

- **Objetivo e gatilho:** Adicionar um produto vendável com preenchimento mínimo; iniciado em Novo produto.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R05, R06, R12. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Usuário autorizado; critérios de preço/quantidade definidos em D02.
- **Entradas e resultado:** Obrigatórios: nome e preço. Opcionais: descrição, custo, categoria, validade, diferenciador e acompanhamento de quantidade conforme UC08. Saída: produto com identificador próprio.

**Fluxo principal**

1. Usuário informa nome e preço.
2. Opcionalmente informa demais atributos.
3. Sistema valida dados e diferenciação de produtos com nomes iguais.
4. Sistema salva o produto no negócio e confirma sua inclusão no catálogo.

**Alternativas e falhas:** Nome vazio ou preço inválido: indicar o campo e preservar preenchimento. Mesmo nome: aceitar com diferenciador visível; sem diferenciador, solicitar distinção. Categoria inexistente/de outro negócio: rejeitar associação. Falha de salvamento: permitir tentativa segura.

**Pós-condições:** Um produto criado; cadastro inválido não produz produto parcial.

**Critérios de aceite**

- CA1: Dados somente nome e preço válidos, quando salva, então cadastra sem exigir custo, categoria ou estoque.
- CA2: Dados dois produtos chamados Camiseta, com tamanhos P e G, então ambos recebem IDs distintos e diferenciação visível.
- CA3: Dado custo ausente, então permanece ausente, não zero.

**Contrato funcional proposto:** CadastrarProduto(dados, identificadorOperacao) → produtoId, versão, atributos.

**Subtarefas sugeridas**

- Backend: Modelo de produto, validações e vínculo opcional à categoria.
- Frontend: Formulário mínimo com campos opcionais progressivos e diferenciador visível.
- Testes: Testar cadastro mínimo, nomes iguais, categoria indevida e repetição.

**Dependências e decisões:** UC01; UC07 para categoria; UC08 para quantidade; D02.

### UC04 — Consultar e localizar produtos

- **Objetivo e gatilho:** Localizar e inspecionar produtos para gestão ou seleção na venda.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R06, R07, R08, R13, R14, R17, R42. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Contexto autorizado; disponibilidade interpretada conforme D03.
- **Entradas e resultado:** Entrada: termo de nome, categoria/sem categoria, disponibilidade e paginação proposta. Saída: lista e detalhe com IDs, diferenciadores e campos não informados.

**Fluxo principal**

1. Usuário abre o catálogo.
2. Sistema apresenta os produtos elegíveis.
3. Usuário combina busca por nome e filtros.
4. Sistema retorna resultados e mantém os filtros na sessão.
5. Usuário abre um produto e visualiza seus dados.

**Alternativas e falhas:** Nenhum resultado: mostrar estado vazio e ação para limpar filtros. Saldo desconhecido: identificar sem classificar como zero. Falha de consulta: manter filtros e permitir repetir. Produtos arquivados não aparecem como selecionáveis em novas vendas.

**Pós-condições:** Consulta não altera produtos; filtros ficam preservados.

**Critérios de aceite**

- CA1: Dado produto sem estoque acompanhado, quando busca seu nome, então ele é encontrado.
- CA2: Dado filtro Sem categoria, então aparecem produtos sem associação.
- CA3: Dada navegação ao detalhe e retorno, então os filtros anteriores permanecem.
- CA4: Dado campo opcional ausente, então o detalhe indica Não informado.

**Contrato funcional proposto:** ListarProdutos(filtros, paginação)/ConsultarProduto(id) → dados e disponibilidade qualificada.

**Subtarefas sugeridas**

- Backend: Consultas filtradas e isoladas, detalhe e ordenação estável.
- Frontend: Lista, busca, filtros combináveis, detalhe e estados vazio/erro.
- Testes: Testar combinação de filtros, nomes iguais, ausência de saldo e retorno de navegação.

**Dependências e decisões:** UC03; D03 para disponibilidade; RT07.

### UC05 — Editar produto preservando vendas anteriores

- **Objetivo e gatilho:** Atualizar o cadastro sem reescrever o passado; iniciado no detalhe.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R05, R06, R09, R12. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Produto do negócio e versão atual disponíveis.
- **Entradas e resultado:** Entrada: produtoId, versão e campos alterados. Saída: cadastro atualizado; itens históricos intactos.

**Fluxo principal**

1. Usuário abre a edição do produto.
2. Altera os campos desejados.
3. Sistema valida dados e eventual diferenciador.
4. Sistema salva a nova versão e confirma.

**Alternativas e falhas:** Outro usuário alterou a mesma versão: informar conflito e solicitar atualização. Nome/preço inválidos: preservar edição. Alterar categoria exige referência válida. Alterar quantidade usa UC08 e a política D03.

**Pós-condições:** Novas seleções usam o cadastro atualizado; vendas anteriores conservam seu retrato histórico.

**Critérios de aceite**

- CA1: Dada venda antiga a R$ 4,50, quando preço do produto muda para R$ 5,00, então a venda continua a R$ 4,50.
- CA2: Dada mudança de nome, então descrição histórica da venda permanece.
- CA3: Dada versão desatualizada, então não ocorre sobrescrita silenciosa.

**Contrato funcional proposto:** EditarProduto(id, versão, alterações) → nova versão.

**Subtarefas sugeridas**

- Backend: Atualização versionada e separação entre cadastro e item histórico.
- Frontend: Formulário preenchido, validações e resolução de atualização concorrente.
- Testes: Comparar venda anterior/depois da edição e testar concorrência.

**Dependências e decisões:** UC03; D02 e D03 quando aplicável.

### UC06 — Arquivar produto

- **Objetivo e gatilho:** Retirar produto de novas seleções conservando seus vínculos.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R10. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Produto pertencente ao negócio.
- **Entradas e resultado:** Entrada: produtoId e versão. Saída: produto arquivado.

**Fluxo principal**

1. Usuário escolhe arquivar no detalhe.
2. Interface informa que o histórico será preservado.
3. Sistema marca o produto como arquivado.
4. Novas seleções deixam de oferecê-lo.

**Alternativas e falhas:** Produto já arquivado: retornar estado atual sem novo efeito. Produto em carrinho ainda não salvo: revalidar antes da conclusão e informar indisponibilidade. Encomenda já existente conserva os dados e o vínculo; sua conclusão segue D04.

**Pós-condições:** Arquivo lógico, sem apagar vendas ou encomendas.

**Critérios de aceite**

- CA1: Dado produto com vendas, quando arquivado, então o histórico ainda mostra seus itens.
- CA2: Dada nova seleção, então o arquivado não está disponível.
- CA3: Dada repetição da ação, então não surgem efeitos adicionais.

**Contrato funcional proposto:** ArquivarProduto(id, versão, operação) → situação.

**Subtarefas sugeridas**

- Backend: Arquivamento lógico e validação de elegibilidade em novas operações.
- Frontend: Ação de arquivamento, informação de impacto e atualização da lista.
- Testes: Testar vínculos antigos, carrinho aberto e repetição.

**Dependências e decisões:** UC03–UC05; D04 para encomenda preexistente.

### UC07 — Gerenciar categorias de produtos

- **Objetivo e gatilho:** Organizar o catálogo por categorias opcionais.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R11, R12, R14. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Contexto autorizado.
- **Entradas e resultado:** Entrada: nome da categoria, identificador para edição e associação opcional no produto. Saída: categorias e associações atualizadas.

**Fluxo principal**

1. Usuário consulta categorias.
2. Cria ou renomeia uma categoria.
3. Sistema valida o nome e salva no negócio.
4. Usuário associa ou remove a categoria de um produto por UC03/UC05.

**Alternativas e falhas:** Nome vazio: rejeitar. Referência de outro negócio: rejeitar. Proposta para exclusão de categoria em uso: solicitar remoção/substituição das associações, sem excluir produtos; validar política antes de oferecer essa ação.

**Pós-condições:** Organização atualizada; produtos sem categoria continuam utilizáveis.

**Critérios de aceite**

- CA1: Dado produto sem categoria, então cadastro e venda são permitidos.
- CA2: Dada categoria renomeada, então filtros exibem o novo nome mantendo vínculos.
- CA3: Dada tentativa de excluir categoria em uso sem política definida, então não há perda de produtos ou associações silenciosa.

**Contrato funcional proposto:** ListarCategorias/CriarCategoria/EditarCategoria; associação por comando de produto. Exclusão depende da política explicitada.

**Subtarefas sugeridas**

- Backend: Persistência de categorias e integridade das referências.
- Frontend: Lista, criação/edição e seletor opcional com Sem categoria.
- Testes: Testar associação, remoção da associação e isolamento.

**Dependências e decisões:** UC01; UC03/UC05; validar semântica de exclusão de categoria.

---

## Disponibilidade e estoque

### UC08 — Configurar acompanhamento de quantidade e política de saldo

- **Objetivo e gatilho:** Permitir controle simplificado quando desejado, sem exigir estoque de todo produto.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R15, R16, R17, R57. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Produto existente; D03 validada e permissão de configuração definida.
- **Entradas e resultado:** Entrada: opção de acompanhamento, saldo inicial opcional, data de referência, política aviso/bloqueio e limite mínimo opcional. Saída: configuração com saldo conhecido ou desconhecido.

**Fluxo principal**

1. Usuário abre as opções de quantidade do produto.
2. Escolhe acompanhar quantidade e informa saldo/data quando conhecidos.
3. Escolhe explicitamente aviso ou bloqueio para insuficiência e, opcionalmente, limite mínimo.
4. Sistema valida e grava a configuração.
5. Interface mostra o modo de acompanhamento e a qualidade do saldo.

**Alternativas e falhas:** Saldo não informado: guardar desconhecido, não zero. Data ausente para saldo informado: pedir referência. Mudança de modo com movimentos existentes: aplicar política D03, sem apagar histórico. Falha: conservar configuração anterior.

**Pós-condições:** Configuração aplicada; nenhuma movimentação automática é presumida além de D03.

**Critérios de aceite**

- CA1: Dado saldo inicial zero, então é exibido zero; dado saldo ausente, então é exibido Desconhecido.
- CA2: Dada política bloqueante e insuficiência conhecida, então UC09 bloqueia; na política de aviso, permite continuar após informação clara.
- CA3: Dado produto sem quantidade ou limite, então não recebe alerta de saldo mínimo.

**Contrato funcional proposto:** ConfigurarQuantidade(produto, modo, saldoOpcional, referência, política, limiteOpcional, versão).

**Subtarefas sugeridas**

- Backend: Estados de saldo, configuração e serviço de verificação compartilhado.
- Frontend: Configuração opcional com explicação de aviso/bloqueio e saldo desconhecido.
- Testes: Testar zero/desconhecido, políticas, troca de modo e limites.

**Dependências e decisões:** UC03; D03. Movimentação/ajuste de estoque exige decisão de escopo antes de implementação.

---

## Vendas

### UC09 — Registrar venda detalhada

- **Objetivo e gatilho:** Registrar o atendimento em fluxo contínuo, sem processar o pagamento.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R16, R22, R23, R24, R25, R63, R64, R66, R67, R69. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Negócio autorizado; produtos selecionáveis; D02/D03 definidas. Sem conexão, usar UC33.
- **Entradas e resultado:** Entrada: itens com produtoId e quantidade, ocorrência, forma de pagamento opcional e identificação estável da operação. Saída: vendaId, itens históricos, total e confirmação de persistência.

**Fluxo principal**

1. Vendedor inicia Nova venda e localiza produtos pelo UC04.
2. Seleciona itens, ajusta quantidades e pode remover itens ainda não concluídos.
3. Sistema calcula subtotais e total e verifica a política de saldo aplicável.
4. Interface apresenta resumo de itens, quantidades e preços.
5. Vendedor aciona explicitamente Concluir registro.
6. Backend revalida os dados, persiste uma única venda e retorna identificador e total.
7. Interface confirma o registro e oferece informar recebimento separadamente pelo UC20.

**Alternativas e falhas:** Saldo insuficiente: avisar ou bloquear conforme UC08. Produto/preço alterado durante atendimento: apresentar alteração para nova confirmação, sem mudar total silenciosamente. Duplo acionamento ou timeout: recuperar o resultado da mesma operação. Falha de persistência: manter dados e não mostrar sucesso. Carrinho vazio/quantidade inválida: impedir conclusão.

**Pós-condições:** Venda válida registrada uma única vez; nenhum recebimento é presumido. Falha não deixa venda parcial.

**Critérios de aceite**

- CA1: Dados 2 itens de R$ 4,50, então resumo e venda totalizam R$ 9,00.
- CA2: Dado duplo toque ou repetição após timeout, então existe somente uma venda com o mesmo ID.
- CA3: Dado erro ao salvar, então carrinho é preservado e não há confirmação falsa.
- CA4: Dada venda concluída sem recebimento informado, então não aparece como recebimento bancário confirmado.

**Contrato funcional proposto:** RegistrarVenda(itens, ocorrência, formaOpcional, operação) → vendaId, total, versão, situação; validar total no servidor.

**Subtarefas sugeridas**

- Backend: Comando transacional, snapshot de itens, idempotência e validação de elegibilidade/saldo.
- Frontend: Carrinho contínuo, resumo, ação explícita, sucesso e recuperação de falha.
- Testes: Testar cálculo, preço concorrente, insuficiência, timeout e separação de recebimento.

**Dependências e decisões:** UC03–UC04, UC08; D02/D03; UC33 para offline.

### UC10 — Calcular troco durante o atendimento

- **Objetivo e gatilho:** Auxiliar o vendedor a calcular troco sem registrar recebimento automaticamente.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R26. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Valor devido conhecido no atendimento.
- **Entradas e resultado:** Entrada: valor devido e valor entregue em dinheiro. Saída: troco ou insuficiência.

**Fluxo principal**

1. Vendedor abre o cálculo de troco.
2. Informa o valor entregue.
3. Sistema valida o valor e subtrai o devido.
4. Interface exibe troco, valor exato ou quanto falta.

**Alternativas e falhas:** Valor entregue inferior: indicar insuficiência, não mostrar troco negativo como pagamento válido. Valor inválido: pedir correção. Mudança no total: recalcular.

**Pós-condições:** Somente cálculo auxiliar; não altera venda nem recebimentos.

**Critérios de aceite**

- CA1: Devido R$ 9,00 e entregue R$ 10,00: troco R$ 1,00.
- CA2: Devido R$ 9,00 e entregue R$ 8,00: falta R$ 1,00.
- CA3: Após calcular troco, o histórico de recebimentos permanece inalterado.

**Contrato funcional proposto:** CalcularTroco(devido, entregue) → resultado; pode ser local com função decimal compartilhada, sem endpoint obrigatório.

**Subtarefas sugeridas**

- Backend: Definir regra de precisão e exemplos de contrato; não criar mutação financeira.
- Frontend: Campo monetário e estados troco/exato/insuficiente.
- Testes: Testar centavos, igualdade e alteração do total.

**Dependências e decisões:** UC09 ou total conhecido; D02.

### UC11 — Consultar histórico e detalhes de vendas

- **Objetivo e gatilho:** Encontrar vendas e compreender seus dados históricos.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R20, R21, R42, R61. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Contexto autorizado; online ou histórico local disponível.
- **Entradas e resultado:** Entrada: período, origem, situação, produto e forma informada. Saída: vendas e detalhes com cobertura da consulta.

**Fluxo principal**

1. Usuário abre Histórico de vendas.
2. Seleciona filtros combináveis.
3. Sistema lista identificador, ocorrência, valor, origem e situação.
4. Usuário abre uma venda.
5. Sistema mostra itens históricos quando detalhados e vínculos disponíveis.

**Alternativas e falhas:** Resumo agregado: identificar ausência de itens; não atribuir produto. Forma não informada: exibir ausência, sem assumir dinheiro. Offline: consultar apenas cache e informar cobertura/última sincronização. Sem resultado ou falha: distinguir vazio de erro.

**Pós-condições:** Nenhum dado alterado; filtros preservados na sessão.

**Critérios de aceite**

- CA1: Após editar preço no catálogo, o detalhe da venda conserva o preço antigo.
- CA2: Filtro por produto não inclui resumo agregado sem identificação de itens.
- CA3: Offline com cache parcial informa o período disponível e não chama a lista de histórico completo.

**Contrato funcional proposto:** ListarVendas(filtros, paginação)/ConsultarVenda(id) → registros, vínculos, cobertura e atualização.

**Subtarefas sugeridas**

- Backend: Consultas com snapshot, filtros elegíveis e paginação estável.
- Frontend: Histórico, filtros persistentes, detalhe e indicação de registro agregado/local.
- Testes: Testar filtros combinados, histórico antigo e cache parcial.

**Dependências e decisões:** UC09/UC24; D02/D11; RT06.

### UC12 — Corrigir registro de venda

- **Objetivo e gatilho:** Corrigir informação equivocada sem contabilizar nova venda.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R39, R63, R65, R66, R67. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Venda identificada, versão atual e campos corrigíveis definidos em D07.
- **Entradas e resultado:** Entrada: vendaId, versão, alterações e justificativa proposta. Saída: versão corrigida vinculada ao original, histórico e métricas atualizadas.

**Fluxo principal**

1. Usuário localiza o registro no UC11 e escolhe Corrigir.
2. Sistema mostra valores atuais e campos permitidos.
3. Usuário informa a correção e revisa seu impacto.
4. Sistema valida vínculos com encomenda/recebimentos e a versão.
5. Persiste a correção com antes/depois, autor e data, recalculando uma única contribuição.

**Alternativas e falhas:** Mudança incompatível com recebimentos ou encomenda: aplicar D05/D07 ou bloquear com explicação. Versão concorrente: exigir revisão. Tentativa de usar correção para representar nova venda: orientar UC09/UC24. Falha: preservar original.

**Pós-condições:** Histórico preservado; somente a versão válida contribui para os totais.

**Critérios de aceite**

- CA1: Venda de R$ 100 corrigida para R$ 90 altera o total em -R$ 10, sem somar R$ 90 como venda adicional.
- CA2: Repetir a mesma correção não aplica nova diferença.
- CA3: Consultar a trilha permite identificar valor anterior, novo valor, autor e instante.

**Contrato funcional proposto:** CorrigirVenda(id, versão, alterações, operação) → versão, valores e vínculos.

**Subtarefas sugeridas**

- Backend: Versionamento/auditoria e recálculo consistente de dependentes.
- Frontend: Edição contextual, revisão do impacto e mensagem de conflito.
- Testes: Testar efeito líquido, repetição e correção com recebimentos.

**Dependências e decisões:** UC11; D05/D07; UC25.

### UC13 — Cancelar venda registrada

- **Objetivo e gatilho:** Invalidar uma venda mantendo rastreabilidade e valores já declarados.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R19, R39, R63, R65, R67. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Venda existente; política de vínculos e estoque definida em D03/D05.
- **Entradas e resultado:** Entrada: vendaId, versão, confirmação explícita e motivo proposto. Saída: venda cancelada e impactos identificados.

**Fluxo principal**

1. Usuário seleciona Cancelar na venda.
2. Sistema apresenta total e vínculos afetados.
3. Solicita confirmação explícita.
4. Usuário confirma.
5. Sistema registra cancelamento, autoria/data e recalcula contribuições válidas.
6. Interface informa que eventual devolução externa precisa ser registrada separadamente.

**Alternativas e falhas:** Usuário desiste: nenhuma alteração. Já cancelada: retornar estado atual. Recebimento existente: conservar informação, sem devolver dinheiro automaticamente. Venda originada de encomenda: aplicar D04/D05 sem deixar vínculo incoerente.

**Pós-condições:** Venda deixa de contribuir como válida; histórico e recebimentos permanecem rastreáveis.

**Critérios de aceite**

- CA1: Sem confirmação, venda continua válida.
- CA2: Após cancelar venda de R$ 50, ela não contribui para vendas válidas nem ranking.
- CA3: Se há recebimento de R$ 50, cancelar não cria devolução nem apaga o recebimento.

**Contrato funcional proposto:** CancelarVenda(id, versão, motivo, operação) → situação, impactos e pendências de tratamento.

**Subtarefas sugeridas**

- Backend: Cancelamento idempotente, auditoria e recálculo dos agregados.
- Frontend: Confirmação de impacto, situação cancelada e orientação sobre devolução.
- Testes: Testar desistência, repetição, indicadores e recebimento prévio.

**Dependências e decisões:** UC11; D03–D05.

---

## Encomendas

### UC14 — Cadastrar encomenda

- **Objetivo e gatilho:** Organizar pedido futuro sem exigir cadastro complexo do cliente.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R27, R63, R64, R66. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Contexto autorizado; validações de data/total definidas.
- **Entradas e resultado:** Entrada: descrição ou itens, total combinado, data prevista e referência suficiente para reconhecer pedido; contato opcional. Saída: encomendaId e situação inicial definida em D04.

**Fluxo principal**

1. Vendedor inicia Nova encomenda.
2. Informa descrição ou seleciona itens, referência do pedido, total e entrega prevista.
3. Sistema valida preenchimento e apresenta resumo.
4. Usuário confirma.
5. Sistema salva a encomenda e permite informar sinal pelo UC20, separadamente.

**Alternativas e falhas:** Nem descrição nem itens: pedir identificação do conteúdo. Referência, data ou total inválido: manter rascunho e indicar erro. Descrição sem produto cadastrado: aceitar conforme R27. Falha de salvamento: não mostrar pedido concluído.

**Pós-condições:** Encomenda criada com autoria e identificação; não presumir venda nem recebimento.

**Critérios de aceite**

- CA1: Descrição Bolo de aniversário, referência Maria, total R$ 100 e data válida permitem cadastro sem itens de catálogo.
- CA2: Criar encomenda sem sinal não cria recebimento.
- CA3: Duplo envio da mesma operação gera uma encomenda lógica única.

**Contrato funcional proposto:** CadastrarEncomenda(conteúdo, referência, totalCombinado, entregaPrevista, operação).

**Subtarefas sugeridas**

- Backend: Modelo de encomenda descritiva/detalhada e validações.
- Frontend: Formulário simples, resumo e registro opcional de sinal separado.
- Testes: Testar ambas as formas de conteúdo, campos mínimos e repetição.

**Dependências e decisões:** D02/D04; UC03/UC04 apenas para itens.

### UC15 — Consultar encomendas e entregas pendentes

- **Objetivo e gatilho:** Saber o que preparar e entregar e qual saldo foi informado.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R28, R34, R42. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Contexto autorizado.
- **Entradas e resultado:** Entrada: data prevista, situação e referência proposta de busca. Saída: lista/detalhe com conteúdo, entrega e situação financeira separada.

**Fluxo principal**

1. Usuário abre Encomendas.
2. Filtra por data prevista e situação, incluindo não entregues.
3. Sistema lista referências, datas e estados.
4. Usuário abre um pedido.
5. Sistema exibe conteúdo, histórico de situação e recebimentos vinculados.

**Alternativas e falhas:** Sem encomendas: estado vazio. Data passada sem entrega: exibir a data e situação real, sem concluir automaticamente. Falha: manter filtros. Saldo financeiro ainda não determinável: informar limitação de D05.

**Pós-condições:** Consulta sem alteração; filtros preservados.

**Critérios de aceite**

- CA1: Dadas encomendas entregues e abertas, o filtro Não entregues mostra apenas as elegíveis.
- CA2: Uma encomenda recebida integralmente continua não entregue até a mudança operacional válida.
- CA3: Retornar do detalhe conserva os filtros.

**Contrato funcional proposto:** ListarEncomendas(filtros)/ConsultarEncomenda(id) → dados operacionais e financeiros separados.

**Subtarefas sugeridas**

- Backend: Consulta com filtros e vínculos financeiros sem contagem duplicada.
- Frontend: Lista por entrega, detalhe e distinção recebido/entregue.
- Testes: Testar filtros, datas vencidas e estados operacionais versus financeiros.

**Dependências e decisões:** UC14; D04/D05.

### UC16 — Editar encomenda

- **Objetivo e gatilho:** Corrigir o combinado preservando histórico e recebimentos.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R30, R63, R65. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Encomenda existente em estado editável conforme D04/D07.
- **Entradas e resultado:** Entrada: encomendaId, versão, campos alterados. Saída: nova versão e histórico.

**Fluxo principal**

1. Usuário abre edição da encomenda.
2. Altera referência, conteúdo, entrega ou total permitido.
3. Sistema apresenta diferenças e eventual impacto no saldo.
4. Usuário salva.
5. Sistema preserva recebimentos e registra antes/depois, autor e instante.

**Alternativas e falhas:** Total menor que recebido: tratar excesso conforme D05; não apagar sinal. Já convertida em venda: correção coordenada conforme D07. Versão concorrente: revisão obrigatória. Dados inválidos: preservar rascunho.

**Pós-condições:** Pedido corrigido sem criar encomenda adicional ou movimentação financeira automática.

**Critérios de aceite**

- CA1: Alterar entrega mantém os recebimentos existentes.
- CA2: Alterar total de R$ 100 para R$ 120 com R$ 30 recebidos resulta em saldo R$ 90 no cenário simples sem outros ajustes.
- CA3: Edição concorrente não apaga silenciosamente a alteração anterior.

**Contrato funcional proposto:** EditarEncomenda(id, versão, alterações, operação) → nova versão e efeito no saldo.

**Subtarefas sugeridas**

- Backend: Atualização versionada, auditoria e validação de vínculos.
- Frontend: Formulário de edição com diferenças e aviso de impacto.
- Testes: Testar alteração de total, sinal existente e pedido convertido.

**Dependências e decisões:** UC14/UC15; D04/D05/D07.

### UC17 — Atualizar andamento da encomenda

- **Objetivo e gatilho:** Acompanhar preparação e disponibilidade segundo transições aprovadas.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R29, R63, R65. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Encomenda existente; máquina de estados D04 validada.
- **Entradas e resultado:** Entrada: encomendaId, estado de destino e versão. Saída: estado, autoria e instante da mudança.

**Fluxo principal**

1. Usuário abre a encomenda.
2. Sistema apresenta somente próximas ações válidas.
3. Usuário seleciona a mudança.
4. Backend valida transição e versão.
5. Persiste novo estado e histórico e atualiza a interface.

**Alternativas e falhas:** Transição inválida ou pedido cancelado: recusar sem alteração. Mudança concorrente: recarregar antes de continuar. Evento escolhido para gerar venda: delegar ao UC18 com consistência transacional. Estado financeiro não é alterado por mera preparação.

**Pós-condições:** Estado operacional atualizado; trilha preservada.

**Critérios de aceite**

- CA1: Transição permitida registra estado anterior/novo, autor e instante.
- CA2: Transição não permitida não altera o pedido mesmo por chamada direta à API.
- CA3: Marcar Pronta, se esse estado for aprovado, não registra pagamento.

**Contrato funcional proposto:** AlterarSituacaoEncomenda(id, destino, versão, operação) → situação e ações disponíveis.

**Subtarefas sugeridas**

- Backend: Máquina de estados e validação centralizada.
- Frontend: Ações permitidas, andamento e conflito de versão.
- Testes: Cobrir cada transição aprovada e cada transição proibida.

**Dependências e decisões:** UC14/UC15; D04; UC18 no evento de conclusão.

### UC18 — Concluir encomenda e vincular venda

- **Objetivo e gatilho:** Representar a conclusão comercial sem duplicar valor de venda ou recebimentos.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R29, R31, R34, R64, R67. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Encomenda apta ao evento de conclusão aprovado em D04.
- **Entradas e resultado:** Entrada: encomendaId, versão e ocorrência do evento. Saída: vendaId vinculado, situação e obrigação financeira única.

**Fluxo principal**

1. Usuário aciona a conclusão no evento escolhido pela equipe.
2. Sistema apresenta resumo do pedido e recebimentos já informados.
3. Usuário confirma.
4. Backend cria ou recupera a única venda correspondente e vincula a encomenda.
5. Preserva relação dos recebimentos à mesma obrigação, sem copiá-los como novas entradas.
6. Interface confirma o vínculo e a situação final.

**Alternativas e falhas:** Pedido já convertido: devolver a venda existente. Pedido cancelado ou transição proibida: rejeitar. Falha entre criar venda e atualizar encomenda: não deixar conclusão parcial. Pedido só descritivo: representar sem inventar produtos/quantidades, conforme D04.

**Pós-condições:** Uma venda contabilizada; pedido e venda rastreáveis; recebimentos permanecem únicos.

**Critérios de aceite**

- CA1: Encomenda de R$ 100 com sinal R$ 30, ao concluir, gera R$ 100 em vendas e mantém apenas R$ 30 em recebimentos.
- CA2: Repetir a conclusão retorna a mesma vendaId.
- CA3: Falha transacional não deixa duas vendas nem pedido concluído sem vínculo.

**Contrato funcional proposto:** ConcluirEncomenda(id, versão, ocorrência, operação) → vendaId e situação.

**Subtarefas sugeridas**

- Backend: Conversão/vínculo atômico, restrição de unicidade e obrigação financeira compartilhada.
- Frontend: Resumo de conclusão e navegação para a venda vinculada.
- Testes: Testar repetição, falha parcial, sinal e encomenda descritiva.

**Dependências e decisões:** UC14/UC17/UC20; D04/D05.

### UC19 — Cancelar encomenda

- **Objetivo e gatilho:** Cancelar compromisso preservando histórico e valores declarados.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R19, R30, R63, R65. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Encomenda em estado cancelável conforme D04.
- **Entradas e resultado:** Entrada: encomendaId, versão, confirmação e motivo proposto. Saída: encomenda cancelada com recebimentos preservados.

**Fluxo principal**

1. Usuário solicita cancelamento.
2. Sistema mostra pedido, recebimentos e eventual venda vinculada.
3. Solicita confirmação explícita.
4. Usuário confirma.
5. Sistema valida o estado, registra cancelamento e orienta tratar devolução externa pelo UC21 quando aplicável.

**Alternativas e falhas:** Desistência: não altera. Já cancelada: retorno idempotente. Pedido já convertido: executar somente tratamento coordenado aprovado em D04/D05; não cancelar um lado e deixar o outro inconsistente.

**Pós-condições:** Cancelamento rastreável; nenhum estorno financeiro automático.

**Critérios de aceite**

- CA1: Sem confirmação, encomenda permanece ativa.
- CA2: Sinal existente permanece no histórico após cancelar.
- CA3: Cancelamento não cria devolução fictícia nem mantém compromisso de entrega ativo.

**Contrato funcional proposto:** CancelarEncomenda(id, versão, motivo, operação) → situação e vínculos preservados.

**Subtarefas sugeridas**

- Backend: Validação de estado, auditoria e consistência com venda vinculada.
- Frontend: Confirmação com impacto e orientação sobre valores recebidos.
- Testes: Testar desistência, sinal e encomenda convertida.

**Dependências e decisões:** UC15; D04/D05.

---

## Recebimentos e devoluções

### UC20 — Informar recebimento externo e acompanhar saldo

- **Objetivo e gatilho:** Registrar valor recebido fora da Ergane, incluindo sinal ou parcela.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R33, R34, R63, R64, R66, R67. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Venda ou encomenda existente do negócio; política D05 definida.
- **Entradas e resultado:** Entrada: vínculo, valor positivo, ocorrência e forma opcional; operação estável. Saída: recebimentoId, indicação Declarado e saldo atualizado.

**Fluxo principal**

1. Vendedor abre a venda/encomenda e escolhe Informar recebimento.
2. Informa valor e data; informa forma apenas se conhecida.
3. Sistema mostra o vínculo e o saldo antes/depois.
4. Usuário confirma a declaração.
5. Backend grava uma única entrada e recalcula o saldo da obrigação compartilhada.

**Alternativas e falhas:** Zero/negativo: rejeitar. Vínculo de outro negócio: rejeitar. Valor superior ao saldo: tratar conforme D05, sem truncar valor silenciosamente. Timeout: repetir a mesma operação. Encomenda convertida: usar a mesma obrigação e não duplicar sinal.

**Pós-condições:** Recebimento declarado persistido; sistema não executa cobrança nem verifica conta bancária.

**Critérios de aceite**

- CA1: Pedido de R$ 100 recebe R$ 30 e R$ 20: dois recebimentos e R$ 50 restantes no cenário sem ajustes.
- CA2: Reenvio da declaração de R$ 30 com a mesma operação mantém somente uma entrada.
- CA3: Forma ausente permanece Não informada.
- CA4: Salvar recebimento não marca encomenda entregue.

**Contrato funcional proposto:** InformarRecebimento(vínculo, valor, ocorrência, formaOpcional, operação) → recebimentoId e saldo.

**Subtarefas sugeridas**

- Backend: Validação monetária, vínculo único e cálculo de pendência.
- Frontend: Formulário simples, confirmação de declaração e saldo atualizado.
- Testes: Testar parcelas, excesso conforme política, timeout e conversão de encomenda.

**Dependências e decisões:** UC09/UC14; D02/D05.

### UC21 — Informar devolução realizada externamente

- **Objetivo e gatilho:** Registrar devolução efetiva fora do sistema com origem e motivo.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R35, R34, R63, R64, R65, R66, R67. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Origem identificada e política de devolução D05 aprovada.
- **Entradas e resultado:** Entrada: origem/vínculo, valor positivo, ocorrência, motivo e operação. Itens/quantidades devolvidos somente se a extensão de D05 for aprovada.

**Fluxo principal**

1. Usuário abre a origem e escolhe Informar devolução.
2. Sistema apresenta recebimentos e devoluções já declarados.
3. Usuário informa valor, data e motivo.
4. Sistema valida disponibilidade devolvível conforme D05 e apresenta impacto.
5. Usuário confirma; sistema persiste a declaração e atualiza saldos aplicáveis.

**Alternativas e falhas:** Valor excede devolvível: recusar ou aplicar regra expressamente aprovada, sem valor negativo oculto. Sem origem identificada: pedir vínculo. Venda cancelada: não recriar dívida automaticamente. Falha: preservar formulário sem registrar devolução.

**Pós-condições:** Devolução registrada e auditável; nenhum dinheiro transferido pela Ergane.

**Critérios de aceite**

- CA1: Recebimento de R$ 100 com devolução anterior R$ 20 admite R$ 30 adicionais no cenário aprovado, conservando ambas as devoluções.
- CA2: Repetição da mesma operação não duplica devolução.
- CA3: Devolução apenas monetária não inventa quantidade devolvida para o ranking.

**Contrato funcional proposto:** InformarDevolucao(origem, valor, ocorrência, motivo, operação) → devolucaoId e impacto.

**Subtarefas sugeridas**

- Backend: Validação de origem/limite, persistência idempotente e efeito financeiro.
- Frontend: Origem visível, resumo de impacto e rótulo de declaração externa.
- Testes: Testar devolução parcial, limite acumulado, cancelamento e repetição.

**Dependências e decisões:** UC20; D05, inclusive semântica de ranking.

### UC22 — Consultar histórico de recebimentos e devoluções

- **Objetivo e gatilho:** Conferir valores declarados e suas origens.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R32, R34, R42, R64. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Contexto autorizado.
- **Entradas e resultado:** Entrada: período, forma quando informada e vínculo proposto. Saída: movimentos com tipo, valor, ocorrência, responsável e origem.

**Fluxo principal**

1. Usuário abre o histórico financeiro.
2. Aplica filtros.
3. Sistema lista recebimentos e devoluções com tipos distintos.
4. Usuário abre um movimento e navega para venda/encomenda de origem.
5. Interface mantém filtros ao retornar.

**Alternativas e falhas:** Forma desconhecida: identificá-la sem completar automaticamente. Sem movimentos: estado vazio, diferente de erro. Origem convertida em venda: mostrar ligação sem repetir movimento.

**Pós-condições:** Consulta sem efeitos financeiros.

**Critérios de aceite**

- CA1: Um sinal vinculado a encomenda convertida aparece uma única vez.
- CA2: Filtro por forma só inclui movimentos com aquela forma informada.
- CA3: Cada movimento mostra valor, data, autor e vínculo navegável autorizado.

**Contrato funcional proposto:** ListarMovimentosInformados(filtros, paginação)/ConsultarMovimento(id).

**Subtarefas sugeridas**

- Backend: Consulta unificada de tipos distintos e vínculos estáveis.
- Frontend: Histórico, filtros, detalhe e rótulos claros.
- Testes: Testar filtros, origem convertida e ausência de informação.

**Dependências e decisões:** UC20/UC21; RT05/RT07.

---

## Conferência do período

### UC23 — Conferir registros de um período

- **Objetivo e gatilho:** Entender o que já foi registrado antes de complementar o movimento.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R36, R45, R46, R50. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Contexto autorizado; período e fuso definidos.
- **Entradas e resultado:** Entrada: intervalo. Saída: vendas, recebimentos do intervalo, pendências na consulta e cobertura.

**Fluxo principal**

1. Usuário escolhe dia, semana, mês ou intervalo disponível.
2. Sistema apresenta registros existentes antes da ação de complemento.
3. Separa vendas de recebimentos e pendências.
4. Usuário inspeciona origem e detalhe dos registros.
5. Se identificar falta, aciona UC24 com o período preservado.

**Alternativas e falhas:** Dados locais parciais ou registros não sincronizados: sinalizar incompletude. Sem registros: mostrar ausência de registros, não afirmar que nada foi vendido. Datas sem horário entram no dia conhecido, não em análise horária.

**Pós-condições:** Período conferido sem criar lançamento automático.

**Critérios de aceite**

- CA1: R$ 160 em vendas e R$ 100 recebidos aparecem separadamente, nunca como R$ 260 vendidos.
- CA2: Antes de complementar, o usuário consegue ver registros existentes.
- CA3: Pendências indicam data de consulta, mesmo que o filtro de vendas seja de outro mês.

**Contrato funcional proposto:** ConferirPeriodo(intervalo) → registros, totais separados e cobertura.

**Subtarefas sugeridas**

- Backend: Consulta consolidada com datas de referência explícitas.
- Frontend: Tela de conferência, acesso a detalhes e ação contextual de complementar.
- Testes: Testar datas distintas de venda/recebimento, cache parcial e ausência de registros.

**Dependências e decisões:** UC11/UC22; D02; UC25.

### UC24 — Complementar vendas não registradas

- **Objetivo e gatilho:** Adicionar somente o movimento ausente com origem e nível de detalhe explícitos.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R37, R39, R63, R65, R66, R67. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Conferência UC23 realizada; formas permitidas aprovadas em D06.
- **Entradas e resultado:** Entrada: período/ocorrência, forma aprovada, valor adicional ou itens detalhados, origem e operação. Saída: complemento identificado como detalhado ou agregado.

**Fluxo principal**

1. Usuário revisa o período existente.
2. Escolhe uma forma aprovada de complementar.
3. Informa apenas vendas ainda não registradas.
4. Sistema mostra o valor adicional e novo total, com aviso de possível sobreposição.
5. Usuário confirma.
6. Sistema registra origem, detalhe e ocorrência, sem inventar produtos ou horários.

**Alternativas e falhas:** Usuário informa total do dia em vez do faltante: esclarecer e exigir confirmação do valor adicional. Mesma operação: retornar resultado anterior. Sobreposição identificável: alertar e solicitar revisão; igualdade de valor/data, sozinha, não prova duplicidade. Correção de complemento usa RT03 e D07; detalhar um resumo depende de D06.

**Pós-condições:** Complemento válido entra uma vez no total; resumos não entram em contagens de itens/transações detalhadas.

**Critérios de aceite**

- CA1: Período com R$ 160 mais complemento de R$ 40 resulta em R$ 200.
- CA2: Resumo de R$ 40 não gera quantidade, produto ou horário presumido.
- CA3: Repetir após timeout conserva o mesmo complemento.
- CA4: Transformar resumo em detalhe, quando aprovado, não soma ambos como vendas independentes.

**Contrato funcional proposto:** ComplementarPeriodo(modo, conteúdo, ocorrência/período, origem, operação) → complementoId e total.

**Subtarefas sugeridas**

- Backend: Modelo de nível de detalhe, origem e proteção de efeito único.
- Frontend: Escolha de forma, conferência do adicional e limitações visíveis.
- Testes: Testar 160+40, repetição, sobreposição e cobertura de indicadores.

**Dependências e decisões:** UC23; D06/D07; UC09 quando detalhado.

---

## Painéis e relatórios

### UC25 — Consultar painel de vendas, recebimentos e pendências

- **Objetivo e gatilho:** Compreender o negócio com indicadores calculados sobre bases explícitas.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R42, R45, R46, R49, R50, R70. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Contexto autorizado; D02/D05 definidas.
- **Entradas e resultado:** Entrada: recorte temporal. Saída: vendas válidas, recebimentos e devoluções do período, pendências atuais e cobertura.

**Fluxo principal**

1. Usuário abre o painel e escolhe o período.
2. Sistema calcula cada métrica por sua data de referência.
3. Exibe os valores em blocos separados e informa intervalo/fuso.
4. Exibe contagens somente para registros detalhados elegíveis.
5. Permite consultar origem, fórmula resumida e limitações dos indicadores.

**Alternativas e falhas:** Dados insuficientes: mostrar indisponibilidade/limitação. Registro cancelado: excluir contribuição de venda válida. Complemento agregado: entra no valor elegível, não em número de itens. Correções/devoluções: aplicar D05 e versões válidas.

**Pós-condições:** Indicadores apresentados sem produzir lançamentos.

**Critérios de aceite**

- CA1: Venda em setembro recebida em outubro aparece em vendas de setembro e recebimentos de outubro.
- CA2: Resumo agregado eleva o valor vendido, mas não inventa contagem de transações.
- CA3: Sem custos e despesas suficientes, nenhum indicador é chamado de lucro líquido.
- CA4: Encomenda convertida não duplica venda nem sinal.

**Contrato funcional proposto:** ConsultarPainel(intervalo) → cada métrica com valor, referência temporal, cobertura e definição.

**Subtarefas sugeridas**

- Backend: Serviço de métricas consistente, cancelamentos/versões e obrigação única.
- Frontend: Painel responsivo, recortes persistentes e explicações acessíveis.
- Testes: Conjunto de dados com venda, sinal, conversão, complemento, cancelamento e datas cruzadas.

**Dependências e decisões:** UC09, UC18, UC20–UC24; D02/D05.

### UC26 — Analisar produtos vendidos, horários e diferença entre preço e custo

- **Objetivo e gatilho:** Consultar análises sustentadas por itens, horários e custos conhecidos.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R40, R41, R42, R46, R47, R50. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Dados elegíveis; critérios analíticos D08 e devoluções D05 definidos.
- **Entradas e resultado:** Entrada: período e visão escolhida. Saída: ranking, distribuição por horário ou diferença estimada, com cobertura.

**Fluxo principal**

1. Usuário escolhe visão e período.
2. Sistema seleciona registros elegíveis e exclui cancelamentos/devoluções conforme regra aprovada.
3. Calcula ranking, faixas horárias ou preço menos custo informado.
4. Interface mostra critério, base utilizada e registros não cobertos.

**Alternativas e falhas:** Horário desconhecido: excluir da distribuição horária e revelar cobertura. Custo ausente: não calcular como zero. Devolução monetária sem itens: não estimar quantidade; aplicar política D05 ou indicar que ranking ajustado não pode ser apurado. Para análise histórica de custo, guardar custo da ocorrência ou explicitar que a fonte não o fornece.

**Pós-condições:** Análises sem inferências não sustentadas.

**Critérios de aceite**

- CA1: Dez registros, quatro sem horário: análise horária declara uso de seis.
- CA2: Preço R$ 10 e custo R$ 6: diferença estimada R$ 4, sem rótulo lucro líquido.
- CA3: Custo não informado: diferença indisponível.
- CA4: Ranking segue a regra aprovada de exclusão de cancelamentos e devoluções e apresenta sua base.

**Contrato funcional proposto:** ConsultarAnaliseProdutos(tipo, intervalo, critérios) → resultado e cobertura; histórico de custo é decisão de modelagem explícita.

**Subtarefas sugeridas**

- Backend: Consultas analíticas por elegibilidade e fórmulas documentadas.
- Frontend: Ranking/gráficos legíveis e indicação de exclusões/dados ausentes.
- Testes: Testar horário ausente, custos nulos/zero, cancelamentos e devoluções parciais.

**Dependências e decisões:** UC11/UC21/UC25; D05/D08.

### UC27 — Exportar histórico ou relatório em PDF e CSV

- **Objetivo e gatilho:** Obter arquivo coerente com a consulta selecionada.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R48, R42, R50. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Consulta autorizada definida e acesso ao serviço de exportação; offline não garantido pela fonte.
- **Entradas e resultado:** Entrada: tipo de relatório, filtros, período e formato. Saída: PDF/CSV com identificação, filtros e limitações.

**Fluxo principal**

1. Usuário abre histórico ou relatório e aplica filtros.
2. Escolhe PDF ou CSV.
3. Sistema usa os mesmos critérios de consulta e registra instante da extração.
4. Gera arquivo com todos os registros filtrados, não apenas a página visível.
5. Interface disponibiliza download e informa resultado.

**Alternativas e falhas:** Sem registros: gerar relatório vazio identificado ou informar ausência de conteúdo conforme contrato, sem arquivo enganoso. Falha: permitir repetir. Dados alterados durante extração: usar visão consistente e informar referência. CSV deve tratar separadores, aspas, quebras e células que possam ser interpretadas como fórmulas.

**Pós-condições:** Arquivo disponível sem alterar dados do negócio.

**Critérios de aceite**

- CA1: Filtro de outubro exporta apenas a base elegível de outubro, incluindo registros além da página atual.
- CA2: PDF e CSV identificam resumos agregados e limitações.
- CA3: Exportação não inclui registros de outro negócio nem presume custos ausentes.

**Contrato funcional proposto:** ExportarRelatorio(tipo, formato, filtros) → arquivo/estado de geração, instante e critérios.

**Subtarefas sugeridas**

- Backend: Geradores sobre consulta comum, isolamento e serialização segura.
- Frontend: Seleção de formato, progresso/erro e download.
- Testes: Comparar totais com painel, paginação, caracteres especiais e isolamento.

**Dependências e decisões:** UC11/UC22/UC25/UC26; D02.

---

## Alertas e planejamento

### UC28 — Consultar sugestões de reposição

- **Objetivo e gatilho:** Apoiar reposição quando saldo e histórico sustentarem uma sugestão.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R51, R50, R57. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Quantidade acompanhada e atualizada; método e suficiência aprovados em D03/D09.
- **Entradas e resultado:** Entrada: produto/período e parâmetros aprovados. Saída: sugestão com saldo, base histórica, período, método e limitações ou indisponibilidade.

**Fluxo principal**

1. Usuário abre as sugestões.
2. Sistema verifica saldo conhecido e histórico suficiente.
3. Calcula sugestão pelo método aprovado.
4. Exibe dados e período utilizados e sua justificativa.
5. Usuário consulta o detalhe; decidir repor não gera compra ou ajuste automático.

**Alternativas e falhas:** Saldo desconhecido/desatualizado ou histórico insuficiente: informar motivo de indisponibilidade. Produto arquivado: não sugerir como se estivesse ativo. Cancelamentos/devoluções/itens sem identificação não sustentam demanda fictícia.

**Pós-condições:** Sugestão explicável, sem alterar estoque nem prometer demanda futura.

**Critérios de aceite**

- CA1: Sem saldo conhecido, nenhuma quantidade de reposição é inventada.
- CA2: Sugestão exibida permite identificar período e dados que a sustentam.
- CA3: Consultar uma sugestão não cria compra nem reposição automaticamente.

**Contrato funcional proposto:** ConsultarSugestoesReposicao(filtros) → disponibilidade, justificativa e base de cálculo.

**Subtarefas sugeridas**

- Backend: Critério de suficiência, método versionado e dados elegíveis.
- Frontend: Sugestão com explicação e estado Dados insuficientes.
- Testes: Testar dados insuficientes, saldo antigo, cancelamentos e resultados do método aprovado.

**Dependências e decisões:** UC08/UC26; D03/D05/D09. Bloqueado para cálculo final até fechar essas decisões.

### UC29 — Definir e acompanhar meta de vendas

- **Objetivo e gatilho:** Definir valor desejado para um período e saber quando for atingido.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R52, R55, R57. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Contexto autorizado; política de avisos D10.
- **Entradas e resultado:** Entrada: valor da meta e período. Saída: meta, progresso sobre vendas válidas e aviso de atingimento.

**Fluxo principal**

1. Usuário informa valor e período da meta.
2. Sistema valida e salva.
3. Calcula progresso com vendas válidas do período.
4. Ao alcançar o valor, registra o evento e apresenta aviso conforme preferências.
5. Usuário consulta valor realizado e base do cálculo.

**Alternativas e falhas:** Período inválido ou valor inadequado: rejeitar. Recebimentos não contam como vendas adicionais. Cancelamento/correção posterior: recalcular progresso e aplicar política de reaviso D10. Repetição da avaliação não multiplica o mesmo aviso.

**Pós-condições:** Meta/progresso consistentes e aviso rastreável.

**Critérios de aceite**

- CA1: Meta R$ 200 com vendas válidas de R$ 200 gera evento de atingimento.
- CA2: R$ 100 em vendas e R$ 100 recebidos sobre elas não atingem meta de R$ 200.
- CA3: Reavaliar o mesmo evento não gera avisos repetidos sem política explícita.

**Contrato funcional proposto:** DefinirMeta(valor, período)/ConsultarMeta; AvaliarAtingimento com identificação do evento.

**Subtarefas sugeridas**

- Backend: Persistência da meta, cálculo compartilhado e deduplicação de aviso.
- Frontend: Configuração, progresso e aviso com período e total.
- Testes: Testar limiar exato, datas, duplicidade e correção posterior.

**Dependências e decisões:** UC25/UC31; D02/D10.

### UC30 — Consultar sugestões de produtos vendidos em conjunto

- **Objetivo e gatilho:** Identificar combinações observadas em transações detalhadas.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R53, R50. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Histórico com itens da mesma venda; critério de suporte D09 aprovado.
- **Entradas e resultado:** Entrada: período e produto opcional. Saída: pares/grupos e suporte observado, com denominador explicitado.

**Fluxo principal**

1. Usuário abre análise de combinações.
2. Sistema seleciona vendas detalhadas elegíveis.
3. Identifica coocorrência de produtos dentro da mesma transação.
4. Calcula suporte pelo critério aprovado.
5. Exibe combinações, número de ocorrências/base e período.

**Alternativas e falhas:** Poucos dados: informar insuficiência. Resumo agregado ou vendas separadas do mesmo dia: não unir artificialmente. Cancelamentos/devoluções: aplicar elegibilidade D05/D08. Não inferir identidade do cliente, pois ela não é necessária.

**Pós-condições:** Sugestão informativa, sem criar kit, promoção ou alterar preços.

**Critérios de aceite**

- CA1: Produtos A e B em vendas diferentes não contam como combinação.
- CA2: Se 3 de 10 transações elegíveis contêm A e B, exibir 3/10 (30%) quando esse denominador for o aprovado.
- CA3: Resumo sem itens não participa do suporte nem gera pares.

**Contrato funcional proposto:** ConsultarCombinacoes(intervalo, produtoOpcional) → combinações, suporte e cobertura.

**Subtarefas sugeridas**

- Backend: Consulta de coocorrência por venda e critério mínimo documentado.
- Frontend: Lista explicativa de pares e suporte observado.
- Testes: Testar transações distintas, itens repetidos na mesma venda e base insuficiente.

**Dependências e decisões:** UC09/UC26; D05/D08/D09.

### UC31 — Configurar preferências de notificações

- **Objetivo e gatilho:** Escolher os avisos opcionais desejados.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R55. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Usuário autenticado; catálogo de avisos/canais definido em D10.
- **Entradas e resultado:** Entrada: tipos opcionais habilitados/desabilitados. Saída: preferências persistidas.

**Fluxo principal**

1. Usuário abre Notificações.
2. Sistema mostra tipos opcionais disponíveis e suas finalidades.
3. Usuário altera preferências.
4. Sistema salva e confirma.
5. Novos avisos opcionais respeitam a escolha.

**Alternativas e falhas:** Falha: manter preferência anterior e informar. Canal indisponível: indicar indisponibilidade. Preferência não suprime mensagem necessária para saber se venda foi salva, bloqueada ou está em conflito.

**Pós-condições:** Preferências aplicadas no escopo definido em D10.

**Critérios de aceite**

- CA1: Aviso opcional desabilitado não é enviado pelo canal correspondente.
- CA2: Reabrir configurações apresenta valores persistidos.
- CA3: Erro de salvamento não é ocultado por desativar notificações opcionais.

**Contrato funcional proposto:** ConsultarPreferencias/SalvarPreferencias(tipos, versão).

**Subtarefas sugeridas**

- Backend: Persistência e aplicação de preferências na entrega de avisos.
- Frontend: Controles com rótulos simples e confirmação de persistência.
- Testes: Testar persistência, entrega opcional e mensagens operacionais.

**Dependências e decisões:** UC01; D10; UC29/UC32.

### UC32 — Consultar alertas e sua justificativa

- **Objetivo e gatilho:** Entender um aviso e os dados que o motivaram.
- **Ator:** Sistema avalia o evento; vendedor consulta o alerta.
- **Requisitos:** R57, R55. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Evento de alerta suportado e regras D03/D10 definidas.
- **Entradas e resultado:** Entrada: contexto e eventos elegíveis. Saída: aviso com motivo, valores de origem, período e ação de consulta.

**Fluxo principal**

1. Sistema avalia evento suportado.
2. Verifica dados necessários e preferências para avisos opcionais.
3. Apresenta alerta com motivo compreensível.
4. Usuário abre a origem para conferir os dados.

**Alternativas e falhas:** Quantidade não acompanhada ou limite não configurado: não gerar alerta de saldo mínimo. Dados antigos/pendentes: revelar limitação. Evento repetido: deduplicar conforme D10. Não adicionar alertas de queda/tendência ausentes da fonte atual.

**Pós-condições:** Aviso explicável sem mudança automática de dados.

**Critérios de aceite**

- CA1: Sem limite mínimo configurado, nenhum alerta de saldo mínimo aparece.
- CA2: Alerta de saldo apresenta saldo considerado, limite e origem.
- CA3: O usuário consegue distinguir um aviso informativo de uma falha no registro.

**Contrato funcional proposto:** ConsultarAlertas(contexto) → tipo, motivo, base e origem; tipos limitados ao catálogo aprovado.

**Subtarefas sugeridas**

- Backend: Avaliação de eventos, elegibilidade e deduplicação.
- Frontend: Avisos legíveis com acesso ao dado de origem.
- Testes: Testar limite ausente, quantidade desconhecida e preferências.

**Dependências e decisões:** UC08/UC29/UC31; D03/D10.

---

## Offline e sincronização

### UC33 — Registrar venda sem conexão

- **Objetivo e gatilho:** Continuar o registro de vendas quando a conexão falhar.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R58, R24, R25, R63, R64, R66, R67, R69. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Sessão local autorizada, catálogo necessário disponível e armazenamento local funcional; D11 definida.
- **Entradas e resultado:** Entrada: venda detalhada e recebimento declarado opcional, com IDs estáveis, autoria e ocorrência. Saída: registro local durável pendente de sincronização.

**Fluxo principal**

1. Interface identifica ausência de conexão e informa limitações.
2. Vendedor monta a venda conforme UC09 com dados locais disponíveis.
3. Confirma o registro.
4. Sistema persiste venda e operação pendente de forma consistente no dispositivo.
5. Mostra identificador, total e mensagem Salvo neste dispositivo — pendente de sincronização.
6. Se recebimento for informado, mantém declaração e vínculo para envio coerente.

**Alternativas e falhas:** Armazenamento cheio/indisponível: não confirmar salvamento; preservar o recuperável. Aplicativo fechado após sucesso local: recuperar registro pendente ao reabrir. Saldo global incerto: seguir D03/D11; não afirmar disponibilidade global. Conta diferente no dispositivo: impedir acesso ao cache de outro negócio.

**Pós-condições:** Venda local recuperável e enfileirada uma única vez, sem afirmar confirmação do servidor.

**Critérios de aceite**

- CA1: Após sucesso local e reinício do aplicativo, venda e pendência permanecem.
- CA2: Falha de gravação local não produz mensagem de venda salva.
- CA3: Dois acionamentos mantêm uma única venda local.
- CA4: Recebimento offline permanece identificado como informado pelo vendedor.

**Contrato funcional proposto:** PersistirVendaLocal(operação, venda, vínculos) → ID local estável e estado pendente; payload compatível com UC34.

**Subtarefas sugeridas**

- Backend: Contrato de ingestão e validações compatíveis com dados offline.
- Frontend: Armazenamento local transacional, fila, retomada e indicação de pendência.
- Testes: Testar queda de rede, reinício, disco indisponível e separação de contas.

**Dependências e decisões:** UC09/UC20; D03/D11; UC34.

### UC34 — Sincronizar registros locais e acompanhar o envio

- **Objetivo e gatilho:** Enviar registros pendentes preservando identidade e evitando duplicidade.
- **Ator:** Sincronizador; vendedor acompanha o resultado.
- **Requisitos:** R59, R62, R63, R64, R66, R67. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Conexão recuperada, sessão autorizada e fila local disponível.
- **Entradas e resultado:** Entrada: operações locais com identificador, vínculo, ocorrência, autoria e versão quando aplicável. Saída: confirmação por operação, falha recuperável ou conflito.

**Fluxo principal**

1. Sincronizador detecta conexão e verifica sessão.
2. Seleciona operações pendentes respeitando dependências entre venda e recebimento.
3. Envia cada operação com a mesma identificação estável.
4. Backend valida autorização, idempotência e vínculos e persiste o efeito.
5. Cliente marca sincronizada somente a operação confirmada.
6. Interface mostra pendentes, falhas, conflitos e conclusão real.

**Alternativas e falhas:** Resposta perdida após gravação: repetir mesma operação e recuperar resultado existente. Sessão expirada: pausar para autenticação sem apagar fila. Falha transitória: manter pendência. Conflito de dados/saldo: separar para UC35. Falha parcial do lote: não marcar itens sem confirmação como sincronizados.

**Pós-condições:** Operações confirmadas existem uma vez no servidor; demais continuam recuperáveis localmente.

**Critérios de aceite**

- CA1: Servidor salva e resposta se perde: reenvio não duplica venda/recebimento.
- CA2: Lote com duas confirmações e uma falha deixa somente a terceira pendente/falha.
- CA3: Ocorrência original e autor permanecem; cadastro/sincronização têm instantes próprios.
- CA4: Envio iniciado não é apresentado como sincronização concluída.

**Contrato funcional proposto:** SincronizarOperacoes(lote) → resultado individual: confirmado, rejeitado recuperável ou conflito; mesma chave/dados retorna mesmo ID.

**Subtarefas sugeridas**

- Backend: Ingestão idempotente, dependências, respostas individuais e proteção de autoria.
- Frontend: Fila persistente, política de tentativa, pausa de autenticação e painel de estado.
- Testes: Simular confirmação perdida, lotes parciais, reordenação e expiração.

**Dependências e decisões:** UC33/UC01; D03/D11; RT03.

### UC35 — Resolver conflito de sincronização

- **Objetivo e gatilho:** Dar tratamento explícito a um registro que não pôde ser sincronizado.
- **Ator:** Vendedor ou responsável autorizado pelo negócio.
- **Requisitos:** R59, R62, R65, R67. Aplicam-se também as regras transversais pertinentes.
- **Pré-condições:** Operação em conflito e estratégia de resolução D11 definida.
- **Entradas e resultado:** Entrada: operação original, dados locais/remotos disponíveis e escolha permitida. Saída: resolução auditada ou conflito mantido.

**Fluxo principal**

1. Usuário abre registro em conflito.
2. Sistema mostra o motivo e as informações necessárias sem expor outro negócio.
3. Oferece somente opções previstas pela política aprovada.
4. Usuário escolhe e confirma a resolução.
5. Sistema registra a decisão, preserva o vínculo à operação original e reprocessa quando cabível.
6. Marca resolvido apenas após confirmação válida.

**Alternativas e falhas:** Sem opção segura ou autorização: manter conflito e orientar responsável, sem descartar venda silenciosamente. Operação remota já aceita: recuperar resultado em vez de criar outra. Conteúdo corrigido após rejeição: criar operação vinculada segundo RT03; não reutilizar chave com payload diferente. Rede cai: resolução permanece pendente.

**Pós-condições:** Conflito resolvido de modo rastreável ou preservado para nova ação; nunca perda silenciosa.

**Critérios de aceite**

- CA1: Conflito não é marcado como sucesso antes da confirmação.
- CA2: Correção não gera duplicidade da venda original nem reutiliza indevidamente uma chave.
- CA3: Dados e motivo originais ficam recuperáveis para auditoria.
- CA4: Reabrir o aplicativo mantém conflitos ainda não resolvidos.

**Contrato funcional proposto:** ConsultarConflito/ResolverConflito(id, versão, decisão, operação) → estado e vínculo de resolução.

**Subtarefas sugeridas**

- Backend: Modelo de conflito, validação de estratégias e auditoria.
- Frontend: Comparação compreensível, opções permitidas e estado pendente.
- Testes: Testar escolha inválida, rede interrompida, operação já aceita e persistência.

**Dependências e decisões:** UC34; D03/D11. Estratégias de mesclagem/descarte não estão aprovadas na fonte.

---

## Rastreabilidade dos 62 requisitos presentes

Cada linha aponta casos que detalham o requisito e regras comuns que completam seu aceite. Requisito transversal não precisa virar caso de uso artificial: sua implementação integra os fluxos consumidores.

- R01: UC01.
- R02: UC02.
- R03: UC01; UC02; RT01.
- R05: UC03; UC05.
- R06: UC03; UC04; UC05.
- R07: UC04.
- R08: UC04.
- R09: UC05; RT02.
- R10: UC06.
- R11: UC07.
- R12: UC03; UC05; UC07.
- R13: UC04.
- R14: UC04; UC07.
- R15: UC08.
- R16: UC08; UC09.
- R17: UC04; UC08.
- R19: UC13; UC19.
- R20: UC11.
- R21: UC11.
- R22: UC09.
- R23: UC09; RT03.
- R24: UC09; UC33.
- R25: UC09; UC33; RT03.
- R26: UC10.
- R27: UC14.
- R28: UC15.
- R29: UC17; UC18.
- R30: UC16; UC19.
- R31: UC18.
- R32: UC22.
- R33: UC20.
- R34: UC15; UC18; UC20; UC21; UC22.
- R35: UC21.
- R36: UC23.
- R37: UC24.
- R39: UC12; UC13; UC24.
- R40: UC26; RT06.
- R41: UC26; RT06.
- R42: UC04; UC11; UC15; UC22; UC25; UC26; UC27; RT07.
- R45: UC23; UC25; RT05/RT06.
- R46: UC23; UC25; UC26; RT06.
- R47: UC26; RT06.
- R48: UC27; RT01/RT06.
- R49: UC25; RT05/RT06.
- R50: UC23; UC25; UC26; UC27; UC28; UC30; RT06.
- R51: UC28.
- R52: UC29.
- R53: UC30.
- R55: UC29; UC31; UC32.
- R57: UC08; UC28; UC29; UC32.
- R58: UC33; RT08.
- R59: UC34; UC35; RT03/RT08.
- R61: UC11; RT08.
- R62: UC34; UC35; RT08.
- R63: UC09; UC12; UC13; UC14; UC16; UC17; UC19; UC20; UC21; UC24; UC33; UC34; RT02.
- R64: UC09; UC14; UC18; UC20; UC21; UC22; UC33; UC34; RT02/RT05.
- R65: UC12; UC13; UC16; UC17; UC19; UC21; UC24; UC35; RT02.
- R66: UC09; UC12; UC14; UC20; UC21; UC24; UC33; UC34; RT02.
- R67: UC09; UC12; UC13; UC18; UC20; UC21; UC24; UC33; UC34; UC35; RT03.
- R68: RT07.
- R69: UC09; UC33; RT07.
- R70: UC25; RT07.

## Como transformar os casos em tasks do Jira

Usar o título completo, por exemplo: UC09 — Registrar venda detalhada. Na descrição, copiar objetivo, requisitos, pré-condições, fluxo, alternativas, pós-condições e critérios de aceite. Vincular os casos dependentes; decisões D pendentes são bloqueios explícitos da regra afetada, não requisitos inventados.

**Subtarefas sugeridas por caso:**

1. Contrato e modelagem: definir campos, exemplos, estados, validações, precisão e erros com backend e frontend.
2. Backend: implementar a responsabilidade indicada no caso, incluindo persistência e isolamento quando aplicáveis.
3. Frontend: implementar o fluxo, estados de tela e integração indicados.
4. Testes e integração: executar os critérios do caso e as regras transversais aplicáveis.

Essas quatro categorias são um ponto de partida, não uma obrigação de criar endpoints ou tabelas para cada caso. UC10 pode ser cálculo local; UC33 contém trabalho predominante no cliente. Serviços compartilhados de autorização, auditoria, cálculo e idempotência devem ser implementados uma vez e vinculados aos casos consumidores.

**Critério de pronto para desenvolver:** decisões que mudam comportamento resolvidas; contrato com exemplos acordado; dependências identificadas; cenários de sucesso, erro e concorrência verificáveis; layout/estados definidos. As propostas deste documento devem ser aprovadas pela equipe quando ultrapassarem a redação da fonte.

**Critério de concluído:** backend e frontend integrados; critérios específicos e transversais demonstrados; autorização verificada diretamente na API; valores e datas consistentes entre tela, histórico e relatório; erros preservam dados recuperáveis; migração e compatibilidade dos registros existentes verificadas quando houver; nenhuma decisão pendente escondida em código.

## Ordem sugerida de desenvolvimento

1. **Fundação:** D01/D02, isolamento, identidade dos registros, datas, contrato de erros e estratégia de idempotência. Definir cedo o formato dos IDs para não inviabilizar offline depois.
2. **Primeiro fluxo utilizável:** UC01, UC03–UC07, UC09–UC11 e UC20. Resolver D03 antes de habilitar o controle opcional de quantidade; permitir venda sem esse controle conforme a fonte.
3. **Consistência e encomendas:** UC12–UC19, UC21–UC24, após D04–D07. Desenvolver conversão e recebimentos como uma mesma obrigação para evitar retrabalho.
4. **Visão do negócio:** UC25–UC27 após estabilizar regras de elegibilidade e cálculos.
5. **Offline completo:** UC33–UC35 com D11; projetar armazenamento e contratos desde a fundação, mesmo que a entrega venha depois.
6. **Planejamento:** UC28–UC32 quando dados e regras D08–D10 estiverem definidos. UC02 permanece condicionado a D12.

Esta ordem é proposta de execução, não alteração de prioridade dos requisitos.

## Cenários integrados de referência

**SI01 — Venda simples.** Cadastrar Pão de queijo a R$ 4,50 sem categoria, custo ou estoque. Registrar duas unidades: venda R$ 9,00. Calcular troco para R$ 10,00: R$ 1,00, sem criar recebimento. Informar R$ 9,00 recebidos: painel separa R$ 9,00 vendidos e R$ 9,00 recebidos. Repetir envio não duplica.

**SI02 — Encomenda com sinal.** Cadastrar pedido de R$ 100; informar sinal R$ 30; alterar data de entrega; concluir no evento aprovado: uma venda R$ 100, um recebimento R$ 30, saldo R$ 70 no cenário sem ajustes. Informar R$ 70: saldo zero. Estado de entrega e saldo financeiro permanecem conceitos distintos.

**SI03 — Conferência do dia.** R$ 160 já registrados; acrescentar R$ 40 não registrados na forma agregada, se aprovada: R$ 200 em vendas. O resumo de R$ 40 não altera contagem de produtos, transações detalhadas nem horários. Repetição da operação mantém R$ 200.

**SI04 — Falha de rede.** Registrar venda offline, fechar e abrir aplicativo: recuperar pendência. Servidor aceita no retorno da conexão, mas resposta é perdida: reenviar a mesma operação, receber o mesmo identificador e marcar sincronizado somente após confirmação.

**SI05 — Histórico e cancelamento.** Alterar preço do catálogo após venda não muda a venda antiga. Cancelar a venda exige confirmação, preserva recebimento declarado e retira a contribuição da venda válida. Devolução externa é outro registro e segue D05; não inferir devolução de produto apenas por seu valor.

**SI06 — Isolamento.** Usuário do negócio A tenta consultar, editar, exportar e sincronizar registros do negócio B por identificador conhecido: todas as ações são negadas sem exposição dos dados.

## Limites desta versão

Os casos detalham todos os requisitos presentes na fonte consultada. As ausências da numeração foram preservadas. Questões D01–D12 e a política de exclusão de categoria em uso são pontos para decisão da equipe antes do aceite das partes afetadas. Nenhum requisito original foi alterado por esta documentação. A especificação não pressupõe stack, banco, arquitetura de implantação ou contratos HTTP já aprovados.
