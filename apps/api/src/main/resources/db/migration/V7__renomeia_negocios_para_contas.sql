-- Alinha o tenant ao diagrama de classes (Conta / contaId)
ALTER TABLE negocios RENAME TO contas;
ALTER TABLE contas RENAME CONSTRAINT negocios_pkey TO contas_pkey;
ALTER TABLE contas RENAME CONSTRAINT negocios_nome_check TO contas_nome_check;

ALTER TABLE categorias RENAME COLUMN negocio_id TO conta_id;
ALTER TABLE categorias RENAME CONSTRAINT categorias_negocio_id_fkey TO categorias_conta_id_fkey;
ALTER INDEX idx_categorias_negocio_nome RENAME TO idx_categorias_conta_nome;

ALTER TABLE acessos_negocio RENAME COLUMN negocio_id TO conta_id;
ALTER TABLE acessos_negocio RENAME CONSTRAINT fk_acessos_negocio_negocio TO fk_acessos_negocio_conta;
ALTER TABLE acessos_negocio RENAME CONSTRAINT uq_acessos_negocio_usuario_negocio TO uq_acessos_negocio_usuario_conta;

ALTER TABLE sessoes RENAME COLUMN negocio_ativo_id TO conta_ativa_id;
ALTER TABLE sessoes RENAME CONSTRAINT fk_sessoes_negocio_ativo TO fk_sessoes_conta_ativa;
