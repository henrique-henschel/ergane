# Comandos do Docker

Guia de consulta rápida dos comandos que usamos no Ergane.

O Docker roda o **PostgreSQL** do projeto dentro de um container. A receita do container está no `docker-compose.yml` da raiz do repositório.

> **Rode todos os comandos `docker compose` na raiz do repositório**, a pasta onde está o `docker-compose.yml`. Em outra pasta ele responde `no configuration file provided`.

Fora a parte de ligar o Docker, **os comandos são iguais no Windows (PowerShell), no Linux e no Mac**. Onde houver diferença, ela está indicada.

---

## 1. Ligar o Docker

### Windows e Mac

Abra o **Docker Desktop** e espere aparecer **"Engine running"** no canto inferior esquerdo. Sem ele aberto, todo comando dá erro (veja a seção 7).

### Linux

O Docker roda como um serviço do sistema:

```bash
sudo systemctl start docker     # liga agora
sudo systemctl enable docker    # liga sozinho quando o computador iniciar
```

Para não precisar de `sudo` em todo comando `docker`, adicione seu usuário ao grupo `docker` (só uma vez). Depois **saia da sessão e entre de novo**:

```bash
sudo usermod -aG docker $USER
```

### Conferir se está funcionando

```bash
docker --version
docker compose version
```

> Se `docker compose` não existir, mas `docker-compose` (com hífen) existir, você tem a versão antiga. Os comandos são os mesmos, só troque o espaço pelo hífen. O ideal é atualizar.

---

## 2. Dia a dia

| O que fazer | Comando |
|---|---|
| Subir o banco (em segundo plano) | `docker compose up -d` |
| Ver se está rodando | `docker compose ps` |
| Ver os logs do banco | `docker compose logs db` |
| Acompanhar os logs ao vivo (`Ctrl + C` para sair) | `docker compose logs -f db` |
| Parar o banco **mantendo os dados** | `docker compose down` |
| Reiniciar o banco | `docker compose restart db` |

O `-d` (*detached*) devolve o terminal. Sem ele, o terminal fica preso mostrando os logs, e o `Ctrl + C` derruba o banco.

---

## 3. Acessar o banco

### Console SQL interativo

```bash
docker compose exec db psql -U ergane
```

Dentro do console:

| Comando | O que faz |
|---|---|
| `\dt` | lista as tabelas |
| `\d venda` | mostra colunas, índices e constraints da tabela `venda` |
| `\x` | liga/desliga o modo "uma coluna por linha" (bom para tabelas largas) |
| `\q` | sai do console |

Todo comando SQL termina com `;`. Exemplo: `SELECT * FROM conta;`

### Comando único, sem entrar no console

```bash
docker compose exec db psql -U ergane -c "\dt"
docker compose exec db psql -U ergane -c "SELECT version, description, success FROM flyway_schema_history"
```

### Rodar um arquivo `.sql`

O jeito abaixo funciona igual em todos os sistemas: primeiro copia o arquivo para dentro do container, depois executa.

```bash
docker compose cp meu_script.sql db:/tmp/meu_script.sql
docker compose exec db psql -U ergane -f /tmp/meu_script.sql
```

> **Por que não usar `< meu_script.sql`?** No Linux funciona, mas no PowerShell o `<` dá erro (`The '<' operator is reserved for future use`). A cópia evita a diferença.

> **Não use isso para criar ou alterar tabelas.** Mudança de estrutura é sempre uma migration nova do Flyway (veja o `CONTRIBUTING.md`). Este comando é para testes e dados de exemplo.

---

## 4. Backup e restauração

### Gerar um backup

```bash
docker compose exec db pg_dump -U ergane -f /tmp/backup.sql
docker compose cp db:/tmp/backup.sql ./backup.sql
```

> **Por que não usar `pg_dump > backup.sql`?** No Windows PowerShell 5, o `>` grava o arquivo em UTF-16, e o PostgreSQL não consegue ler de volta. Gerar dentro do container e copiar para fora evita esse problema.

### Restaurar um backup

Restaure num banco vazio (veja a seção 5):

```bash
docker compose cp ./backup.sql db:/tmp/backup.sql
docker compose exec db psql -U ergane -f /tmp/backup.sql
```

> Não suba arquivos de backup para o repositório.

---

## 5. Apagar o banco e começar do zero

```bash
docker compose down -v
docker compose up -d
```

O `-v` apaga o **volume** `pgdata`, onde ficam os dados. **Tudo some**: tabelas, dados e o histórico do Flyway. Na próxima vez que a API subir, o Flyway recria todas as tabelas a partir das migrations.

Quando usar:
- o Spring reclama de `checksum mismatch` (alguém editou uma migration que já tinha rodado no seu banco);
- os dados de teste viraram bagunça e você quer recomeçar.

---

## 6. Liberar memória

Mesmo com o banco parado, o Docker Desktop continua ocupando memória.

### Windows

1. `docker compose down`
2. Clique com o botão direito no ícone da baleia (perto do relógio) → **Quit Docker Desktop**.
3. Para liberar também a máquina virtual Linux que o Docker usa:

```powershell
wsl --shutdown
```

### Mac

`docker compose down` e depois feche o Docker Desktop pelo ícone da baleia na barra de menu.

### Linux

O Docker Engine sem containers rodando gasta pouca memória. Se quiser desligar mesmo assim:

```bash
sudo systemctl stop docker docker.socket
```

### Ver o que está ocupando espaço

```bash
docker ps              # containers rodando agora (vazio = nada rodando)
docker system df       # espaço usado por imagens, containers e volumes
```

---

## 7. Problemas comuns

**Windows: `failed to connect to the docker API at npipe:////./pipe/dockerDesktopLinuxEngine`**
O Docker Desktop não está aberto. Abra e espere o "Engine running".

**Linux: `Cannot connect to the Docker daemon at unix:///var/run/docker.sock`**
O serviço não está rodando: `sudo systemctl start docker`.

**Linux: `permission denied while trying to connect to the Docker daemon socket`**
Seu usuário não está no grupo `docker`. Veja a seção 1 e lembre de sair e entrar de novo na sessão.

**`no configuration file provided: not found`**
Você não está na raiz do repositório. Vá para a pasta onde está o `docker-compose.yml`.

**`port is already allocated` / `bind: address already in use` na porta 5432**
Já existe outro PostgreSQL rodando no seu computador, instalado fora do Docker.
- Windows: `services.msc` → serviço `postgresql-...` → **Parar**.
- Linux: `sudo systemctl stop postgresql`.

Ou troque a porta só na sua máquina, sem subir essa mudança: no `docker-compose.yml` use `"5433:5432"` e no `application.properties` use `localhost:5433`.

**Windows: o Docker Desktop não abre ou reclama de WSL**
No PowerShell **como administrador**:

```powershell
wsl --update
```

Depois reinicie o computador.

**Os dados sumiram**
Alguém rodou `docker compose down -v`. Sem backup, os dados não voltam. As tabelas voltam sozinhas quando a API subir.
