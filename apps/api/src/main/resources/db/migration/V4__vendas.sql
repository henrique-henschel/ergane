-- =====================================================================
-- V4 · VENDAS
-- O coração do sistema: o que aconteceu no balcão.
--
-- Pense no cupom fiscal: venda = cabeçalho, item_venda = linhas,
-- recebimento = os pagamentos lá embaixo.
-- =====================================================================


-- ---------------------------------------------------------------------
-- numeracao_venda: o "Venda nº 152" mostrado ao cliente.
-- O uuid é para o sistema; o número sequencial é para gente.
--
-- Por que uma tabela e não MAX(numero) + 1? Porque dois vendedores
-- salvando ao mesmo tempo leriam o mesmo MAX e gerariam números
-- repetidos. Com esta tabela a aplicação faz:
--     UPDATE numeracao_venda SET ultimo_numero = ultimo_numero + 1
--     WHERE loja_id = ? RETURNING ultimo_numero;
-- O UPDATE trava a linha, então o segundo vendedor espera o primeiro e
-- recebe o número seguinte. Numeração por loja: cada uma começa do 1.
-- ---------------------------------------------------------------------
CREATE TABLE numeracao_venda (
    loja_id        uuid    PRIMARY KEY REFERENCES loja (id) ON DELETE RESTRICT,
    ultimo_numero  bigint  NOT NULL DEFAULT 0 CHECK (ultimo_numero >= 0)
);


-- ---------------------------------------------------------------------
-- venda: o cabeçalho.
--
-- Situações:
--   CONFIRMADA: a venda normal.
--   CANCELADA:  cancelamento formal (com motivo e quem cancelou). Devolve
--               o estoque e estorna o caixa.
--   DESFEITA:   o vendedor desfez o registro logo depois de salvar
--               (registrou errado).
--
-- subtotal, valor_desconto e total são DESNORMALIZADOS: dariam para
-- calcular a partir dos itens, mas são guardados para listar vendas rápido.
-- A aplicação mantém os três coerentes; o CHECK abaixo confere a conta.
--
-- Fiado: se a soma dos recebimentos for menor que o total, a diferença é
-- fiado, o cliente_id passa a ser obrigatório e nasce uma conta_receber
-- (V6). Isso é regra da APLICAÇÃO, porque depende de somar outra tabela
-- (um CHECK só enxerga a própria linha).
-- ---------------------------------------------------------------------
CREATE TABLE venda (
    id                       uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id                 uuid           NOT NULL REFERENCES conta (id)        ON DELETE RESTRICT,
    loja_id                  uuid           NOT NULL REFERENCES loja (id)         ON DELETE RESTRICT,
    vendedor_id              uuid           NOT NULL REFERENCES usuario (id)      ON DELETE RESTRICT,
    cliente_id               uuid           REFERENCES cliente (id)               ON DELETE RESTRICT,
    sessao_caixa_id          uuid           REFERENCES sessao_caixa (id)          ON DELETE RESTRICT,
    numero                   bigint         NOT NULL CHECK (numero > 0),
    situacao                 varchar(10)    NOT NULL DEFAULT 'CONFIRMADA'
        CHECK (situacao IN ('CONFIRMADA', 'CANCELADA', 'DESFEITA')),

    -- Desconto na venda inteira: R$ 5,00 (VALOR) ou 10% (PERCENTUAL).
    desconto_tipo            varchar(10)    CHECK (desconto_tipo IN ('VALOR', 'PERCENTUAL')),
    desconto_valor           numeric(15,2)  CHECK (desconto_valor >= 0),

    subtotal                 numeric(15,2)  NOT NULL CHECK (subtotal >= 0),         -- soma dos itens
    valor_desconto           numeric(15,2)  NOT NULL DEFAULT 0 CHECK (valor_desconto >= 0),  -- desconto em R$
    total                    numeric(15,2)  NOT NULL CHECK (total >= 0),
    observacao               varchar(500),
    registrada_em            timestamptz    NOT NULL DEFAULT now(),

    -- Cancelamento
    cancelada_em             timestamptz,
    motivo_cancelamento      varchar(20)
        CHECK (motivo_cancelamento IN ('REGISTRO_ERRADO', 'CLIENTE_DESISTIU', 'DEVOLUCAO', 'PAGAMENTO_NAO_CAIU', 'OUTRO')),
    observacao_cancelamento  varchar(500),
    cancelado_por_id         uuid           REFERENCES usuario (id) ON DELETE RESTRICT,

    desfeita_em              timestamptz,
    versao                   bigint         NOT NULL DEFAULT 0,
    criado_em                timestamptz    NOT NULL DEFAULT now(),
    atualizado_em            timestamptz    NOT NULL DEFAULT now(),

    UNIQUE (loja_id, numero),

    -- Tipo e valor do desconto andam juntos; percentual vai até 100.
    CHECK ((desconto_tipo IS NULL) = (desconto_valor IS NULL)),
    CHECK (desconto_tipo IS DISTINCT FROM 'PERCENTUAL' OR desconto_valor <= 100),

    -- A conta tem que fechar (e, como total >= 0, o desconto não passa do subtotal).
    CHECK (total = subtotal - valor_desconto),

    -- Cancelada exige data, motivo e quem cancelou. Desfeita exige data.
    CHECK (situacao <> 'CANCELADA' OR (cancelada_em IS NOT NULL AND motivo_cancelamento IS NOT NULL AND cancelado_por_id IS NOT NULL)),
    CHECK (situacao <> 'DESFEITA'  OR desfeita_em IS NOT NULL)
);

