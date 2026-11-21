-- Alinha o tenant ao diagrama de classes (Conta / contaId)
ALTER TABLE negocios RENAME TO contas;
ALTER TABLE contas RENAME CONSTRAINT negocios_pkey TO contas_pkey;
ALTER TABLE contas RENAME CONSTRAINT negocios_nome_check TO contas_nome_check;

ALTER TABLE categorias RENAME COLUMN negocio_id TO conta_id;
ALTER TABLE categorias RENAME CONSTRAINT categorias_negocio_id_fkey TO categorias_conta_id_fkey;
ALTER INDEX idx_categorias_negocio_nome RENAME TO idx_categorias_conta_nome;
