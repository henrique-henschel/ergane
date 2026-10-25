# Ergane API

Back-end do Ergane: API REST em **Java 21 + Spring Boot**, com banco **PostgreSQL 17** rodando em Docker e esquema versionado com **Flyway**.

```
front-end  ──►  API Spring (porta 8080)  ──►  PostgreSQL (porta 5432, no Docker)
```

## Pré-requisitos

Instale uma vez só:

| Ferramenta | Versão | Como conferir | Como instalar (Windows) |
|---|---|---|---|
| Git | qualquer | `git --version` | `winget install Git.Git` |
| JDK | 21 ou mais nova | `java -version` | `winget install EclipseAdoptium.Temurin.21.JDK` |
| Docker Desktop | qualquer | `docker --version` | `winget install Docker.DockerDesktop` |

O **Maven não precisa ser instalado**: o projeto traz o Maven Wrapper (`mvnw.cmd` no Windows, `mvnw` no Linux/Mac), que baixa a versão certa sozinho.

No Windows, o Docker Desktop usa o **WSL2**. Se ele reclamar de WSL na primeira vez, rode `wsl --update` no PowerShell como administrador e reinicie o computador.

## Subindo o projeto

### 1. Clonar o repositório

```bash
git clone <url-do-repositorio>
cd <pasta-do-repositorio>
```

### 2. Subir o banco

Abra o **Docker Desktop** e espere aparecer **"Engine running"**. Depois, **na raiz do repositório**:

```bash
docker compose up -d
```

Na primeira vez ele baixa a imagem do PostgreSQL, então demora um pouco. O `-d` deixa o banco rodando em segundo plano e devolve o terminal.

Para conferir se está no ar:

```bash
docker compose ps
```

O serviço `db` deve aparecer com status `running`.

### 3. Subir a API

Ainda no terminal, entre na pasta da API:

```bash
cd apps/api
```

Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

Linux / Mac:

```bash
./mvnw spring-boot:run
```

Está pronto quando o log terminar com algo como:

```
Tomcat started on port 8080
Started ApiApplication in ... seconds
```

Ao subir, o Flyway cria ou atualiza as tabelas sozinho. Não é preciso rodar nenhum `.sql` na mão.

Abrindo http://localhost:8080 no navegador aparece uma página de erro 404 ("Whitelabel Error Page"). Isso é normal: significa que a API está respondendo, só não existe nada na raiz.

## Parando

| Para parar | Comando |
|---|---|
| A API | `Ctrl + C` no terminal onde ela está rodando |
| O banco (os dados continuam guardados) | `docker compose down` na raiz |
| O banco **e apagar todos os dados** | `docker compose down -v` na raiz |

## Dados de conexão do banco (só desenvolvimento)

| | |
|---|---|
| Host | `localhost` |
| Porta | `5432` |
| Banco | `ergane` |
| Usuário | `ergane` |
| Senha | `ergane` |

Para ver as tabelas com interface gráfica, use o [DBeaver](https://dbeaver.io/) (gratuito) com esses dados.

Pelo terminal, sem instalar nada:

```bash
docker compose exec db psql -U ergane          # abre o console SQL (\q para sair)
docker compose exec db psql -U ergane -c "\dt" # lista as tabelas
```

## Problemas comuns

**`failed to connect to the docker API` / `dockerDesktopLinuxEngine`**
O Docker Desktop não está aberto. Abra e espere o "Engine running".

**`port is already allocated` / porta 5432 em uso**
Você tem outro PostgreSQL instalado direto no computador. Pare o serviço dele (Windows: `services.msc` → postgresql → Parar) ou troque no `docker-compose.yml` para `"5433:5432"` e no `application.properties` a URL para `localhost:5433`. Não suba essa troca para o repositório.

**`Failed to configure a DataSource` / `Connection refused`**
A API subiu antes do banco. Rode o `docker compose up -d` e tente de novo.

**`Validate failed: Migrations have failed validation` / `checksum mismatch`**
Alguém editou uma migration que já tinha rodado no seu banco. Migration não se edita (veja o `CONTRIBUTING.md`). Para destravar sua máquina, apague o banco local e deixe o Flyway recriar:

```bash
docker compose down -v
docker compose up -d
```

**`Schema-validation: missing table` / `missing column`**
Existe uma entidade Java sem a tabela ou coluna correspondente. Falta criar a migration.

**`'mvnw.cmd' não é reconhecido`**
Você não está em `apps/api`. Rode `cd apps/api` antes.

**`release version 21 not supported`**
O seu Java é mais antigo que o 21. Instale o JDK 21 (tabela de pré-requisitos).
