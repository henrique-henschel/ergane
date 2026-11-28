-- Venda e ItemVenda conforme o diagrama de classes 06 (UC09–UC13)
CREATE TABLE vendas (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id                  UUID NOT NULL REFERENCES contas (id) ON DELETE RESTRICT,
    autor_id                  UUID NOT NULL REFERENCES usuarios (id) ON DELETE RESTRICT,
    ocorrencia_data           DATE NOT NULL,
    ocorrencia_hora           TIME,
    ocorrencia_fuso           VARCHAR(64),
    registrada_em             TIMESTAMPTZ NOT NULL DEFAULT now(),
    situacao                  VARCHAR(20) NOT NULL
        CHECK (situacao IN ('CONFIRMADA', 'CANCELADA', 'DESFEITA')),
    origem                    TEXT NOT NULL,
    total_registrado          NUMERIC(12, 2) NOT NULL CHECK (total_registrado >= 0),
    forma_pagamento_informada VARCHAR(20)
        CHECK (forma_pagamento_informada IN ('DINHEIRO', 'PIX', 'CARTAO_DEBITO', 'CARTAO_CREDITO')),
    encomenda_origem_id       UUID, -- sem FK até a tabela de encomendas existir (UC14)
    versao                    BIGINT NOT NULL DEFAULT 0
);

COMMENT ON COLUMN vendas.ocorrencia_hora IS
    'Nulo quando só a data é conhecida: não vira meia-noite fictícia (RT02).';

COMMENT ON COLUMN vendas.forma_pagamento_informada IS
    'Nulo = forma não informada; não presumir dinheiro (UC11).';

-- Mesma ordem da listagem do histórico (UC11)
CREATE INDEX ix_vendas_conta_ocorrencia
    ON vendas (conta_id, ocorrencia_data DESC, ocorrencia_hora DESC NULLS LAST, id DESC);

CREATE TABLE itens_venda (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    venda_id                UUID NOT NULL REFERENCES vendas (id) ON DELETE RESTRICT,
    produto_id              UUID NOT NULL REFERENCES produtos (id) ON DELETE RESTRICT,
    nome_snapshot           VARCHAR(100) NOT NULL CHECK (TRIM(nome_snapshot) <> ''),
    diferenciador_snapshot  VARCHAR(50),
    quantidade              NUMERIC(15, 3) NOT NULL CHECK (quantidade > 0),
    preco_unitario_snapshot NUMERIC(12, 2) NOT NULL CHECK (preco_unitario_snapshot >= 0),
    custo_unitario_snapshot NUMERIC(12, 2) CHECK (custo_unitario_snapshot >= 0)
);

-- Detalhe da venda (itens em ordem de id)
CREATE INDEX ix_itens_venda_venda
    ON itens_venda (venda_id, id);

-- Filtro do histórico por produto
CREATE INDEX ix_itens_venda_produto
    ON itens_venda (produto_id, venda_id);
