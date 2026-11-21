CREATE TABLE acessos_negocio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    negocio_id UUID NOT NULL,
    papel VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_acessos_negocio_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_acessos_negocio_negocio
        FOREIGN KEY (negocio_id)
        REFERENCES negocios (id) ON DELETE RESTRICT,
    CONSTRAINT uq_acessos_negocio_usuario_negocio
        UNIQUE (usuario_id, negocio_id),
    CONSTRAINT ck_acessos_negocio_papel
        CHECK (papel IN (
            'PROPRIETARIO', 'ADMINISTRADOR', 'FUNCIONARIO'
        )),
    CONSTRAINT ck_acessos_negocio_proprietario_ativo
        CHECK (papel <> 'PROPRIETARIO' OR ativo)
);

CREATE UNIQUE INDEX uq_acessos_negocio_proprietario
    ON acessos_negocio (negocio_id)
    WHERE papel = 'PROPRIETARIO';
