ALTER TABLE categorias ADD CONSTRAINT uq_categorias_negocio_id UNIQUE (negocio_id, id);

CREATE TABLE produtos (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    negocio_id    UUID NOT NULL REFERENCES negocios (id) ON DELETE RESTRICT,
    categoria_id  UUID,
    nome          VARCHAR(100) NOT NULL CHECK (TRIM(nome) <> ''),
    diferenciador VARCHAR(50)  CHECK (diferenciador IS NULL OR TRIM(diferenciador) <> ''),
    descricao     VARCHAR(255),
    preco         NUMERIC(12, 2) NOT NULL CHECK (preco >= 0),
    custo         NUMERIC(12, 2) CHECK (custo >= 0),
    validade      DATE,
    arquivado_em  TIMESTAMPTZ,
    criado_em     TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_produtos_categoria
        FOREIGN KEY (negocio_id, categoria_id)
        REFERENCES categorias (negocio_id, id)
        ON DELETE RESTRICT
);

CREATE UNIQUE INDEX ux_produtos_nome_diferenciador
    ON produtos (negocio_id, LOWER(nome), LOWER(COALESCE(diferenciador, '')))
    WHERE arquivado_em IS NULL;

-- Acelera o filtro por categoria e a checagem "esta categoria tem produtos?"
CREATE INDEX ix_produtos_categoria
    ON produtos (negocio_id, categoria_id);
