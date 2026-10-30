-- Negocio fixo usado até o UC01 (login) existir
INSERT INTO negocios (id, nome)
VALUES ('00000000-0000-0000-0000-000000000001', 'Negocio de Teste')
ON CONFLICT (id) DO NOTHING;
