# Isolamento por negócio (multi-tenant)
- Status: accepted
- Date: 2026-11-02

## Contexto
O Ergane atende múltiplos negócios e vendedores de maneira concorrente em uma única base operacional. A regra transversal RT01 e os critérios de aceite de acesso (UC01) estabelecem que um usuário autenticado jamais deve visualizar, consultar ou alterar dados pertencentes a outro negócio. Além disso, consultar o identificador de um recurso que pertença a outro negócio não pode sequer revelar sua existência. A arquitetura precisa garantir esse isolamento absoluto com simplicidade operacional, evitando soluções que onerem desproporcionalmente o custo de manutenção da equipe.

## Decisão
Implementar o isolamento multi-tenant por meio de segregação lógica de dados baseada na coluna discriminadora `negocio_id` em todas as tabelas principais, combinando controle centralizado na aplicação e restrições de chave estrangeira composta no PostgreSQL.

O isolamento é estruturado com as seguintes regras:

- Toda tabela de entidade de negócio possui a coluna `negocio_id UUID NOT NULL REFERENCES negocios (id) ON DELETE RESTRICT`. Tabelas filhas estritamente dependentes de uma entidade-mãe (ex.: itens de uma venda) podem prescindir da coluna quando o acesso for estritamente subordinado à chave da entidade-mãe;
- Todas as consultas de leitura e escrita filtram obrigatoriamente pelo identificador do negócio corrente (ex.: `findByNegocioIdAndId`, `findAllByNegocioId...`);
- Caso um ID solicitado não pertença ao negócio corrente, a aplicação lança `RecursoNaoEncontradoException`, resultando no código HTTP 404 (Not Found). O sistema nunca retorna HTTP 403 (Forbidden) nesses cenários, impedindo que um invasor confirme a existência de recursos de outros negócios (RT01);
- O componente gerenciado `ContextoNegocio` centraliza a identificação do tenant atual. Durante a fase inicial do MVP ele fornece um UUID fixo (`00000000-0000-0000-0000-000000000001`), e a partir do desenvolvimento do UC01 passará a extrair o negócio a partir do token ou sessão autenticada, sem necessidade de alterar as regras dos services;
- O campo `negocio_id` nunca é aceito a partir do cliente (não consta em DTOs de entrada como `CategoriaRequest` ou `ProdutoRequest`);
- Proteção em profundidade no banco de dados: chaves estrangeiras compostas como `CONSTRAINT fk_produtos_categoria FOREIGN KEY (negocio_id, categoria_id) REFERENCES categorias (negocio_id, id)` garantem fisicamente que um produto de um negócio nunca possa apontar para uma categoria de outro negócio.

## Alternativas consideradas

- **Banco de dados ou Schema por negócio (Database/Schema-per-tenant):** geraria alta complexidade na execução de migrações com Flyway (dezenas ou centenas de schemas a cada deploy) e consumo excessivo de conexões de banco de dados, sendo inviável para o modelo de pequenos negócios com margem baixa.
- **Row-Level Security (RLS) nativo do PostgreSQL:** embora elegante no banco, adiciona complexidade ao gerenciar variáveis de sessão transacionais com pools de conexões (HikariCP) no Spring Boot, dificultando o rastreamento e a depuração de consultas na camada de aplicação.

## Consequências

- **Ganhos:** garantia rigorosa de estanqueidade lógica entre negócios; mitigação de vazamento de existência de dados com HTTP 404; integridade referencial protegida tanto na aplicação quanto no banco por constraints compostas; ponto focal limpo de migração futura para autenticação no bean `ContextoNegocio`.
- **Custos e limitações:** requer disciplina estrita dos desenvolvedores para nunca omitir o filtro por `negocio_id` em novas consultas e métodos de repositório; necessidade de criar índices e constraints auxiliares como `(negocio_id, id)` nas tabelas de referência.
