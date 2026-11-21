ALTER TABLE sessoes
    ADD COLUMN negocio_ativo_id UUID;

ALTER TABLE sessoes
    ADD CONSTRAINT fk_sessoes_negocio_ativo
        FOREIGN KEY (negocio_ativo_id)
        REFERENCES negocios (id) ON DELETE RESTRICT;
