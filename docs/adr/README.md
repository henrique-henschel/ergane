# Architecture Decision Records (ADRs) - Ergane

Registro das decisões arquiteturais e técnicas fundamentais que orientam o desenvolvimento do monorepo Ergane.

## Índice de Decisões

> Esta tabela é mantida **manualmente**: ao criar uma ADR, acrescente a linha correspondente aqui. O site gerado pelo Log4brains (`npm run adr:preview`) lista as ADRs automaticamente.

| Data | Título | Status | Documento |
| :---: | :--- | :---: | :--- |
| 2026-10-25 | Usar Log4brains para manter as ADRs | Aceita | [20261025-usar-log4brains-para-manter-as-adrs](20261025-usar-log4brains-para-manter-as-adrs.md) |
| 2026-11-02 | Java 21 + Spring Boot como back-end | Aceita | [20261102-java-21-spring-boot-como-back-end](20261102-java-21-spring-boot-como-back-end.md) |
| 2026-11-02 | PostgreSQL como banco de dados | Aceita | [20261102-postgresql-como-banco-de-dados](20261102-postgresql-como-banco-de-dados.md) |
| 2026-11-02 | Monólito modular organizado por funcionalidade | Aceita | [20261102-monolito-modular-organizado-por-funcionalidade](20261102-monolito-modular-organizado-por-funcionalidade.md) |
| 2026-11-02 | Monorepo | Aceita | [20261102-monorepo](20261102-monorepo.md) |
| 2026-11-02 | Migrations versionadas com Flyway | Aceita | [20261102-migrations-versionadas-com-flyway](20261102-migrations-versionadas-com-flyway.md) |
| 2026-11-02 | UUID como chave primária única | Aceita | [20261102-uuid-como-chave-primaria-unica](20261102-uuid-como-chave-primaria-unica.md) |
| 2026-11-02 | Isolamento por negócio (multi-tenant) | Aceita | [20261102-isolamento-por-negocio-multi-tenant](20261102-isolamento-por-negocio-multi-tenant.md) |
| 2026-11-02 | Representação de dinheiro | Aceita | [20261102-representacao-de-dinheiro](20261102-representacao-de-dinheiro.md) |
| 2026-11-02 | Política de exclusão e histórico | Aceita | [20261102-politica-de-exclusao-e-historico](20261102-politica-de-exclusao-e-historico.md) |
| 2026-11-02 | Contrato da API REST | Aceita | [20261102-contrato-da-api-rest](20261102-contrato-da-api-rest.md) |

---

### Formato e Padrão
As ADRs deste projeto seguem o modelo clássico de Michael Nygard, com os metadados (`Status`, `Date`, `Deciders`, `Tags`) exigidos pelo Log4brains, estruturado em:

- **Contexto:** o problema e as restrições que motivaram a decisão.
- **Decisão:** o direcionamento técnico objetivo e seus detalhes práticos de implementação.
- **Alternativas consideradas:** opções avaliadas e os motivos de não terem sido adotadas.
- **Consequências:** ganhos obtidos, limitações assumidas e itens postergados.

---

### Log4brains
As ADRs são gerenciadas com o [Log4brains](https://github.com/thomvaill/log4brains) (configuração em `.log4brains.yml`). Requer Node.js e `npm install` na raiz do repositório.

| Comando | Descrição |
| :--- | :--- |
| `npm run adr:new` | cria uma nova ADR a partir do `template.md` |
| `npm run adr:list` | lista as ADRs e seus status |
| `npm run adr:preview` | site local com hot reload em http://localhost:4004 |
| `npm run adr:build` | gera o site estático em `.log4brains/out` |

Cada ADR é um arquivo `AAAAMMDD-slug.md` (data de criação + título em minúsculas, sem acentos e com hífens), por exemplo `20261102-monorepo.md`. O `npm run adr:new` já gera o nome nesse padrão. O Log4brains ordena as ADRs pelo campo `Date` e exibe as mais recentes primeiro; ADRs com a mesma data ficam em ordem alfabética pelo nome do arquivo.

Ao criar uma ADR, preencha `Status` e `Date`: enquanto o `Status` mantiver o texto do template, ela aparece como rascunho (`draft`).

Os metadados no topo de cada ADR usam chaves e status em inglês, para o Log4brains interpretá-los:
`Status` (`draft`, `proposed`, `accepted`, `rejected`, `deprecated` ou `superseded by [..](..)`), `Date` (`AAAA-MM-DD`) e, opcionalmente, `Deciders` e `Tags`. O corpo do documento permanece em português.
