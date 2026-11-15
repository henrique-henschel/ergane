# Ergane

**Gestão simples para quem faz o próprio negócio acontecer.**

A **Ergane** é uma plataforma de gestão voltada a pequenos negócios, microempreendedores e vendedores independentes.

Nossa proposta é simplificar a organização de vendas, encomendas, produtos e informações financeiras, reunindo o essencial para a gestão de um negócio em uma experiência acessível, intuitiva e prática.

> 🚧 **Status: Em desenvolvimento**
>
> Atualmente, a Ergane funciona em **ambiente local de desenvolvimento**. Em breve, a plataforma será disponibilizada em um ambiente online, permitindo o acesso aos seus recursos sem a necessidade de instalação e configuração local.

---

## Sobre o projeto

Administrar um pequeno negócio exige muito mais do que vender. É preciso acompanhar pedidos, organizar produtos, registrar valores e compreender os resultados.

Muitos pequenos empreendedores realizam essas atividades manualmente, utilizando cadernos, planilhas ou ferramentas que nem sempre atendem às suas necessidades.

A Ergane nasce para tornar essa gestão mais simples.

**Nosso objetivo é permitir que empreendedores tenham maior controle sobre seus negócios sem precisar dominar ferramentas complexas.**

### Funcionalidades previstas

- **Gestão de vendas:** registro rápido de vendas, produtos, quantidades e valores.
- **Gestão de produtos:** organização e consulta dos produtos comercializados.
- **Gestão de encomendas:** acompanhamento de pedidos, prazos e entregas.
- **Controle financeiro:** registro de valores recebidos e pendentes.
- **Histórico de operações:** consulta de vendas e movimentações registradas.
- **Indicadores de desempenho:** visualização de informações relevantes para o negócio.
- **Análise inteligente:** identificação de padrões e geração de informações preditivas a partir dos dados disponíveis.
- **Notificações:** alertas relacionados a operações e informações importantes.

> As funcionalidades estão sujeitas ao andamento do desenvolvimento. Algumas ainda podem estar em fase de planejamento ou implementação.

---

## Design e prototipação

A interface da Ergane é desenvolvida com foco em simplicidade, acessibilidade e fluidez.

Buscamos construir uma experiência intuitiva, capaz de reduzir a quantidade de interações necessárias para realizar as principais operações.

## Tecnologias utilizadas

| Camada               | Tecnologias                |
| -------------------- | -------------------------- |
| Backend              | Java 21, Spring Boot 4.1.2 |
| API                  | REST                       |
| Persistência         | Spring Data JPA            |
| Banco de dados       | PostgreSQL 17              |
| Migrações            | Flyway                     |
| Mobile               | React Native, Expo         |
| Linguagem mobile     | JavaScript                 |
| Infraestrutura local | Docker, Docker Compose     |
| Build backend        | Maven                      |
| Versionamento        | Git, GitHub                |

A arquitetura está organizada em um único repositório, permitindo centralizar diferentes aplicações e seus recursos compartilhados.

---

## Estrutura do repositório

```text
ergane/
│
├── apps/
│   ├── api/                # Backend Spring Boot
│   ├── mobile/             # Aplicativo React Native
│   └── web/                # Aplicação web (reservada)
│
├── contracts/
│   └── openapi/            # Contratos da API
│
├── docs/
│   ├── adr/                # Decisões arquiteturais
│   ├── design/             # Documentação de design
│   ├── modelagem/          # Modelagem do sistema
│   ├── requisitos/         # Requisitos e casos de uso
│   ├── specs/              # Especificações técnicas
│   └── docker.md           # Guia de utilização do Docker
│
├── infra/                  # Infraestrutura
├── scripts/                # Scripts auxiliares
│
├── docker-compose.yml      # Configuração do PostgreSQL
├── package.json            # Ferramentas de documentação (Log4brains para as ADRs)
├── CONTRIBUTING.md         # Convenções internas
└── README.md
```

---

## Como executar o projeto

Atualmente, os serviços da Ergane são executados localmente. Para configurar o ambiente de desenvolvimento, siga as instruções abaixo.

### Pré-requisitos

Antes de iniciar, instale:

- Git
- Java JDK 21
- Docker e Docker Compose
- Node.js e npm
- Uma IDE de sua preferência

O backend utiliza Maven Wrapper, portanto não é necessário instalar o Maven separadamente.

### 1. Clonar o repositório

```bash
git clone https://github.com/henrique-henschel/ergane.git
cd ergane
```

### 2. Inicializar o banco de dados

Na raiz do projeto:

```bash
docker compose up -d
```

Verifique se o PostgreSQL está em execução:

```bash
docker compose ps
```

O Docker Compose atual inicializa apenas o banco de dados.

#### Configuração local do banco

| Propriedade | Valor     |
| ----------- | --------- |
| Host        | localhost |
| Porta       | 5432      |
| Database    | ergane    |
| Username    | ergane    |
| Password    | ergane    |

