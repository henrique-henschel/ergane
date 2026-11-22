CREATE TABLE vendas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    negocio_id UUID NOT NULL REFERENCES negocios (id) ON DELETE RESTRICT,
    registrada_em TIMESTAMPTZ,
    data_ocorrencia DATE,
    total NUMERIC(15, 2) NOT NULL CHECK (total >= 0),
    origem TEXT,
    situacao TEXT NOT NULL
        CHECK (situacao IN ('CONFIRMADA', 'CANCELADA', 'DESFEITA')),

    CONSTRAINT ck_vendas_uma_ocorrencia
        CHECK (
            (registrada_em IS NOT NULL)
            <>
            (data_ocorrencia IS NOT NULL)
        )
);

COMMENT ON COLUMN vendas.registrada_em IS
    'Instante da ocorrência quando o horário é conhecido.';

COMMENT ON COLUMN vendas.data_ocorrencia IS
    'Data civil da ocorrência quando não há horário conhecido.';

CREATE INDEX idx_vendas_negocio_registrada_id
    ON vendas (negocio_id, registrada_em DESC, id DESC)
    WHERE registrada_em IS NOT NULL;

CREATE INDEX idx_vendas_negocio_data_id
    ON vendas (negocio_id, data_ocorrencia DESC, id DESC)
    WHERE data_ocorrencia IS NOT NULL;


CREATE TABLE itens_venda (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    venda_id UUID NOT NULL REFERENCES vendas (id) ON DELETE RESTRICT,
    item_vendavel_id UUID,
    descricao_snapshot TEXT NOT NULL,
    unidade_snapshot VARCHAR(40) NOT NULL,
    quantidade NUMERIC(15, 3) NOT NULL CHECK (quantidade > 0),
    preco_unitario NUMERIC(15, 2) NOT NULL CHECK (preco_unitario >= 0),
    custo_unitario NUMERIC(15, 2)
        CHECK (custo_unitario IS NULL OR custo_unitario >= 0)
);

-- Sem FK até a tabela do catálogo existir no schema.
CREATE INDEX idx_itens_venda_item_vendavel_venda
    ON itens_venda (item_vendavel_id, venda_id)
    WHERE item_vendavel_id IS NOT NULL;

CREATE INDEX idx_itens_venda_venda
    ON itens_venda (venda_id, id);


CREATE TABLE recebimentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    venda_id UUID NOT NULL REFERENCES vendas (id) ON DELETE RESTRICT,
    forma TEXT NOT NULL
        CHECK (forma IN (
            'DINHEIRO',
            'PIX',
            'CARTAO_DEBITO',
            'CARTAO_CREDITO'
        )),
    valor NUMERIC(15, 2) NOT NULL CHECK (valor > 0),
    recebido_em TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_recebimentos_venda_forma
    ON recebimentos (venda_id, forma);
