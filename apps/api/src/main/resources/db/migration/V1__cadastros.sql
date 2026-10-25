-- =====================================================================
-- V1 · CADASTROS
-- Quem usa o sistema (conta, usuarios, lojas) e para quem se vende (clientes).
--
-- Convencoes usadas em todas as migrations:
--  - id uuid: pode ser gerado no celular (evita venda duplicada quando a
--    internet cai e o app reenvia). O DEFAULT eh soh para quando nao vier.
--  - Sem "opcional" no DER  -> NOT NULL. Com "opcional" -> aceita NULL.
--  - Linha continua no DER  -> ON DELETE CASCADE  (filha morre com a mae).
--    Linha tracejada        -> ON DELETE RESTRICT (nao deixa apagar a mae).
--  - criado_em / atualizado_em: auditoria. O atualizado_em eh preenchido
--    pela aplicacao (@UpdateTimestamp no Hibernate), nao pelo banco.
--  - versao: bloqueio otimista (@Version no Hibernate).
--  - Um CHECK em coluna que aceita NULL soh vale quando ela esta preenchida.
--  - O PostgreSQL NAO cria indice sozinho para chave estrangeira, por isso
--    os CREATE INDEX no fim de cada bloco.
-- =====================================================================


-- ---------------------------------------------------------------------
-- conta: o negocio que assina o SaaS (o "tenant").
-- Quase toda tabela tem conta_id, e toda consulta filtra por ela, para um
-- comerciante nunca enxergar os dados de outro.
-- Nunca eh apagada: desativa-se com ativa = false (soft delete).
-- ---------------------------------------------------------------------
CREATE TABLE conta (
    id                      uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    nome                    varchar(120)  NOT NULL,
    nome_responsavel        varchar(120)  NOT NULL,
    whatsapp                varchar(13)   CHECK (whatsapp ~ '^[0-9]{10,13}$'),  -- soh digitos, com DDD
    fuso_horario            varchar(64)   NOT NULL DEFAULT 'America/Sao_Paulo', -- "vendas de hoje" depende dele
    boas_vindas_concluidas  boolean       NOT NULL DEFAULT false,               -- ja passou pela tela de boas-vindas
    ativa                   boolean       NOT NULL DEFAULT true,
    criada_em               timestamptz   NOT NULL DEFAULT now(),
    atualizado_em           timestamptz   NOT NULL DEFAULT now()
);


-- ---------------------------------------------------------------------
-- conta_ramo: o que o negocio vende (perguntado nas boas-vindas).
-- Tabela a parte porque uma conta pode ter varios ramos, e uma coluna nao
-- deve guardar uma lista (1a forma normal).
-- ---------------------------------------------------------------------
CREATE TABLE conta_ramo (
    conta_id  uuid         NOT NULL REFERENCES conta (id) ON DELETE CASCADE,
    ramo      varchar(60)  NOT NULL,
    PRIMARY KEY (conta_id, ramo)   -- chave composta: o mesmo ramo nao se repete na conta
);


-- ---------------------------------------------------------------------
-- configuracao_negocio: as preferencias do comerciante. Exatamente uma
-- por conta (o UNIQUE em conta_id garante o 1 para 1).
-- ---------------------------------------------------------------------
CREATE TABLE configuracao_negocio (
    id                          uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id                    uuid           NOT NULL UNIQUE REFERENCES conta (id) ON DELETE RESTRICT,
    vender_fiado                boolean        NOT NULL DEFAULT false,

    -- Taxas da maquininha, em %. Sao custo do comerciante: servem para o
    -- sistema mostrar o lucro de verdade, nao mudam o que o cliente paga.
    taxa_debito                 numeric(5,2)   NOT NULL DEFAULT 0 CHECK (taxa_debito BETWEEN 0 AND 99.99),
    taxa_credito_a_vista        numeric(5,2)   NOT NULL DEFAULT 0 CHECK (taxa_credito_a_vista BETWEEN 0 AND 99.99),
    taxa_credito_parcelado      numeric(5,2)   NOT NULL DEFAULT 0 CHECK (taxa_credito_parcelado BETWEEN 0 AND 99.99),

    chave_pix                   varchar(140),                                   -- para mostrar ao cliente na hora de pagar
    prazo_padrao_fiado_dias     integer        NOT NULL DEFAULT 30 CHECK (prazo_padrao_fiado_dias BETWEEN 1 AND 365),
    bloquear_venda_sem_estoque  boolean        NOT NULL DEFAULT false,         -- false = estoque pode ficar negativo
    troco_inicial_padrao        numeric(15,2)  NOT NULL DEFAULT 0 CHECK (troco_inicial_padrao >= 0),
    versao                      bigint         NOT NULL DEFAULT 0,
    criado_em                   timestamptz    NOT NULL DEFAULT now(),
    atualizado_em               timestamptz    NOT NULL DEFAULT now()
);


