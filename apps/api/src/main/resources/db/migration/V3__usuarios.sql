CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome TEXT NOT NULL,
    email TEXT NOT NULL,
    senha_hash TEXT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    email_verificado_em TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_usuarios_nome_preenchido CHECK (btrim(nome) <> ''),
    CONSTRAINT ck_usuarios_email_preenchido CHECK (btrim(email) <> ''),
    CONSTRAINT ck_usuarios_senha_hash_preenchida CHECK (btrim(senha_hash) <> '')
);

CREATE UNIQUE INDEX uq_usuarios_email ON usuarios (lower(btrim(email)));
