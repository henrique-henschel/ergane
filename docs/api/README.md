# Documentação e testes manuais da API

Duas formas de conhecer e testar os endpoints da API (`apps/api`):

| | O que é | Onde |
|---|---|---|
| **Swagger UI** | Documentação gerada do próprio código: rotas, parâmetros, corpos, validações e respostas. Permite chamar a API pelo navegador. | http://localhost:8080/swagger-ui.html |
| **Collection do Postman** | Fluxo completo com testes automáticos de cada cenário (sessão, categorias, produtos e vendas). | `ergane-api.postman_collection.json` + `ergane-local.postman_environment.json` |

A documentação (Swagger e `/v3/api-docs`) só fica ligada no perfil `local`. Nos demais perfis ela não existe.

## 1. Subir a API com login funcionando

O login gera tokens JWT assinados com um par de chaves RSA, informado pelas variáveis `ERGANE_SECURITY_JWT_*`. Sem elas, `POST /api/sessoes` responde 503. Para o ambiente local, use o script, que gera um par de chaves só de desenvolvimento e sobe a API:

```bash
docker compose up -d          # na raiz: sobe o banco
cd apps/api
./scripts/rodar-local.sh      # no Windows, rode pelo Git Bash
```

- As chaves ficam em `apps/api/.jwt-local/`, que é ignorada pelo git e pelo Docker. Nunca versione chaves.
- O script não altera a configuração de segurança. Se você já define as variáveis `ERGANE_SECURITY_JWT_*` de outro jeito, os valores do terminal têm prioridade.
- Para usar outra porta: `PORT=8081 ./scripts/rodar-local.sh`.

## 2. Swagger UI

Com a API no ar, abra http://localhost:8080/swagger-ui.html.

1. `POST /api/cadastros` → crie um usuário (o `nomeConta` cria o negócio). Guarde o `contaId`.
2. `POST /api/sessoes` → entre e copie o `accessToken`.
3. Clique em **Authorize** e cole o `accessToken`.
4. `PUT /api/sessoes/atual/conta` com `{"contaId": "..."}`.
5. A partir daí, todas as rotas de categorias, produtos e vendas funcionam.

A especificação OpenAPI em JSON fica em http://localhost:8080/v3/api-docs. Ela também pode ser importada no Postman, Insomnia etc.

## 3. Postman

1. No Postman: **Import** → selecione os dois arquivos `.json` desta pasta.
2. Selecione o environment **Ergane local**. Se a API estiver em outra porta, ajuste `baseUrl`.
3. Rode as pastas **em ordem**, pelo botão **Run** (Collection Runner) ou requisição a requisição:

| Pasta | O que faz |
|---|---|
| 0. Sessão (UC01) | Cria usuário e conta novos (email único a cada execução), entra e seleciona a conta. Salva tokens e IDs no environment. |
| 1. Categorias (UC07) | CRUD de categorias e suas validações. |
| 2. Produtos (UC03–UC06) | Cadastro, consulta/filtros, edição e arquivamento de produtos. |
| 3. Vendas (UC11) | Histórico e detalhe de vendas. **Precisa do seed** (abaixo). |
| 4. Sessão — renovar e encerrar | Renova os tokens e encerra a sessão. Rode por último. |

Cada requisição tem testes (aba **Tests**). O resultado aparece em **Test Results** ou no resumo do Runner.

Para recomeçar do zero, rode de novo a pasta 0: ela cria outra conta, vazia.

### Seed de vendas (antes da pasta 3)

O registro de vendas (UC09) ainda não existe na API. Por isso as vendas de exemplo entram direto no banco local. Depois das pastas 0 e 2, copie o `email` do environment e rode, **na raiz do repositório**:

```bash
# Git Bash / Linux / Mac
docker compose exec -T db psql -U ergane -d ergane -v email=EMAIL_DO_ENVIRONMENT < docs/api/seed-vendas.sql
```

```powershell
# PowerShell
Get-Content docs/api/seed-vendas.sql | docker compose exec -T db psql -U ergane -d ergane -v email=EMAIL_DO_ENVIRONMENT
```

O seed cria 3 vendas na conta desse usuário: uma detalhada com itens, um resumo agregado sem itens e uma cancelada. Ele não roda duas vezes na mesma conta.

Quando o UC09 existir, troque o seed por requisições de registro de venda na própria collection.

## Mantendo atualizado

- **Swagger:** atualiza sozinho a partir do código (controllers, DTOs e anotações de validação).
- **Postman:** ao criar ou alterar endpoints, edite a collection no Postman e exporte de volta para esta pasta (**Export** → Collection v2.1), substituindo o arquivo.
