CREATE TABLE sessoes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    token_acesso_id UUID NOT NULL,
    refresh_token_hash VARCHAR(64) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL,
    expira_em TIMESTAMPTZ NOT NULL,
    limite_absoluto_em TIMESTAMPTZ NOT NULL,
    revogada_em TIMESTAMPTZ,
    CONSTRAINT fk_sessoes_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT uq_sessoes_token_acesso_id UNIQUE (token_acesso_id),
    CONSTRAINT uq_sessoes_refresh_token_hash UNIQUE (refresh_token_hash),
    CONSTRAINT ck_sessoes_refresh_token_hash
        CHECK (refresh_token_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_sessoes_prazos
        CHECK (criado_em < expira_em
            AND expira_em <= limite_absoluto_em),
    CONSTRAINT ck_sessoes_revogacao
        CHECK (revogada_em IS NULL OR revogada_em >= criado_em)
);

CREATE TABLE tentativas_login (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email_normalizado TEXT NOT NULL,
    falhas INTEGER NOT NULL DEFAULT 0,
    janela_inicio_em TIMESTAMPTZ NOT NULL,
    bloqueado_ate TIMESTAMPTZ,
    CONSTRAINT uq_tentativas_login_email
        UNIQUE (email_normalizado),
    CONSTRAINT ck_tentativas_login_email
        CHECK (email_normalizado <> ''
            AND email_normalizado = lower(btrim(email_normalizado))),
    CONSTRAINT ck_tentativas_login_falhas CHECK (falhas >= 0)
);
