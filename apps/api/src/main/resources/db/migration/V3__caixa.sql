-- =====================================================================
-- V3 · CAIXA
-- O dinheiro em espécie da gaveta. Vem antes das vendas porque a venda
-- (V4) aponta para a sessão de caixa em que foi registrada.
--
-- Pix e cartão NÃO passam por aqui: não entram na gaveta. O caixa só
-- controla o dinheiro que dá para contar com a mão.
-- =====================================================================


-- ---------------------------------------------------------------------
-- sessao_caixa: "abrir o caixa" de manhã e "fechar" à noite.
--
-- No fechamento o operador conta as notas (saldo_contado) e o sistema
-- calcula quanto deveria haver (saldo_esperado = inicial + entradas -
-- saídas). A diferença entre os dois é a "quebra de caixa".
-- O esperado é congelado ao fechar, para o relatório não mudar se alguém
-- mexer em algo depois.
-- ---------------------------------------------------------------------
CREATE TABLE sessao_caixa (
    id                            uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id                      uuid           NOT NULL REFERENCES conta (id)       ON DELETE RESTRICT,
    ponto_venda_id                uuid           NOT NULL REFERENCES ponto_venda (id) ON DELETE RESTRICT,
    operador_id                   uuid           NOT NULL REFERENCES usuario (id)     ON DELETE RESTRICT,
    data_referencia               date           NOT NULL,   -- o "dia" no fuso da conta (caixa aberto às 23h conta para aquele dia)
    saldo_inicial                 numeric(15,2)  NOT NULL CHECK (saldo_inicial >= 0),   -- o troco
    saldo_contado                 numeric(15,2)  CHECK (saldo_contado >= 0),
    saldo_esperado_no_fechamento  numeric(15,2),
    observacao_fechamento         varchar(500),
    aberta_em                     timestamptz    NOT NULL DEFAULT now(),
    fechada_em                    timestamptz,                -- NULL = aberta (ou reaberta)
    versao                        bigint         NOT NULL DEFAULT 0,
    criado_em                     timestamptz    NOT NULL DEFAULT now(),
    atualizado_em                 timestamptz    NOT NULL DEFAULT now(),

    CHECK (fechada_em IS NULL OR fechada_em >= aberta_em),
    -- Fechou? Então contou e o sistema calculou o esperado.
    CHECK (fechada_em IS NULL OR (saldo_contado IS NOT NULL AND saldo_esperado_no_fechamento IS NOT NULL))
);

-- Regra: só UMA sessão aberta por ponto de venda.
-- É um índice único PARCIAL: só vale para as linhas com fechada_em NULL.
-- Sessões fechadas podem existir aos montes; abertas, uma só.
CREATE UNIQUE INDEX ux_sessao_caixa_aberta ON sessao_caixa (ponto_venda_id) WHERE fechada_em IS NULL;

CREATE INDEX ix_sessao_caixa_operador ON sessao_caixa (operador_id);
CREATE INDEX ix_sessao_caixa_conta_dia ON sessao_caixa (conta_id, data_referencia);


-- ---------------------------------------------------------------------
-- movimento_caixa: o extrato da gaveta. Cada entrada ou saída de dinheiro
-- vira uma linha. O saldo esperado é a soma deste extrato.
--
-- valor é sempre positivo; o tipo diz o sentido:
--   entram: VENDA, RECEBIMENTO_FIADO, SUPRIMENTO (pôr troco extra)
--   saem:   DESPESA, SANGRIA (tirar para levar ao banco), ESTORNO, REEMBOLSO
--
-- origem_tipo + origem_id: referência "polimórfica" ao que gerou o
-- movimento (uma venda, uma devolução...). Não tem FK porque uma FK só
-- aponta para uma tabela; quem garante a coerência é a aplicação.
-- ---------------------------------------------------------------------
CREATE TABLE movimento_caixa (
    id               uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    sessao_caixa_id  uuid           NOT NULL REFERENCES sessao_caixa (id) ON DELETE CASCADE,
    tipo             varchar(20)    NOT NULL
        CHECK (tipo IN ('VENDA', 'RECEBIMENTO_FIADO', 'DESPESA', 'SUPRIMENTO', 'SANGRIA', 'ESTORNO', 'REEMBOLSO')),
    valor            numeric(15,2)  NOT NULL CHECK (valor > 0),
    origem_tipo      varchar(20)    CHECK (origem_tipo IN ('VENDA', 'DEVOLUCAO', 'RECEBIMENTO_FIADO', 'DESPESA')),
    origem_id        uuid,
    descricao        varchar(255),
    ocorrido_em      timestamptz    NOT NULL DEFAULT now(),
    criado_em        timestamptz    NOT NULL DEFAULT now(),
    atualizado_em    timestamptz    NOT NULL DEFAULT now(),

    -- Os dois juntos ou nenhum (SUPRIMENTO e SANGRIA são lançados à mão, sem origem).
    CHECK ((origem_tipo IS NULL) = (origem_id IS NULL))
);

CREATE INDEX ix_movimento_caixa_sessao ON movimento_caixa (sessao_caixa_id);
CREATE INDEX ix_movimento_caixa_origem ON movimento_caixa (origem_tipo, origem_id);


-- ---------------------------------------------------------------------
-- despesa: qualquer gasto do negócio (gasolina, mercadoria, almoço).
-- Se foi pago com o dinheiro da gaveta, também gera um movimento_caixa
-- do tipo DESPESA (regra da aplicação).
-- ---------------------------------------------------------------------
CREATE TABLE despesa (
    id                 uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id           uuid           NOT NULL REFERENCES conta (id)        ON DELETE RESTRICT,
    loja_id            uuid           NOT NULL REFERENCES loja (id)         ON DELETE RESTRICT,
    sessao_caixa_id    uuid           REFERENCES sessao_caixa (id)          ON DELETE RESTRICT,
    registrada_por_id  uuid           NOT NULL REFERENCES usuario (id)      ON DELETE RESTRICT,
    descricao          varchar(255),                  -- vazia = a tela mostra o nome da categoria
    categoria          varchar(12)    NOT NULL
        CHECK (categoria IN ('MERCADORIA', 'TRANSPORTE', 'ALIMENTACAO', 'PONTO', 'TAXAS', 'OUTROS')),
    valor              numeric(15,2)  NOT NULL CHECK (valor > 0),
    forma              varchar(20)    NOT NULL CHECK (forma IN ('DINHEIRO_DA_GAVETA', 'OUTRA')),
    ocorrida_em        timestamptz    NOT NULL DEFAULT now(),
    criado_em          timestamptz    NOT NULL DEFAULT now(),
    atualizado_em      timestamptz    NOT NULL DEFAULT now(),

    -- Tirou da gaveta? Então precisa dizer de qual sessão de caixa saiu.
    CHECK (forma <> 'DINHEIRO_DA_GAVETA' OR sessao_caixa_id IS NOT NULL)
);

CREATE INDEX ix_despesa_conta_data ON despesa (conta_id, ocorrida_em);
CREATE INDEX ix_despesa_loja       ON despesa (loja_id);
CREATE INDEX ix_despesa_sessao     ON despesa (sessao_caixa_id);
