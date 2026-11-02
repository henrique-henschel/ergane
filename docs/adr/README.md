# Architecture Decision Records (ADRs) - Ergane

Registro das decisões arquiteturais e técnicas fundamentais que orientam o desenvolvimento do monorepo Ergane.

## Índice de Decisões

| Número | Título | Status | Documento |
| :---: | :--- | :---: | :--- |
| **0001** | Java 21 + Spring Boot como back-end | Aceita | [ADR-0001](0001-java-21-spring-boot-como-back-end.md) |
| **0002** | PostgreSQL como banco de dados | Aceita | [ADR-0002](0002-postgresql-como-banco-de-dados.md) |
| **0003** | Monólito modular organizado por funcionalidade | Aceita | [ADR-0003](0003-monolito-modular-organizado-por-funcionalidade.md) |
| **0004** | Monorepo | Aceita | [ADR-0004](0004-monorepo.md) |
| **0005** | Migrations versionadas com Flyway | Aceita | [ADR-0005](0005-migrations-versionadas-com-flyway.md) |
| **0006** | UUID como chave primária única | Aceita | [ADR-0006](0006-uuid-como-chave-primaria-unica.md) |
| **0007** | Isolamento por negócio (multi-tenant) | Aceita | [ADR-0007](0007-isolamento-por-negocio-multi-tenant.md) |
| **0008** | Representação de dinheiro | Aceita | [ADR-0008](0008-representacao-de-dinheiro.md) |
| **0009** | Política de exclusão e histórico | Aceita | [ADR-0009](0009-politica-de-exclusao-e-historico.md) |
| **0010** | Contrato da API REST | Aceita | [ADR-0010](0010-contrato-da-api-rest.md) |

---

### Formato e Padrão
As ADRs deste projeto seguem o modelo clássico de Michael Nygard estruturado em:
- **Contexto:** o problema e as restrições que motivaram a decisão.
- **Decisão:** o direcionamento técnico objetivo e seus detalhes práticos de implementação.
- **Alternativas consideradas:** opções avaliadas e os motivos de não terem sido adotadas.
- **Consequências:** ganhos obtidos, limitações assumidas e itens postergados.
