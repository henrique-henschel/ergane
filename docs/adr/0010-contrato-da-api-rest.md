# ADR-0010: Contrato da API REST
- Status: Aceita
- Data: 2026-11-02

## Contexto
O ecossistema do Ergane integra front-ends em React (web) e React Native (mobile) a um back-end Spring Boot. Para garantir manutenibilidade e evitar quebras de compatibilidade, os contratos de interface devem ser estáveis, previsíveis e bem documentados (RT09). O sistema requer uma estratégia clara para tratamento de erros que oriente o usuário final e os formulários de tela (RT07), sem vazar dados estruturais internos ou permitir que consultas a identificadores de outros negócios confirmem a existência de dados alheios (RT01). Além disso, o modelo de dados interno de persistência (entidades JPA) não pode ser exposto publicamente para os clientes.

## Decisão
Padronizar a comunicação do sistema através de uma API REST sob o prefixo `/api`, utilizando substantivos no plural para recursos, DTOs imutáveis modelados como Java `records` e respostas de erro estruturadas segundo o padrão ProblemDetail (RFC 9457).

O contrato estabelece as seguintes convenções:
- Recursos organizados no plural precedidos de `/api`: `/api/categorias`, `/api/produtos`;
- Isolamento total entre transporte e persistência: entidades JPA nunca são expostas em controladores. Toda entrada trafega via `Request` record (com anotações de Bean Validation como `@NotBlank`, `@Size`, `@Digits`) e toda saída via `Response` record (com métodos estáticos de mapeamento, ex.: `CategoriaResponse.de(categoria)`);
- Códigos de status HTTP padronizados:
  - `200 OK`: consultas e atualizações síncronas realizadas com sucesso;
  - `201 Created`: criação bem-sucedida de recurso, acompanhada obrigatoriamente do cabeçalho `Location` contendo o URI do novo registro;
  - `204 No Content`: exclusões físicas efetuadas com sucesso;
  - `400 Bad Request`: erros de validação sintática ou de campos obrigatórios;
  - `404 Not Found`: recurso inexistente ou pertencente a outro negócio (RT01);
  - `409 Conflict`: violações de regras de negócio ou de integridade no banco (conflito de unicidade).
- Tratamento global de erros centralizado via `@RestControllerAdvice` (`TratadorDeErros`), emitindo instâncias de `ProblemDetail` (RFC 9457). Para erros de validação, adiciona-se a propriedade `campos` mapeando o nome de cada campo à respectiva mensagem amigável de erro;
- Ações operacionais que não representam um CRUD tradicional utilizam sub-recursos expressos com verbos em métodos POST (ex.: `POST /api/produtos/{id}/arquivar`), explicitando que a ação não constitui uma deleção HTTP DELETE;
- Decisão pendente de definição com a equipe: inclusão de versionamento explícito no caminho da URL (ex.: `/api/v1/produtos`). No estágio atual do MVP, as rotas utilizam o prefixo direto `/api`.

## Alternativas consideradas
- **GraphQL:** traria complexidade desnecessária para o escopo do MVP, exigindo gerenciar esquemas adicionais, controle de autorização a nível de campos no multi-tenant e mitigação de consultas excessivamente custosas, além de não aproveitar a semântica nativa de cache e status do HTTP.
- **Exposição direta de entidades JPA nos controladores:** gera riscos severos de segurança (*mass assignment*), ciclos infinitos de serialização JSON com relacionamentos bidirecionais e acoplamento perigoso entre o esquema do banco de dados e os contratos de tela do front-end.

## Consequências
- **Ganhos:** contratos estáveis, seguros e autoexplicativos; padronização de tratamento de falhas em conformidade com o padrão RFC 9457; proteção do domínio interno contra acoplamento direto; suporte claro à validação de formulários no front-end por meio do mapa `campos`.
- **Custos e limitações:** maior quantidade de classes records de DTOs criadas para entrada e saída de cada funcionalidade; necessidade de mapeadores manuais entre entidades e DTOs; pendência na definição de versionamento formal no caminho (`/v1`) da API.