CREATE INDEX ix_venda_conta_data ON venda (conta_id, registrada_em);   -- "vendas de hoje", relatórios
CREATE INDEX ix_venda_cliente    ON venda (cliente_id);
CREATE INDEX ix_venda_sessao     ON venda (sessao_caixa_id);
CREATE INDEX ix_venda_vendedor   ON venda (vendedor_id);


-- ---------------------------------------------------------------------
-- venda_alerta_ignorado: o cliente estava bloqueado, com fiado atrasado ou
-- acima do limite, e o vendedor vendeu fiado mesmo assim. Fica registrado
-- (trilha de auditoria: o dono vê quem liberou).
-- ---------------------------------------------------------------------
CREATE TABLE venda_alerta_ignorado (
    venda_id  uuid         NOT NULL REFERENCES venda (id) ON DELETE CASCADE,
    alerta    varchar(10)  NOT NULL CHECK (alerta IN ('BLOQUEADO', 'ATRASADO', 'LIMITE')),
    PRIMARY KEY (venda_id, alerta)
);


-- ---------------------------------------------------------------------
-- item_venda: as linhas da venda.
--
-- SNAPSHOT: descrição, unidade, preço e custo são COPIADOS do catálogo no
-- momento da venda. Se o preço mudar amanhã, a venda de ontem continua
-- mostrando o valor que o cliente pagou.
--
-- item_vendavel_id NULL = "valor avulso": o vendedor digita "R$ 15,00" sem
-- produto cadastrado. É por isso que a descrição precisa ser copiada.
-- ---------------------------------------------------------------------
CREATE TABLE item_venda (
    id                uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    venda_id          uuid           NOT NULL REFERENCES venda (id) ON DELETE CASCADE,
    posicao           integer        NOT NULL CHECK (posicao >= 1),       -- ordem em que aparece no cupom
    item_vendavel_id  uuid           REFERENCES item_vendavel (id) ON DELETE RESTRICT,
    descricao         varchar(120)   NOT NULL,
    unidade           varchar(12)    NOT NULL
        CHECK (unidade IN ('UNIDADE', 'QUILOGRAMA', 'GRAMA', 'LITRO', 'PACOTE', 'CAIXA', 'DUZIA')),
    controla_estoque  boolean        NOT NULL,
    quantidade        numeric(15,3)  NOT NULL CHECK (quantidade > 0),     -- 3 casas: 0,350 kg
    preco_unitario    numeric(15,2)  NOT NULL CHECK (preco_unitario >= 0),
    custo_unitario    numeric(15,2)  CHECK (custo_unitario >= 0),         -- para o lucro; NULL se o custo era desconhecido
    criado_em         timestamptz    NOT NULL DEFAULT now(),
    atualizado_em     timestamptz    NOT NULL DEFAULT now(),

    UNIQUE (venda_id, posicao),

    -- Valor avulso não tem produto, então não tem estoque para baixar.
    CHECK (item_vendavel_id IS NOT NULL OR controla_estoque = false)
);

CREATE INDEX ix_item_venda_item_vendavel ON item_venda (item_vendavel_id);


