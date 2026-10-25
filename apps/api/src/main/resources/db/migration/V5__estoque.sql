-- =====================================================================
-- V5 · ESTOQUE
-- Quanto tem de cada produto em cada loja.
--
-- Padrão "foto + filme":
--   estoque           = a FOTO: o saldo atual, para ler rápido.
--   movimento_estoque = o FILME: cada entrada e saída, como um extrato
--                       bancário. Se o saldo parecer errado, o extrato
--                       mostra de onde veio cada unidade.
-- A aplicação SEMPRE grava os dois juntos, na mesma transação.
-- =====================================================================


-- ---------------------------------------------------------------------
-- estoque: um saldo por item por loja.
-- Só existe linha para itens com controla_estoque = true.
--
-- quantidade PODE ficar negativa: o ambulante muitas vezes vende antes de
-- lançar a mercadoria que comprou. Quem não quiser isso liga
-- configuracao_negocio.bloquear_venda_sem_estoque (regra da aplicação).
--
-- "Estoque baixo" não é coluna: é calculado (quantidade <= quantidade_minima).
-- ---------------------------------------------------------------------
CREATE TABLE estoque (
    id                 uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id           uuid           NOT NULL REFERENCES conta (id)         ON DELETE RESTRICT,
    loja_id            uuid           NOT NULL REFERENCES loja (id)          ON DELETE RESTRICT,
    item_vendavel_id   uuid           NOT NULL REFERENCES item_vendavel (id) ON DELETE RESTRICT,
    quantidade         numeric(15,3)  NOT NULL DEFAULT 0,      -- sem CHECK: pode ser negativa
    quantidade_minima  numeric(15,3)  NOT NULL DEFAULT 0 CHECK (quantidade_minima >= 0),
    versao             bigint         NOT NULL DEFAULT 0,      -- dois vendedores baixando o mesmo item ao mesmo tempo
    criado_em          timestamptz    NOT NULL DEFAULT now(),
    atualizado_em      timestamptz    NOT NULL DEFAULT now(),

    UNIQUE (loja_id, item_vendavel_id)
);

CREATE INDEX ix_estoque_item_vendavel ON estoque (item_vendavel_id);
CREATE INDEX ix_estoque_conta         ON estoque (conta_id);


-- ---------------------------------------------------------------------
-- movimento_estoque: o extrato.
--
-- quantidade é SEMPRE positiva; o tipo (ENTRADA/SAIDA) diz o sinal.
-- saldo_apos guarda o saldo logo depois do movimento (como o "saldo" na
-- última coluna do extrato do banco).
--
-- Custo: numa ENTRADA de COMPRA, o custo pago pode atualizar o
-- item_vendavel.preco_custo (custo médio). custo_anterior guarda o custo
-- que havia antes, para conseguir desfazer a entrada se lançaram errado.
-- ---------------------------------------------------------------------
CREATE TABLE movimento_estoque (
    id              uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id        uuid           NOT NULL REFERENCES conta (id)    ON DELETE RESTRICT,
    estoque_id      uuid           NOT NULL REFERENCES estoque (id)  ON DELETE RESTRICT,
    tipo            varchar(7)     NOT NULL CHECK (tipo IN ('ENTRADA', 'SAIDA')),
    motivo          varchar(20)    NOT NULL
        CHECK (motivo IN ('COMPRA', 'VENDA', 'CANCELAMENTO_VENDA', 'DEVOLUCAO', 'AJUSTE', 'PERDA', 'TRANSFERENCIA')),
    motivo_perda    varchar(10)    CHECK (motivo_perda IN ('VENCIDO', 'QUEBRADO', 'OUTRO')),
    quantidade      numeric(15,3)  NOT NULL CHECK (quantidade > 0),
    saldo_apos      numeric(15,3)  NOT NULL,
    custo_unitario  numeric(15,2)  CHECK (custo_unitario >= 0),
    custo_anterior  numeric(15,2)  CHECK (custo_anterior >= 0),
    origem_tipo     varchar(10)    CHECK (origem_tipo IN ('VENDA', 'DEVOLUCAO')),
    origem_id       uuid,                                          -- polimórfica, sem FK
    usuario_id      uuid           NOT NULL REFERENCES usuario (id) ON DELETE RESTRICT,
    observacao      varchar(500),
    ocorrido_em     timestamptz    NOT NULL DEFAULT now(),
    criado_em       timestamptz    NOT NULL DEFAULT now(),
    atualizado_em   timestamptz    NOT NULL DEFAULT now(),

    -- Cada motivo tem um sentido fixo; AJUSTE e TRANSFERENCIA podem ser os dois
    -- (ajuste para mais ou para menos; transferência sai de uma loja e entra em outra).
    CHECK (
        (motivo IN ('COMPRA', 'CANCELAMENTO_VENDA', 'DEVOLUCAO') AND tipo = 'ENTRADA') OR
        (motivo IN ('VENDA', 'PERDA')                            AND tipo = 'SAIDA')   OR
        (motivo IN ('AJUSTE', 'TRANSFERENCIA'))
    ),
    -- Motivo da perda: obrigatório em PERDA e proibido nos outros.
    CHECK ((motivo = 'PERDA') = (motivo_perda IS NOT NULL)),
    -- Custo só faz sentido em entrada.
    CHECK (tipo = 'ENTRADA' OR (custo_unitario IS NULL AND custo_anterior IS NULL)),
    -- Origem: os dois juntos ou nenhum.
    CHECK ((origem_tipo IS NULL) = (origem_id IS NULL))
);

CREATE INDEX ix_movimento_estoque_estoque_data ON movimento_estoque (estoque_id, ocorrido_em);
CREATE INDEX ix_movimento_estoque_origem       ON movimento_estoque (origem_tipo, origem_id);
CREATE INDEX ix_movimento_estoque_usuario      ON movimento_estoque (usuario_id);
