# ADR-0002: PostgreSQL como banco de dados
- Status: Aceita
- Data: 2026-11-02

## Contexto
O domínio do Ergane possui forte natureza relacional e transacional, interligando negócios, categorias, produtos vendáveis, itens de vendas, encomendas e recebimentos (UC01 a UC35, RT01 a RT06). Operações de venda, controle de saldos pendentes e histórico financeiro não toleram perda de dados nem inconsistências parciais. Além disso, regras como diferenciação de produtos com mesmo nome (UC03, UC05) e integridade referencial por negócio exigem recursos avançados de indexação e restrições no nível de armazenamento. O ambiente local de desenvolvimento precisa ser padronizado e facilmente reproduzível entre os membros do grupo.

## Decisão
Adotar o PostgreSQL (versão 17) como o banco de dados relacional oficial do projeto, executado localmente por meio do Docker Compose.

A aplicação delega garantias críticas de consistência e integridade ao próprio banco:
- Integridade referencial com chaves estrangeiras declaradas com `ON DELETE RESTRICT`, impedindo a deleção em cascata de dados históricos;
- Restrições `CHECK` a nível de coluna (ex.: `preco >= 0`, `custo >= 0`, `TRIM(nome) <> ''`), garantindo que dados inválidos nunca sejam persistidos;
- Tipos de dados nativos adequados ao domínio: `NUMERIC(12,2)` para exatidão monetária, `TIMESTAMPTZ` para instantes temporais com fuso horário, e tipo `UUID` nativo com geração via `gen_random_uuid()`;
- Recursos avançados do PostgreSQL já empregados no schema:
  - Índices únicos baseados em expressões funcionais (ex.: `LOWER(nome)` em categorias e `(LOWER(nome), LOWER(COALESCE(diferenciador, '')))` em produtos);
  - Índices parciais com filtro condicional (ex.: `WHERE arquivado_em IS NULL`), permitindo que um novo produto utilize o nome de um produto previamente arquivado;
  - Chaves estrangeiras compostas (ex.: `(negocio_id, categoria_id) REFERENCES categorias(negocio_id, id)`), impedindo em nível de banco o relacionamento cruzado entre entidades de negócios distintos.
- Ambiente local padronizado pelo serviço `db` com imagem `postgres:17` no arquivo `docker-compose.yml`.

## Alternativas consideradas
- **MySQL / MariaDB:** embora relacionais, possuem suporte histórico menos ergonômico para índices parciais e índices com expressões funcionais complexas, além de tratamento nativo de UUID e tipos de data com timezone menos consistentes que os do PostgreSQL.
- **Bancos de documentos (NoSQL / MongoDB):** descartados por serem inadequados para dados financeiros e transacionais com forte interdependência relacional. Modelar vendas, recebimentos e categorias em coleções desnormalizadas exigiria implementar garantias ACID e validações de integridade manualmente no código da aplicação, criando alto risco de corrupção contábil.

## Consequências
- **Ganhos:** garantias de integridade e consistência delegadas ao motor de banco de dados; suporte maduro a UUID, tipos monetários exatos e fusos horários; índices avançados que simplificam regras complexas de validação; ambiente de desenvolvimento unificado e isolado via Docker.
- **Custos e limitações:** obrigatoriedade de ter o daemon do Docker ativo no ambiente de desenvolvimento local para execução da API; necessidade de gerenciar índices e constraints com atenção durante a escrita de novas migrações SQL.
