CREATE TABLE negocios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL CHECK (TRIM(nome) <> ''),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE categorias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    negocio_id UUID NOT NULL REFERENCES negocios (id) ON DELETE RESTRICT,
    nome VARCHAR(100) NOT NULL CHECK (TRIM(nome) <> ''),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_categorias_negocio_nome ON categorias (negocio_id, LOWER(nome));
