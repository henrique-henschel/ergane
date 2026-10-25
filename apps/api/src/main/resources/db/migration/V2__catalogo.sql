-- =====================================================================
-- V2 · CATALOGO
-- O que o comerciante vende: produtos (tem estoque) e serviços (nao tem).
-- =====================================================================


-- ---------------------------------------------------------------------
-- categoria: agrupa os itens na tela de venda ("Bebidas", "Salgados").
-- Eh opcional: da para vender sem organizar nada.
-- ---------------------------------------------------------------------
CREATE TABLE categoria (
    id             uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id       uuid         NOT NULL REFERENCES conta (id) ON DELETE RESTRICT,
    nome           varchar(60)  NOT NULL,
    emoji          varchar(16),                 -- varchar(16): alguns emojis ocupam varios caracteres
    criado_em      timestamptz  NOT NULL DEFAULT now(),
    atualizado_em  timestamptz  NOT NULL DEFAULT now(),
    UNIQUE (conta_id, nome)
);


-- ---------------------------------------------------------------------
-- item_vendavel: cada coisa que aparece na tela de venda.
-- Emoji, cor e foto existem porque o publico nao eh tecnico e vende com
-- pressa no celular: botoes grandes e coloridos, nao uma lista de codigos.
-- Item com vendas nao eh apagado: desativa-se com ativo = false.
-- ---------------------------------------------------------------------
CREATE TABLE item_vendavel (
    id                uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id          uuid           NOT NULL REFERENCES conta (id) ON DELETE RESTRICT,
    categoria_id      uuid           REFERENCES categoria (id) ON DELETE RESTRICT,   -- opcional
    codigo            varchar(60),                -- codigo de barras ou interno, opcional
    descricao         varchar(120)   NOT NULL,
    tipo              varchar(10)    NOT NULL CHECK (tipo IN ('PRODUTO', 'SERVICO')),
    unidade           varchar(12)    NOT NULL DEFAULT 'UNIDADE'
        CHECK (unidade IN ('UNIDADE', 'QUILOGRAMA', 'GRAMA', 'LITRO', 'PACOTE', 'CAIXA', 'DUZIA')),
    preco_venda       numeric(15,2)  NOT NULL CHECK (preco_venda >= 0),
    preco_custo       numeric(15,2)  CHECK (preco_custo >= 0),        -- NULL = "nao sei"; com ele, o sistema mostra o lucro
    controla_estoque  boolean        NOT NULL DEFAULT false,
    emoji             varchar(16),
    foto_url          varchar(500),
    cor               varchar(10)    NOT NULL DEFAULT 'CINZA'
        CHECK (cor IN ('VERDE', 'AZUL', 'AMARELO', 'LARANJA', 'VERMELHO', 'ROSA', 'ROXO', 'CINZA')),
    ativo             boolean        NOT NULL DEFAULT true,
    criado_em         timestamptz    NOT NULL DEFAULT now(),
    atualizado_em     timestamptz    NOT NULL DEFAULT now(),

    -- Codigo unico na conta quando preenchido (NULLs nao colidem no UNIQUE).
    UNIQUE (conta_id, codigo),

    -- Servico nao tem estoque: nao da para "ter 5 cortes de cabelo na prateleira".
    CHECK (tipo = 'PRODUTO' OR controla_estoque = false)
);

CREATE INDEX ix_item_vendavel_categoria       ON item_vendavel (categoria_id);
CREATE INDEX ix_item_vendavel_conta_descricao ON item_vendavel (conta_id, descricao);  -- busca na tela de venda
