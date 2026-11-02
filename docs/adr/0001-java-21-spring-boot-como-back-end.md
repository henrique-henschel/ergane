# ADR-0001: Java 21 + Spring Boot como back-end
- Status: Aceita
- Data: 2026-11-02

## Contexto
O Ergane é um SaaS de gestão comercial voltado a pequenos negócios e vendedores autônomos, abrangendo o registro de vendas, encomendas e valores recebidos. O sistema precisa garantir precisão absoluta no cálculo de valores monetários (RT04), isolamento seguro de dados entre múltiplos negócios (RT01) e suporte a regras transacionais consistentes. Tratando-se de um projeto acadêmico em grupo com prazo fixo para a entrega do MVP, a escolha da plataforma do back-end deve mitigar riscos técnicos, aproveitando o conhecimento prévio da equipe e selecionando um ecossistema maduro, com suporte de longo prazo e componentes integrados de fábrica.

## Decisão
Adotar Java 21 (LTS) e o framework Spring Boot (linha 4) como a tecnologia central do back-end (`apps/api/`).

A aplicação utiliza o ecossistema Spring Boot integrando módulos oficiais:
- Spring Web MVC para criação da API REST e comunicação HTTP;
- Spring Data JPA com Hibernate para persistência relacional;
- Bean Validation (`spring-boot-starter-validation`) para validação declarativa de contratos de entrada nos DTOs;
- Spring Security planejado para o fluxo de autenticação e proteção de sessão (UC01).

A compilação tem como alvo o Java 21 (LTS), usufruindo de tipagem estática forte, construções modernas da linguagem (como `records` para DTOs imutáveis) e da classe `BigDecimal` para operações financeiras exatas sem erros de ponto flutuante. A escolha da linha Spring Boot 4 em detrimento da 3.x alinha o projeto às versões com suporte ativo e atualizações contínuas de segurança, uma vez que o suporte comunitário gratuito da linha 3.5 foi encerrado. A escolha também valoriza a alta empregabilidade no mercado e a experiência prévia dos membros do grupo.

## Alternativas consideradas
- **Node.js (NestJS / Express):** embora possibilite compartilhar TypeScript entre back-end e front-end, o JavaScript representa números nativamente em ponto flutuante (IEEE 754), exigindo bibliotecas externas adicionais para garantir precisão financeira. Além disso, a equipe possui menor domínio com os padrões corporativos do NestJS.
- **.NET (C# / ASP.NET Core):** oferece excelente desempenho, tipagem forte e suporte ao tipo `decimal`, mas a equipe não tem a mesma familiaridade com o ecossistema e ferramentas da Microsoft quanto possui com o ecossistema Java/Spring.

## Consequências
- **Ganhos:** ecossistema robusto, estável e amplamente documentado; integração nativa entre camada web, validação e persistência; garantia de exatidão em cálculos contábeis com `BigDecimal`; rápida velocidade de entrega inicial devido à familiaridade técnica do time.
- **Custos e limitações:** maior consumo inicial de memória e tempo de inicialização (*cold start*) comparado a runtimes mais leves; a configuração do Spring Security e do provedor de autenticação fica postergada para a implementação do UC01.
