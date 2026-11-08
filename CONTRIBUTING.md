# Como contribuir com o Ergane

Regras combinadas pelo grupo para manter o repositorio organizado.

## Commits

Seguimos o padrao [Conventional Commits](https://www.conventionalcommits.org/pt-br/):

```
tipo(escopo): descricao no imperativo
```

Exemplos:

```
feat(api): adiciona cadastro de cliente
fix(web): corrige calculo do troco na tela de venda
feat(db): cria tabelas de cadastros
docs: atualiza diagrama entidade-relacionamento
chore(api): configura projeto spring boot e postgresql via docker
```

### Tipos

| Tipo       | Quando usar                                                  |
| ---------- | ------------------------------------------------------------ |
| `feat`     | funcionalidade nova                                          |
| `fix`      | correcao de bug                                              |
| `docs`     | documentacao, README, diagramas                              |
| `refactor` | muda o codigo sem mudar o comportamento                      |
| `test`     | adiciona ou ajusta testes                                    |
| `chore`    | configuracao, infraestrutura, tarefas que nao mudam o produto |
| `build`    | dependencias e build (`pom.xml`, `package.json`)             |

### Escopos

| Escopo  | Parte do projeto                  |
| ------- | --------------------------------- |
| `api`   | back-end Spring (`apps/api`)      |
| `web`   | front-end                         |
| `db`    | migrations do banco               |
| `infra` | Docker e configuracao de ambiente |

O escopo eh opcional quando o commit mexe no projeto inteiro (ex.: `docs: ...`).

### Regras da descricao

- Em portugues, no imperativo: "adiciona", "corrige", "remove" (complete a frase "este commit...").
- Comeca com letra miniscula e nao termina com ponto.
- Curta: ate uns 70 caracteres. Se precisar explicar mais, use o corpo do commit.

## Branches

Por enquanto todos fazem commit direto na `main`. Antes de subir:

1. `git pull` para pegar o que os outros subiram.
2. Garanta que a aplicacao sobe sem erro.
3. `git push origin main`.

## ADRs (decisoes arquiteturais)

As decisoes arquiteturais ficam em `docs/adr/` e sao gerenciadas com o [Log4brains](https://github.com/thomvaill/log4brains). Na raiz do repositorio, rode `npm install` uma vez e depois:

| Comando               | O que faz                                          |
| --------------------- | -------------------------------------------------- |
| `npm run adr:new`     | cria uma ADR nova a partir de `docs/adr/template.md` |
| `npm run adr:list`    | lista as ADRs e seus status                        |
| `npm run adr:preview` | site local em http://localhost:4004                |
| `npm run adr:build`   | gera o site estatico em `.log4brains/out`          |

Para criar uma ADR:

1. Rode `npm run adr:new` e informe o titulo. O arquivo e criado como `docs/adr/AAAAMMDD-titulo.md`.
2. Preencha o arquivo (em portugues, com as secoes do template) e ajuste `Status` (ex.: `accepted`) e `Date` (`AAAA-MM-DD`).
3. Acrescente a linha da nova ADR na tabela de `docs/adr/README.md` (o indice e manual).
4. Mantenha uma linha em branco antes de qualquer lista, senao ela nao renderiza no site.

Mais detalhes em `docs/adr/README.md`.