-- ---------------------------------------------------------------------
-- recebimento: os pagamentos da venda. Uma venda de R$ 100 pode ter
-- R$ 60 no Pix e R$ 40 em dinheiro: duas linhas.
--
-- valor:          quanto abate do total da venda.
-- valor_entregue: só em dinheiro; o cliente deu R$ 50 para pagar R$ 42,
--                 então o troco é R$ 8 (valor_entregue - valor).
-- taxa:           % da maquininha COPIADO da configuração no momento.
--                 É custo do comerciante (R$ = valor * taxa / 100); o
--                 cliente paga o mesmo valor.
-- ---------------------------------------------------------------------
CREATE TABLE recebimento (
    id               uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    venda_id         uuid           NOT NULL REFERENCES venda (id) ON DELETE CASCADE,
    forma            varchar(20)    NOT NULL
        CHECK (forma IN ('DINHEIRO', 'PIX', 'CARTAO_DEBITO', 'CARTAO_CREDITO', 'OUTRO')),
    valor            numeric(15,2)  NOT NULL CHECK (valor > 0),
    valor_entregue   numeric(15,2),
    parcelas_cartao  integer        NOT NULL DEFAULT 1 CHECK (parcelas_cartao BETWEEN 1 AND 12),
    taxa             numeric(5,2)   NOT NULL DEFAULT 0 CHECK (taxa >= 0),
    recebido_em      timestamptz    NOT NULL DEFAULT now(),
    criado_em        timestamptz    NOT NULL DEFAULT now(),
    atualizado_em    timestamptz    NOT NULL DEFAULT now(),

    -- Troco só existe em dinheiro, e o cliente não pode entregar menos que o valor.
    CHECK (valor_entregue IS NULL OR (forma = 'DINHEIRO' AND valor_entregue >= valor)),
    -- Parcelar só no cartão de crédito.
    CHECK (parcelas_cartao = 1 OR forma = 'CARTAO_CREDITO')
);

CREATE INDEX ix_recebimento_venda ON recebimento (venda_id);


-- ---------------------------------------------------------------------
-- devolucao: o cliente trouxe algo de volta. Pode ser parcial (1 de 3
-- itens), por isso tem tabela de itens própria.
--
-- Como o cliente é reembolsado:
--   DINHEIRO:         sai da gaveta (exige sessão de caixa)
--   PIX:              o comerciante devolve por Pix
--   ABATIMENTO_FIADO: desconta do que ele deve (gera baixa_parcela, V6)
--   CREDITO_CLIENTE:  vira crédito para usar depois (lancamento_credito, V6)
-- ---------------------------------------------------------------------
CREATE TABLE devolucao (
    id                 uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id           uuid          NOT NULL REFERENCES conta (id)        ON DELETE RESTRICT,
    venda_id           uuid          NOT NULL REFERENCES venda (id)        ON DELETE RESTRICT,
    registrada_por_id  uuid          NOT NULL REFERENCES usuario (id)      ON DELETE RESTRICT,
    sessao_caixa_id    uuid          REFERENCES sessao_caixa (id)          ON DELETE RESTRICT,
    forma_reembolso    varchar(20)   NOT NULL
        CHECK (forma_reembolso IN ('DINHEIRO', 'PIX', 'ABATIMENTO_FIADO', 'CREDITO_CLIENTE')),
    motivo             varchar(255)  NOT NULL,
    registrada_em      timestamptz   NOT NULL DEFAULT now(),
    criado_em          timestamptz   NOT NULL DEFAULT now(),
    atualizado_em      timestamptz   NOT NULL DEFAULT now(),

    CHECK (forma_reembolso <> 'DINHEIRO' OR sessao_caixa_id IS NOT NULL)
);

CREATE INDEX ix_devolucao_venda  ON devolucao (venda_id);
CREATE INDEX ix_devolucao_sessao ON devolucao (sessao_caixa_id);


-- ---------------------------------------------------------------------
-- item_devolvido: quais itens voltaram e quanto de cada.
--
-- item_vendavel_id é obrigatório: valor avulso não tem o que devolver ao
-- estoque. retorna_ao_estoque = false quando o produto voltou estragado
-- (aí é perda, não volta para a prateleira).
--
-- Regra da APLICAÇÃO: não devolver mais do que foi vendido, somando todas
-- as devoluções daquele item_venda.
-- ---------------------------------------------------------------------
CREATE TABLE item_devolvido (
    id                  uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    devolucao_id        uuid           NOT NULL REFERENCES devolucao (id)     ON DELETE CASCADE,
    item_venda_id       uuid           NOT NULL REFERENCES item_venda (id)    ON DELETE RESTRICT,
    item_vendavel_id    uuid           NOT NULL REFERENCES item_vendavel (id) ON DELETE RESTRICT,
    quantidade          numeric(15,3)  NOT NULL CHECK (quantidade > 0),
    valor_unitario      numeric(15,2)  NOT NULL CHECK (valor_unitario >= 0),   -- snapshot do preço pago
    retorna_ao_estoque  boolean        NOT NULL DEFAULT true,
    criado_em           timestamptz    NOT NULL DEFAULT now(),
    atualizado_em       timestamptz    NOT NULL DEFAULT now()
);

CREATE INDEX ix_item_devolvido_devolucao  ON item_devolvido (devolucao_id);
CREATE INDEX ix_item_devolvido_item_venda ON item_devolvido (item_venda_id);
CREATE INDEX ix_item_devolvido_vendavel   ON item_devolvido (item_vendavel_id);
