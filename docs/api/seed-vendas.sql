-- Vendas de exemplo para testar o UC11 (Consultar histórico e detalhes de vendas)
-- enquanto o registro de vendas (UC09) não existe na API. Uso exclusivo do banco local.
--
-- Pré-requisito: rodar as pastas "0. Sessão" e "2. Produtos" da collection do Postman.
-- Uso, na raiz do repositório, com o email que está no environment do Postman:
--   Git Bash / Linux / Mac:
--     docker compose exec -T db psql -U ergane -d ergane -v email=EMAIL < docs/api/seed-vendas.sql
--   PowerShell:
--     Get-Content docs/api/seed-vendas.sql | docker compose exec -T db psql -U ergane -d ergane -v email=EMAIL
--
-- Cria na conta desse usuário, usando o produto ativo mais antigo da conta:
--   1. venda detalhada: 20/11/2026 10:00, PIX, CONFIRMADA, origem 'balcao', 2 unidades do produto
--   2. resumo agregado: 20/11/2026 sem hora, sem itens, sem forma informada, CONFIRMADA, origem 'complemento'
--   3. venda cancelada: 10/11/2026 15:30, DINHEIRO, CANCELADA, origem 'balcao', sem itens
\set ON_ERROR_STOP on

SELECT count(*) = 0 AS usuario_nao_encontrado
FROM usuarios
WHERE lower(email) = lower(btrim(:'email')) \gset

\if :usuario_nao_encontrado
    \echo 'Erro! Nenhum usuário com esse email. Rode a pasta "0. Sessão" do Postman e use o email do environment.'
    \quit
\endif

SELECT u.id AS usuario_id, a.conta_id
FROM usuarios u
JOIN acessos_negocio a ON a.usuario_id = u.id AND a.ativo
WHERE lower(u.email) = lower(btrim(:'email'))
ORDER BY a.criado_em
LIMIT 1 \gset

SELECT count(*) > 0 AS ja_tem_vendas FROM vendas WHERE conta_id = :'conta_id' \gset

\if :ja_tem_vendas
    \echo 'Erro! Essa conta já possui vendas; o seed não foi aplicado de novo. Para recomeçar, rode a pasta "0. Sessão" (cria outra conta).'
    \quit
\endif

SELECT count(*) = 0 AS sem_produto
FROM produtos
WHERE conta_id = :'conta_id' AND arquivado_em IS NULL \gset

\if :sem_produto
    \echo 'Erro! A conta não tem produto ativo. Rode a pasta "2. Produtos" do Postman antes.'
    \quit
\endif

SELECT id AS produto_id
FROM produtos
WHERE conta_id = :'conta_id' AND arquivado_em IS NULL
ORDER BY criado_em, id
LIMIT 1 \gset

BEGIN;

-- 1. Venda detalhada: itens guardam nome, diferenciador, preço e custo do momento da venda
WITH venda AS (
    INSERT INTO vendas (conta_id, autor_id, ocorrencia_data, ocorrencia_hora, ocorrencia_fuso,
                        situacao, origem, total_registrado, forma_pagamento_informada)
    SELECT :'conta_id', :'usuario_id', DATE '2026-11-20', TIME '10:00', 'America/Sao_Paulo',
           'CONFIRMADA', 'balcao', 2 * p.preco_venda, 'PIX'
    FROM produtos p
    WHERE p.id = :'produto_id'
    RETURNING id
)
INSERT INTO itens_venda (venda_id, produto_id, nome_snapshot, diferenciador_snapshot,
                         quantidade, preco_unitario_snapshot, custo_unitario_snapshot)
SELECT venda.id, p.id, p.nome, p.diferenciador, 2, p.preco_venda, p.custo_unitario
FROM venda, produtos p
WHERE p.id = :'produto_id';

-- 2. Resumo agregado: só data, sem itens e sem forma informada
INSERT INTO vendas (conta_id, autor_id, ocorrencia_data, situacao, origem, total_registrado)
VALUES (:'conta_id', :'usuario_id', DATE '2026-11-20', 'CONFIRMADA', 'complemento', 50.00);

-- 3. Venda cancelada
INSERT INTO vendas (conta_id, autor_id, ocorrencia_data, ocorrencia_hora, ocorrencia_fuso,
                    situacao, origem, total_registrado, forma_pagamento_informada)
VALUES (:'conta_id', :'usuario_id', DATE '2026-11-10', TIME '15:30', 'America/Sao_Paulo',
        'CANCELADA', 'balcao', 12.00, 'DINHEIRO');

COMMIT;

\echo 'Vendas de exemplo criadas. Agora rode a pasta "3. Vendas" do Postman.'