**Atenção:** essas credenciais pertencem ao ambiente de desenvolvimento. Não devem ser utilizadas em produção.

### 3. Executar o backend

Entre no diretório da API:

```bash
cd apps/api
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
./mvnw spring-boot:run
```

A API utiliza, por padrão:

```text
http://localhost:8080
```

O Flyway é responsável pelo gerenciamento das migrações do banco de dados.

> Um retorno HTTP 404 ao acessar a raiz da API não significa necessariamente que a aplicação esteja indisponível. Pode não existir um endpoint configurado nesse caminho.

### 4. Executar o aplicativo mobile

Abra outro terminal e acesse, a partir da raiz do projeto:

```bash
cd apps/mobile
```

Instale as dependências:

```bash
npm ci
```

Inicie o Expo:

```bash
npx expo start
```

Outros comandos disponíveis:

```bash
npm run android
npm run ios
npm run web
```

A execução em dispositivos físicos ou emuladores pode exigir configurações adicionais de rede para comunicação com a API.

### 5. Encerrar os serviços

Para interromper a API e o Expo, utilize `Ctrl + C` nos respectivos terminais.

Para parar o banco de dados, execute na raiz do projeto:

```bash
docker compose down
```

Esse comando mantém os dados armazenados no volume do PostgreSQL.

> **Atenção:** executar `docker compose down -v` também remove o volume do banco de dados, ocasionando a perda dos dados armazenados localmente.

---

## Testes

Para executar os testes do backend, acesse:

```bash
cd apps/api
```

No Windows:

```powershell
.\mvnw.cmd test
```

No Linux ou macOS:

```bash
./mvnw test
```

A cobertura de testes será ampliada conforme o desenvolvimento das funcionalidades.

---

## Documentação

Os documentos técnicos estão disponíveis no próprio repositório.

| Documento                                                       | Descrição                             |
| --------------------------------------------------------------- | ------------------------------------- |
| [Histórias de usuário](docs/requisitos/historias-de-usuario.md) | Necessidades e objetivos dos usuários |
| [Casos de uso](docs/requisitos/casos-de-uso.md)                 | Fluxos e comportamentos do sistema    |
| [Regras de negócio](docs/requisitos/regras-de-negocio.md)       | Regras que orientam as operações      |
| [Documentação da API](apps/api/README.md)                       | Instalação e execução do backend      |
| [Guia do Docker](docs/docker.md)                                | Comandos e manutenção do banco local  |
| [Decisões arquiteturais](docs/adr/)                             | Registro de decisões técnicas         |

A documentação orienta a implementação das funcionalidades e ajuda a manter a consistência entre as interfaces, a API e o banco de dados.

---

## Arquitetura

A Ergane é organizada em aplicações independentes, que compartilham contratos e regras de negócio.

A API centraliza o processamento das operações e a comunicação com o banco de dados, enquanto as interfaces são responsáveis pela interação com os usuários.

### Visão geral

```text
          Aplicativo Mobile
            React Native
                 |
                 v
             API REST
           Spring Boot
                 |
                 v
          Spring Data JPA
                 |
                 v
            PostgreSQL
```

O projeto também reserva espaço para uma aplicação web e para a evolução da infraestrutura de implantação.

### Persistência de dados

O PostgreSQL é utilizado como banco de dados relacional.

As alterações estruturais no banco são gerenciadas pelo Flyway, permitindo manter o histórico das migrações e a consistência entre os ambientes.

---

## Segurança e boas práticas

A Ergane lida com informações operacionais e financeiras dos negócios cadastrados. Por isso, segurança, privacidade e integridade dos dados são aspectos essenciais do desenvolvimento.

Entre os principais cuidados previstos estão:

- Autenticação e autorização de usuários.
- Isolamento dos dados entre diferentes contas.
- Validação das informações recebidas pela API.
- Proteção de credenciais e dados sensíveis.
- Versionamento das alterações no banco de dados.
- Estratégias de backup e recuperação.
- Testes automatizados.
- Monitoramento e tratamento adequado de erros.

Esses itens representam requisitos e diretrizes de desenvolvimento, não necessariamente funcionalidades já implementadas.

---

## Estado atual e próximos passos

A Ergane encontra-se em desenvolvimento ativo, com seus componentes sendo implementados e integrados progressivamente.

Atualmente, o projeto conta com:

- Estrutura inicial da API Spring Boot.
- Banco de dados PostgreSQL configurado para execução local.
- Aplicativo React Native com Expo.
- Documentação de requisitos e casos de uso.
- Estrutura de versionamento das migrações do banco.
- Organização inicial dos componentes e contratos.

### Próximas etapas

O desenvolvimento continuará com a implementação das funcionalidades de gestão, integração entre os componentes, validação dos fluxos e evolução da infraestrutura.

A disponibilização online da Ergane está prevista para uma etapa futura, após os ajustes necessários de segurança, configuração e implantação.

---

## Repositório

**GitHub:** https://github.com/henrique-henschel/ergane

---