-- ---------------------------------------------------------------------
-- configuracao_forma_ativa: quais formas de pagamento aparecem na tela de
-- venda (ex.: quem nao tem maquininha desliga os cartoes).
-- ---------------------------------------------------------------------
CREATE TABLE configuracao_forma_ativa (
    configuracao_negocio_id  uuid         NOT NULL REFERENCES configuracao_negocio (id) ON DELETE CASCADE,
    forma                    varchar(20)  NOT NULL
        CHECK (forma IN ('DINHEIRO', 'PIX', 'CARTAO_DEBITO', 'CARTAO_CREDITO', 'OUTRO')),
    PRIMARY KEY (configuracao_negocio_id, forma)
);


-- ---------------------------------------------------------------------
-- loja: o lugar fisico onde se vende.
-- ---------------------------------------------------------------------
CREATE TABLE loja (
    id                    uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id              uuid          NOT NULL REFERENCES conta (id) ON DELETE RESTRICT,
    nome                  varchar(80)   NOT NULL,
    endereco_cep          char(8)       CHECK (endereco_cep ~ '^[0-9]{8}$'),
    endereco_logradouro   varchar(120),
    endereco_numero       varchar(10),                 -- varchar porque existe "S/N", "120-A"
    endereco_complemento  varchar(60),
    endereco_bairro       varchar(80),
    endereco_cidade       varchar(80),
    endereco_uf           char(2)       CHECK (endereco_uf ~ '^[A-Z]{2}$'),
    ativa                 boolean       NOT NULL DEFAULT true,
    criado_em             timestamptz   NOT NULL DEFAULT now(),
    atualizado_em         timestamptz   NOT NULL DEFAULT now(),
    UNIQUE (conta_id, nome)   -- nome unico dentro da conta (duas contas podem ter "Loja Centro")
);


-- ---------------------------------------------------------------------
-- ponto_venda: cada caixa/gaveta dentro da loja. O caixa (V5) eh aberto e
-- fechado por ponto de venda. Ambulante = 1 loja com 1 ponto.
-- ---------------------------------------------------------------------
CREATE TABLE ponto_venda (
    id             uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id       uuid         NOT NULL REFERENCES conta (id) ON DELETE RESTRICT,
    loja_id        uuid         NOT NULL REFERENCES loja (id)  ON DELETE RESTRICT,
    nome           varchar(60)  NOT NULL,
    ativo          boolean      NOT NULL DEFAULT true,
    criado_em      timestamptz  NOT NULL DEFAULT now(),
    atualizado_em  timestamptz  NOT NULL DEFAULT now(),
    UNIQUE (loja_id, nome)
);

CREATE INDEX ix_ponto_venda_conta ON ponto_venda (conta_id);


-- ---------------------------------------------------------------------
-- usuario: quem faz login. A senha NUNCA eh guardada, so o hash BCrypt
-- (Spring Security). percentual_maximo_desconto impede o vendedor de dar
-- mais desconto do que o dono permite.
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id                          uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id                    uuid           NOT NULL REFERENCES conta (id) ON DELETE RESTRICT,
    nome                        varchar(120)   NOT NULL,
    email                       varchar(254)   NOT NULL,
    senha_hash                  varchar(100)   NOT NULL,
    percentual_maximo_desconto  numeric(5,2)   NOT NULL DEFAULT 0
        CHECK (percentual_maximo_desconto BETWEEN 0 AND 100),
    ativo                       boolean        NOT NULL DEFAULT true,
    criado_em                   timestamptz    NOT NULL DEFAULT now(),
    atualizado_em               timestamptz    NOT NULL DEFAULT now()
);

