# UC01 — Contrato REST

As rotas protegidas recebem `Authorization: Bearer <accessToken>`. Erros da API usam `ProblemDetail`. Os exemplos nesta página descrevem o contrato.

| Método e rota | Entrada | Sucesso |
| --- | --- | --- |
| `POST /api/sessoes` | `email`, `senha` | `201` com credenciais e prazos da sessão |
| `POST /api/sessoes/renovacoes` | `refreshToken` | `200` com credenciais rotacionadas e prazos da sessão |
| `GET /api/sessoes` | Sem corpo | `200` com as sessões ativas do usuário |
| `POST /api/sessoes/atual/revogar` | Sem corpo | `200` com a confirmação de revogação |
| `POST /api/sessoes/{id}/revogar` | `senha` | `200` com a confirmação de revogação |
| `POST /api/sessoes/revogar-todas` | `senha` | `200` com o resultado da revogação |
| `GET /api/negocios` | Sem corpo | `200` com os vínculos ativos do usuário, ordenados por nome e ID |
| `GET /api/sessoes/atual/negocio` | Sem corpo | `200` com `negocioId`, que pode ser `null` |
| `PUT /api/sessoes/atual/negocio` | `negocioId` | `200` com o `negocioId` autorizado |
| `POST /api/negocios/{negocioId}/acessos/{acessoId}/revogar` | `senha` | `204` sem corpo |

A listagem independe da seleção atual. Um usuário sem vínculos ativos recebe `[]`. Cada negócio tem a seguinte forma:

```json
{
  "negocioId": "b0a70000-0000-4000-8000-000000000001",
  "nome": "Padaria Central",
  "papel": "PROPRIETARIO"
}
```

O `PUT` registra a seleção na sessão atual e a preferência na conta. No próximo login, a API restaura a preferência se o vínculo estiver ativo. Cada sessão aberta conserva sua própria seleção. Se o vínculo da seleção tiver sido revogado, o `GET` devolve `{"negocioId":null}`.

Na revogação de vínculo, proprietários podem revogar administradores e funcionários. Administradores podem revogar funcionários. O vínculo informado deve pertencer ao negócio da rota. A revogação impede novos acessos ao negócio e preserva a sessão para outros vínculos ativos. Autenticação inválida ou confirmação de senha incorreta pode gerar `401`. O bloqueio temporário por tentativas retorna `429` e `Retry-After`. Um negócio ou vínculo fora da autorização retorna `404`.
