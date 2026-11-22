ALTER TABLE categorias ADD CONSTRAINT uq_categorias_conta_id UNIQUE (conta_id, id);

CREATE TABLE produtos (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id       UUID NOT NULL REFERENCES contas (id) ON DELETE RESTRICT,
    categoria_id   UUID,
    nome           VARCHAR(100) NOT NULL CHECK (TRIM(nome) <> ''),
    diferenciador  VARCHAR(50)  CHECK (diferenciador IS NULL OR TRIM(diferenciador) <> ''),
    descricao      VARCHAR(255),
    preco_venda    NUMERIC(12, 2) NOT NULL CHECK (preco_venda >= 0),
    custo_unitario NUMERIC(12, 2) CHECK (custo_unitario >= 0),
    validade       DATE,
    arquivado_em   TIMESTAMPTZ,
    versao         BIGINT NOT NULL DEFAULT 0,
    criado_por_id  UUID NOT NULL REFERENCES usuarios (id) ON DELETE RESTRICT,
    criado_em      TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_produtos_categoria
        FOREIGN KEY (conta_id, categoria_id)
        REFERENCES categorias (conta_id, id)
        ON DELETE RESTRICT
);

CREATE UNIQUE INDEX ux_produtos_nome_diferenciador
    ON produtos (conta_id, LOWER(nome), LOWER(COALESCE(diferenciador, '')))
    WHERE arquivado_em IS NULL;

-- Acelera o filtro por categoria e a checagem "esta categoria tem produtos?"
CREATE INDEX ix_produtos_categoria
    ON produtos (conta_id, categoria_id);