-- E-mail eh o login, entao eh unico no sistema inteiro (nao so na conta).
-- lower() faz "Joao@x.com" e "joao@x.com" contarem como o mesmo e-mail.
CREATE UNIQUE INDEX ux_usuario_email ON usuario (lower(email));
CREATE INDEX ix_usuario_conta ON usuario (conta_id);


-- ---------------------------------------------------------------------
-- usuario_papel: o que o usuario pode fazer. Um usuario pode ter varios
-- papeis (o dono tambem opera o caixa, por exemplo).
-- ---------------------------------------------------------------------
CREATE TABLE usuario_papel (
    usuario_id  uuid         NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    papel       varchar(20)  NOT NULL
        CHECK (papel IN ('PROPRIETARIO', 'GERENTE', 'VENDEDOR', 'OPERADOR_CAIXA')),
    PRIMARY KEY (usuario_id, papel)
);


-- ---------------------------------------------------------------------
-- usuario_loja: em quais lojas o usuario pode trabalhar (N para N).
-- Apagar o usuario apaga as autorizacoes (CASCADE); apagar uma loja com
-- usuarios autorizados eh bloqueado (RESTRICT).
-- ---------------------------------------------------------------------
CREATE TABLE usuario_loja (
    usuario_id  uuid  NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    loja_id     uuid  NOT NULL REFERENCES loja (id)    ON DELETE RESTRICT,
    PRIMARY KEY (usuario_id, loja_id)
);

-- A PK ja serve para buscar "lojas do usuario"; este indice serve para o
-- caminho inverso, "usuarios da loja".
CREATE INDEX ix_usuario_loja_loja ON usuario_loja (loja_id);


-- ---------------------------------------------------------------------
-- cliente: para quem se vende. So o nome eh obrigatorio: ambulante nao pede
-- CPF para vender um salgado. O cliente so eh exigido na venda quando ha
-- fiado (regra da aplicacao, V3).
-- ---------------------------------------------------------------------
CREATE TABLE cliente (
    id                    uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id              uuid           NOT NULL REFERENCES conta (id) ON DELETE RESTRICT,
    nome                  varchar(120)   NOT NULL,
    telefone              varchar(13)    CHECK (telefone ~ '^[0-9]{10,13}$'),   -- WhatsApp, so digitos
    observacao            varchar(255),                                         -- apelido: "Ze da padaria"
    documento             varchar(14)    CHECK (documento ~ '^([0-9]{11}|[0-9]{14})$'),  -- CPF (11) ou CNPJ (14)

    endereco_cep          char(8)        CHECK (endereco_cep ~ '^[0-9]{8}$'),
    endereco_logradouro   varchar(120),
    endereco_numero       varchar(10),
    endereco_complemento  varchar(60),
    endereco_bairro       varchar(80),
    endereco_cidade       varchar(80),
    endereco_uf           char(2)        CHECK (endereco_uf ~ '^[A-Z]{2}$'),

    -- Regras do fiado (V6):
    limite_credito        numeric(15,2)  CHECK (limite_credito >= 0),   -- NULL = sem limite
    bloqueado_para_fiado  boolean        NOT NULL DEFAULT false,

    -- Quando a LOJA deve ao cliente (ele pagou a mais, devolveu algo).
    -- Eh a soma do extrato lancamento_credito (V6), guardada para ler rapido.
    saldo_credito         numeric(15,2)  NOT NULL DEFAULT 0 CHECK (saldo_credito >= 0),

    arquivado             boolean        NOT NULL DEFAULT false,  -- cliente com vendas nao some: eh arquivado
    versao                bigint         NOT NULL DEFAULT 0,
    criado_em             timestamptz    NOT NULL DEFAULT now(),
    atualizado_em         timestamptz    NOT NULL DEFAULT now(),

    -- CPF/CNPJ unico dentro da conta. No PostgreSQL, NULLs nao colidem
    -- entre si no UNIQUE, entao varios clientes sem documento sao aceitos.
    UNIQUE (conta_id, documento)
);

-- Busca de cliente pelo nome na tela de venda.
CREATE INDEX ix_cliente_conta_nome ON cliente (conta_id, nome);
