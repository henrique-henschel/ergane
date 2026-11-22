ALTER TABLE usuarios
    ADD COLUMN ultimo_negocio_id UUID;

ALTER TABLE usuarios
    ADD CONSTRAINT fk_usuarios_ultimo_negocio
        FOREIGN KEY (ultimo_negocio_id)
        REFERENCES negocios (id) ON DELETE SET NULL;
