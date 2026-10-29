# Arquitetura e Componentes do Sistema

```mermaid
flowchart TB
    subgraph Client ["Camada Cliente (Frontend)"]
        direction TB
        n15["Usuário"] --> n14["Aplicação Cliente<br/>(Interface e Estado de Tela)"]
    end

    subgraph Transport ["Comunicação"]
        n14 ==>|"HTTPS / HTTP + JSON"| n13
    end

    subgraph Server ["Camada de Servidor (Backend - Spring Boot)"]
        direction TB
        n13["API REST (Spring Boot)"]
        
        subgraph AuthSec ["Segurança"]
            n8["Autenticação, Autorização<br/>e Isolamento por Conta"]
        end

        subgraph Modules ["Módulos de Domínio"]
            direction LR
            n7["Cadastros<br/>(Conta, Usuário, Loja, PDV)"]
            n6["Catálogo<br/>(Item Vendável, Categoria)"]
            n5["Vendas<br/>(Venda, Itens, Recebimentos)"]
            n4["Estoque<br/>(Saldo e Movimentos)"]
            n3["Caixa<br/>(Sessão, Movimentos, Despesas)"]
        end

        subgraph Shared ["Componentes compartilhados"]
            n1["Componentes Comuns<br/>(Dinheiro, Qtd, Percentual, Datas)"]
        end

        subgraph Persistence ["Persistência"]
            n2["Camada de Persistência<br/>(Repositórios e ORM)"]
        end

        n13 --> n8
        n13 --> n7
        n13 --> n6
        n13 --> n5
        n13 --> n4
        n13 --> n3

        n3 --> n1
        n4 --> n1
        n5 --> n1
        n6 --> n1
        n7 --> n1

        n3 --> n2
        n4 --> n2
        n5 --> n2
        n6 --> n2
        n7 --> n2
        n8 --> n2

        n5 --> n4
    end

    subgraph Offline ["Camada Offline-First (Cliente / Local)"]
        direction TB
        n11["Gravação / Consulta Local"] --> n10["Armazenamento Local<br/>& Fila de Operações"]
        n10 --> n9["Sincronizador de Operações<br/>(Background Sync)"]
        n9 -.->|"Reconexão / Envio de Fila"| n13
    end

    n14 -.->|"Operação Offline"| n11

    subgraph DB ["Banco de Dados"]
        n0[("PostgreSQL 17")]
    end

    n2 --> n0
```