# ADR-0005: Migrations versionadas com Flyway
- Status: Aceita
- Data: 2026-11-02

## Contexto
O banco de dados relacional é o pilar de integridade e histórico contábil do Ergane. Em um ambiente com múltiplos desenvolvedores, a criação ou alteração manual de tabelas e índices acarreta desvios de schema (*schema drift*), quebras de ambiente e inconsistências de difícil diagnóstico. Por outro lado, delegar a criação e alteração do banco ao mecanismo automático do ORM Hibernate (`ddl-auto=update` ou `create-drop`) retira o controle sobre tipos de dados precisos, restrições `CHECK`, índices parciais e scripts de carga controlados, além de oferecer risco de perda de dados.

## Decisão
Gerenciar a criação e evolução de todo o schema do PostgreSQL exclusivamente através de scripts SQL versionados com a ferramenta Flyway, mantendo o Hibernate em modo estrito de validação (`ddl-auto=validate`).

As diretrizes adotadas compreendem:
- O schema é inteiramente definido em arquivos SQL na pasta `apps/api/src/main/resources/db/migration/`, seguindo a convenção de nomes `V{N}__{descricao}.sql` e escritos em SQL puro;
- O Hibernate atua unicamente validando o mapeamento das entidades contra as tabelas reais no startup da aplicação (`spring.jpa.hibernate.ddl-auto=validate`), abortando a inicialização caso haja qualquer divergência de tipos, colunas ou constraints;
- Princípio da imutabilidade: uma migration aplicada e versionada nunca é editada; toda nova alteração, correção ou ajuste estrutural exige a criação de uma nova migration com numeração sequencial subsequente;
- Cargas de dados iniciais ou essenciais para desenvolvimento local (como a criação do negócio fixo em `V2__dados_iniciais_dev.sql`) são provisionadas por migrations dedicadas e com instruções idempotentes (`ON CONFLICT DO NOTHING`).

## Alternativas consideradas
- **Hibernate ddl-auto=update:** cria ou modifica schemas de forma imprevisível e não versionada, não suporta índices parciais ou funcionais avançados do PostgreSQL e apresenta alto risco de corrupção ou inconsistência estrutural entre instâncias do banco.
- **Liquibase:** embora seja uma ferramenta robusta com suporte a múltiplos formatos (YAML, XML, JSON), adiciona uma camada de abstração e verbosidade desnecessárias para o porte do projeto. O Flyway permite trabalhar diretamente com a sintaxe nativa do PostgreSQL, que é mais transparente e eficiente para o time.

## Consequências
- **Ganhos:** rastreabilidade total da evolução do schema no histórico do Git; garantia de ambientes de desenvolvimento, teste e homologação idênticos; validação automática no startup da aplicação prevenindo discrepâncias entre código Java e banco; controle fino sobre recursos avançados do PostgreSQL (constraints, índices parciais e tipos numéricos exatos).
- **Custos e limitações:** necessidade de escrever scripts DDL manualmente para toda alteração de entidade; impossibilidade de editar migrações que já foram executadas por outros desenvolvedores, exigindo migrações compensatórias caso seja necessário corrigir uma alteração prévia.
