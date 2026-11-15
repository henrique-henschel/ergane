# Representação de dinheiro
- Status: accepted
- Date: 2026-11-02

## Contexto
O Ergane é uma plataforma de controle comercial e financeiro. Operações com preços de venda, custos de mercadoria, totais de pedidos, trocos e recebimentos parciais exigem precisão matemática absoluta. Tipos binários de ponto flutuante sofrem de imprecisão inerente da representação IEEE 754, gerando dízimas e perdas de centavos que inviabilizam relatórios contábeis. Além disso, as regras do negócio (RT04 e D02) exigem que a ausência de valor informado não seja confundida com o número zero (custo não informado é diferente de custo zero), e que valores informados pelo front-end não sejam aceitos cegamente como totais finais.

## Decisão
Representar todos os valores monetários por meio da classe `BigDecimal` no Java e do tipo `NUMERIC(12,2)` no PostgreSQL, com validação de escala na entrada e cálculo de totais realizado soberanamente pelo back-end.

A política de tratamento financeiro compreende:

- Tipos de dados monetários padronizados: `BigDecimal` na camada de aplicação e `NUMERIC(12, 2)` no schema relacional, com restrições `CHECK` que barram valores negativos (`preco >= 0`, `custo >= 0`);
- Rejeição de arredondamento silencioso: na construção e atualização de entidades (ex.: `Produto.java`), a escala é normalizada com `setScale(2, RoundingMode.UNNECESSARY)`, gerando exceção imediata caso um valor com mais de duas casas decimais tente ser atribuído;
- Validação declarativa de entrada via Bean Validation nos DTOs de Request (ex.: `ProdutoRequest`), usando `@Digits(integer = 10, fraction = 2)` e `@DecimalMin(value = "0.00")` para barrar entradas sintaticamente inválidas antes de chegarem ao serviço;
- Distinção semântica de valores ausentes (RT04): campos opcionais como custo ou saldo utilizam tipos envelopados e preservam `null` quando não informados, impedindo que métricas financeiras considerem produtos com custo zero (o que falsearia margens de lucro);
- Soberania de cálculo no back-end: valores totais de transações, itens e recebimentos são invariavelmente somados e computados no back-end, sem confiar em totais calculados ou enviados pelo front-end;
- Moeda única oficial estabelecida como Real Brasileiro (BRL) para o escopo do MVP (D02).

## Alternativas consideradas

- **Ponto flutuante binário (float / double):** descartado por acumular erros de arredondamento inerentes à representação binária fracionária, que causam distorções inaceitáveis em somatórios e fechamentos de caixa.
- **Inteiro representando centavos (long / int):** embora evite problemas de ponto flutuante, introduz atrito contínuo de conversão (multiplicações e divisões por 100) em queries, validações de formulário, formatação em relatórios e mapeamento com JPA/Hibernate, sendo menos natural que o suporte nativo de `NUMERIC` e `BigDecimal`.

## Consequências

- **Ganhos:** exatidão contábil e matemática rigorosa em todas as operações; proteção contra manipulação de totais no cliente; consistência entre relatórios, telas e banco de dados; fidelidade às regras RT04 e D02.
- **Custos e limitações:** maior verbosidade na escrita de código Java devido à imutabilidade e métodos de cálculo do `BigDecimal`; necessidade de garantir a serialização JSON consistente com duas casas decimais; suporte a múltiplas moedas e conversão cambial não contemplado no MVP.
