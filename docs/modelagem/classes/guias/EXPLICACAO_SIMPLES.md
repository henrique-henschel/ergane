# Entendendo as classes da Ergane, sem complicação

Imagine que a Ergane organiza as anotações de uma pequena loja de bolos.

**Conta** é o negócio da pessoa. **Usuario** é quem entra; **AcessoNegocio** diz em quais negócios ela pode trabalhar. O mesmo usuário pode participar de mais de um negócio, mas um não pode enxergar os dados do outro.

**Produto** é a ficha atual do que é vendido: um bolo, preço, nome, custo (se conhecido). **Categoria** agrupa produtos. **AcompanhamentoQuantidade** decide se é possível falar de saldo disponível, sem fingir que estoque desconhecido é zero.

**Venda** representa uma venda registrada, mas não prova que o cliente pagou. **ItemVenda** é a fotografia exata dos itens naquele momento: preço e nome ficam congelados para o histórico. Por isso mudar o preço atual do bolo não reescreve as vendas antigas.

**Encomenda** é a promessa de uma entrega futura; **ItemEncomenda** guarda os detalhes quando existem. O vendedor pode anotar apenas 'bolo de aniversário da Maria' e um total, sem inventar produtos de catálogo. Quando a encomenda é concluída, pode originar uma **Venda** segundo D04.

**ObrigacaoFinanceira** é a ligação que evita duplicar dinheiro: se a Maria deixou R$ 30 de sinal numa encomenda de R$ 100, a conversão em venda não cria um segundo sinal. **RecebimentoInformado** diz o que o vendedor declarou receber fora da Ergane. **DevolucaoInformada** registra uma devolução de valor ocorrida fora do app. Essas classes **não** fazem cobranças bancárias.

**ComplementoAgregado** registra um total adicional de vendas que não foram detalhadas; não é uma venda com 'itens imaginários'. **OperacaoIdempotente** lembra uma requisição já executada e impede que um clique duplo crie duas vendas. **AlteracaoAuditada** registra o que mudou, por quem e quando.

**Dinheiro**, **Quantidade** e **Ocorrencia** padronizam cálculo, quantidade e data/hora do acontecimento. Uma data sem hora não pode ser inventada como meia-noite.

**IndicadoresPeriodo**, **CoberturaDados**, **SugestaoReposicao** e **CombinacaoObservada** são resultados de consultas. Eles precisam mostrar quando os dados estão incompletos, em vez de apresentar certezas falsas.

**MetaVendas**, **PreferenciaNotificacao** e **EventoAviso** organizam metas e notificações. **CacheNegocio**, **OperacaoLocal** e **ConflitoSincronizacao** preservam vendas feitas sem internet, suas pendências e os problemas de envio.

## Exemplo integrado

1. A loja cadastra 'Bolo' a R$ 50.
2. Registra duas unidades numa venda: R$ 100, com os itens preservados em `ItemVenda`.
3. A venda existe, mas ainda não existe recebimento informado.
4. O vendedor informa R$ 40 recebidos: a obrigação registra saldo de R$ 60 no cenário simples aprovado.
5. Ao mudar o preço do bolo para R$ 55, aquela venda ainda exibe R$ 50 por unidade.

As regras exatas de excesso, cancelamentos e devoluções permanecem condicionadas à D05.
