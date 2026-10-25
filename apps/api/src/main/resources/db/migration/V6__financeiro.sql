-- =====================================================================
-- V6 · FINANCEIRO (FIADO)
-- O "caderninho": quem levou e vai pagar depois. É o diferencial do
-- produto, porque é o que o pequeno comerciante mais perde no papel.
--
-- Como as tabelas conversam:
--   venda com fiado --gera--> conta_receber --divide-se em--> parcela
--   cliente paga    --gera--> recebimento_fiado --abate--> baixa_parcela(s)
--   pagou a mais    --gera--> lancamento_credito (crédito do cliente)
--
-- "Atrasado", "quitada" e "quanto falta" de cada parcela NÃO são colunas:
-- saem de vencimento < hoje e da soma das baixas. Assim nunca ficam
-- desatualizados.
-- =====================================================================


-- ---------------------------------------------------------------------
-- conta_receber: a dívida gerada por UMA venda (venda_id é UNIQUE).
-- valor_total = o que ficou faltando pagar na venda.
-- A situação QUITADA é atualizada pela aplicação quando as baixas somam
-- o total.
-- ---------------------------------------------------------------------
CREATE TABLE conta_receber (
    id             uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id       uuid           NOT NULL REFERENCES conta (id)    ON DELETE RESTRICT,
    cliente_id     uuid           NOT NULL REFERENCES cliente (id)  ON DELETE RESTRICT,
    venda_id       uuid           NOT NULL UNIQUE REFERENCES venda (id) ON DELETE RESTRICT,
    valor_total    numeric(15,2)  NOT NULL CHECK (valor_total > 0),
    situacao       varchar(10)    NOT NULL DEFAULT 'ABERTA' CHECK (situacao IN ('ABERTA', 'QUITADA', 'CANCELADA')),
    criada_em      timestamptz    NOT NULL DEFAULT now(),
    cancelada_em   timestamptz,
    versao         bigint         NOT NULL DEFAULT 0,
    atualizado_em  timestamptz    NOT NULL DEFAULT now(),

    CHECK ((situacao = 'CANCELADA') = (cancelada_em IS NOT NULL))
);

CREATE INDEX ix_conta_receber_cliente        ON conta_receber (cliente_id);
CREATE INDEX ix_conta_receber_conta_situacao ON conta_receber (conta_id, situacao);   -- "quem está devendo?"


-- ---------------------------------------------------------------------
-- parcela: a dívida dividida em vencimentos. Fiado simples = 1 parcela
-- vencendo em configuracao_negocio.prazo_padrao_fiado_dias.
-- Regra da APLICAÇÃO: a soma das parcelas = conta_receber.valor_total.
-- ---------------------------------------------------------------------
CREATE TABLE parcela (
    id                uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_receber_id  uuid           NOT NULL REFERENCES conta_receber (id) ON DELETE CASCADE,
    numero            integer        NOT NULL CHECK (numero >= 1),
    vencimento        date           NOT NULL,
    valor             numeric(15,2)  NOT NULL CHECK (valor > 0),
    criado_em         timestamptz    NOT NULL DEFAULT now(),
    atualizado_em     timestamptz    NOT NULL DEFAULT now(),

    UNIQUE (conta_receber_id, numero)
);

CREATE INDEX ix_parcela_vencimento ON parcela (vencimento);   -- "quais estão atrasadas?"


-- ---------------------------------------------------------------------
-- recebimento_fiado: o cliente chega e diz "toma R$ 50 do que eu devo".
-- A aplicação distribui esse valor entre as parcelas em aberto (das mais
-- antigas para as mais novas), gerando uma baixa_parcela para cada uma.
--
-- valor:          dinheiro NOVO que o cliente entregou.
-- credito_usado:  quanto do crédito que ele já tinha foi usado junto.
-- credito_gerado: se pagou mais do que devia, o excesso vira crédito.
--
-- ATENÇÃO, diferença do DER: lá valor é "> 0". Aqui aceita 0 desde que
-- haja crédito usado, para permitir quitar uma dívida só com o crédito
-- que o cliente já tinha.
-- ---------------------------------------------------------------------
CREATE TABLE recebimento_fiado (
    id                 uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id           uuid           NOT NULL REFERENCES conta (id)        ON DELETE RESTRICT,
    cliente_id         uuid           NOT NULL REFERENCES cliente (id)      ON DELETE RESTRICT,
    registrado_por_id  uuid           NOT NULL REFERENCES usuario (id)      ON DELETE RESTRICT,
    sessao_caixa_id    uuid           REFERENCES sessao_caixa (id)          ON DELETE RESTRICT,
    forma              varchar(20)    NOT NULL
        CHECK (forma IN ('DINHEIRO', 'PIX', 'CARTAO_DEBITO', 'CARTAO_CREDITO', 'OUTRO')),
    valor              numeric(15,2)  NOT NULL CHECK (valor >= 0),
    credito_usado      numeric(15,2)  NOT NULL DEFAULT 0 CHECK (credito_usado >= 0),
    credito_gerado     numeric(15,2)  NOT NULL DEFAULT 0 CHECK (credito_gerado >= 0),
    recebido_em        timestamptz    NOT NULL DEFAULT now(),
    desfeito_em        timestamptz,                         -- registrou errado e desfez
    criado_em          timestamptz    NOT NULL DEFAULT now(),
    atualizado_em      timestamptz    NOT NULL DEFAULT now(),

    CHECK (valor > 0 OR credito_usado > 0),                 -- tem que abater alguma coisa
    CHECK (sessao_caixa_id IS NULL OR forma = 'DINHEIRO')   -- só dinheiro vai para a gaveta
);

