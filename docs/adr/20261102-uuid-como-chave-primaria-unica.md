# UUID como chave primária única
- Status: accepted
- Date: 2026-11-02

## Contexto
O Ergane é um sistema multi-tenant (RT01) com previsão de operação em modo offline e sincronização posterior para registro de vendas (UC33 a UC35, RT08). O uso tradicional de identificadores sequenciais inteiros (`SERIAL` ou `BIGINT`) apresenta duas fragilidades críticas: expõe publicamente o volume de registros e transações do negócio por meio de rotas previsíveis (`/api/produtos/1`, `/api/vendas/42`), facilitando ataques de enumeração direta; e depende obrigatoriamente de uma conexão síncrona com o banco central para alocação do próximo valor sequencial, o que inviabiliza que dispositivos clientes gerem identificadores válidos e finais enquanto desconectados da internet.

## Decisão
Adotar UUID (versão 4) como o padrão exclusivo de chave primária técnica de coluna única para todas as tabelas do sistema.

A implementação segue os seguintes critérios:

- Toda tabela possui uma chave primária composta por uma única coluna `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`. Nenhuma tabela utiliza chave primária composta;
- Os identificadores expostos nas URLs REST são imprevisíveis e não enumeráveis, preservando a confidencialidade do volume de operações e dificultando varreduras indevidas entre diferentes contas;
- Viabiliza a geração do identificador fora do banco de dados (no cliente web, mobile ou na aplicação), pré-requisito indispensável para a criação local de vendas e itens no modo offline (UC33) e para o suporte a requisições idempotentes (RT03), onde o cliente envia uma operação repetida com a mesma chave sem duplicar registros;
- Quando o negócio exigir um identificador numérico curto e legível para comunicação com o usuário final (como o número sequencial de uma venda ou pedido), esse valor será armazenado em uma coluna de negócio separada, sem exercer o papel de chave primária técnica da entidade.

## Alternativas consideradas

- **SERIAL / BIGINT sequencial autoincremento:** descartado porque não permite que dispositivos móveis offline criem entidades com IDs definitivos antes de sincronizar, revelaria o volume de dados de cada negócio nas URLs públicas e facilitaria ataques de enumeração.
- **Chave primária composta (ex.: negócio_id + id sequencial):** adiciona grande complexidade de mapeamento no JPA/Hibernate (`@EmbeddedId` ou `@IdClass`), dificulta o consumo em rotas REST padronizadas e prejudica a integração com bibliotecas de front-end.

## Consequências

- **Ganhos:** permite a geração descentralizada de chaves no modo offline; reforça a segurança contra enumeração em endpoints públicos; padroniza o mapeamento JPA com `@GeneratedValue(strategy = GenerationType.UUID)`; simplifica rotas REST com um único identificador.
- **Custos e limitações:** maior consumo de espaço em disco e memória comparado a inteiros (16 bytes versus 4 ou 8 bytes por registro); UUIDs puramente aleatórios (v4) não possuem ordenação cronológica natural, exigindo ordenação explícita nas consultas via campos temporais (`criado_em`); ligeiro aumento na fragmentação de índices B-tree sob alto volume de escrita.
