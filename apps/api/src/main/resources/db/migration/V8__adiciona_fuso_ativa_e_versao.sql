-- Conta: fusoHorario e ativa (diagrama de classes 03)
-- Fuso das contas já existentes (conta fixa de dev); novas contas devem informá-lo (D02)
ALTER TABLE contas ADD COLUMN fuso_horario VARCHAR(64) NOT NULL DEFAULT 'America/Sao_Paulo'
    CHECK (TRIM(fuso_horario) <> '');
ALTER TABLE contas ALTER COLUMN fuso_horario DROP DEFAULT;

ALTER TABLE contas ADD COLUMN ativa BOOLEAN NOT NULL DEFAULT TRUE;

-- Categoria: versao para bloqueio otimista (diagrama de classes 04, RT03)
ALTER TABLE categorias ADD COLUMN versao BIGINT NOT NULL DEFAULT 0;