CREATE INDEX ix_recebimento_fiado_cliente ON recebimento_fiado (cliente_id, recebido_em);
CREATE INDEX ix_recebimento_fiado_sessao  ON recebimento_fiado (sessao_caixa_id);


-- ---------------------------------------------------------------------
-- baixa_parcela: cada abatimento numa parcela. Permite pagamento parcial:
-- deve R$ 80, pagou R$ 30 -> uma baixa de R$ 30, faltam R$ 50.
--
-- Três jeitos de abater:
--   PAGAMENTO:            dinheiro novo (vem de um recebimento_fiado)
--   USO_CREDITO:          crédito que o cliente já tinha
--   ABATIMENTO_DEVOLUCAO: devolveu produto e descontou da dívida
-- ---------------------------------------------------------------------
CREATE TABLE baixa_parcela (
    id                    uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    parcela_id            uuid           NOT NULL REFERENCES parcela (id)          ON DELETE CASCADE,
    tipo                  varchar(20)    NOT NULL
        CHECK (tipo IN ('PAGAMENTO', 'USO_CREDITO', 'ABATIMENTO_DEVOLUCAO')),
    valor                 numeric(15,2)  NOT NULL CHECK (valor > 0),
    forma                 varchar(20)
        CHECK (forma IN ('DINHEIRO', 'PIX', 'CARTAO_DEBITO', 'CARTAO_CREDITO', 'OUTRO')),
    sessao_caixa_id       uuid           REFERENCES sessao_caixa (id)      ON DELETE RESTRICT,
    recebimento_fiado_id  uuid           REFERENCES recebimento_fiado (id) ON DELETE RESTRICT,
    devolucao_id          uuid           REFERENCES devolucao (id)         ON DELETE RESTRICT,
    registrada_em         timestamptz    NOT NULL DEFAULT now(),
    criado_em             timestamptz    NOT NULL DEFAULT now(),
    atualizado_em         timestamptz    NOT NULL DEFAULT now(),

    -- Forma de pagamento: só (e sempre) em PAGAMENTO.
    CHECK ((tipo = 'PAGAMENTO') = (forma IS NOT NULL)),
    -- Gaveta: só em pagamento em dinheiro.
    CHECK (sessao_caixa_id IS NULL OR (tipo = 'PAGAMENTO' AND forma = 'DINHEIRO')),
    -- Recebimento de fiado: só em PAGAMENTO e USO_CREDITO.
    CHECK (recebimento_fiado_id IS NULL OR tipo IN ('PAGAMENTO', 'USO_CREDITO')),
    -- Devolução: só (e sempre) em ABATIMENTO_DEVOLUCAO.
    CHECK ((tipo = 'ABATIMENTO_DEVOLUCAO') = (devolucao_id IS NOT NULL))
);

CREATE INDEX ix_baixa_parcela_parcela     ON baixa_parcela (parcela_id);
CREATE INDEX ix_baixa_parcela_sessao      ON baixa_parcela (sessao_caixa_id);
CREATE INDEX ix_baixa_parcela_recebimento ON baixa_parcela (recebimento_fiado_id);
CREATE INDEX ix_baixa_parcela_devolucao   ON baixa_parcela (devolucao_id);


-- ---------------------------------------------------------------------
-- lancamento_credito: o extrato do crédito do cliente (quando a LOJA deve
-- a ele). cliente.saldo_credito (V1) é a soma deste extrato.
--
-- valor sempre positivo; o tipo diz o sentido:
--   entram: EXCEDENTE_RECEBIMENTO (pagou a mais), CANCELAMENTO_VENDA,
--           DEVOLUCAO
--   saem:   USO_EM_RECEBIMENTO (usou para pagar fiado), ESTORNO
-- ---------------------------------------------------------------------
CREATE TABLE lancamento_credito (
    id             uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    conta_id       uuid           NOT NULL REFERENCES conta (id)   ON DELETE RESTRICT,
    cliente_id     uuid           NOT NULL REFERENCES cliente (id) ON DELETE RESTRICT,
    tipo           varchar(25)    NOT NULL
        CHECK (tipo IN ('EXCEDENTE_RECEBIMENTO', 'CANCELAMENTO_VENDA', 'DEVOLUCAO', 'USO_EM_RECEBIMENTO', 'ESTORNO')),
    valor          numeric(15,2)  NOT NULL CHECK (valor > 0),
    origem_tipo    varchar(20)    NOT NULL CHECK (origem_tipo IN ('VENDA', 'RECEBIMENTO_FIADO', 'DEVOLUCAO')),
    origem_id      uuid           NOT NULL,                     -- polimórfica, sem FK
    ocorrido_em    timestamptz    NOT NULL DEFAULT now(),
    criado_em      timestamptz    NOT NULL DEFAULT now(),
    atualizado_em  timestamptz    NOT NULL DEFAULT now()
);

CREATE INDEX ix_lancamento_credito_cliente ON lancamento_credito (cliente_id, ocorrido_em);
CREATE INDEX ix_lancamento_credito_origem  ON lancamento_credito (origem_tipo, origem_id);
