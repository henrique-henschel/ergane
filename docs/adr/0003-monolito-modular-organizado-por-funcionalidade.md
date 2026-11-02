# ADR-0003: Monólito modular organizado por funcionalidade
- Status: Aceita
- Data: 2026-11-02

## Contexto
O sistema precisa de uma estrutura de código clara, modular e de fácil navegação para suportar o desenvolvimento paralelo entre diferentes membros da equipe. Projetos divididos em microsserviços geram alto custo de infraestrutura e sobrecarga operacional desnecessária para um time acadêmico focado em um MVP. Por outro lado, arquiteturas tradicionais monolíticas organizadas puramente por camada técnica (`controller/`, `service/`, `repository/`) fragmentam a coesão do negócio, dificultam a visualização dos limites de cada domínio e frequentemente levam a acoplamentos indevidos entre áreas não relacionadas.

## Decisão
Estruturar o back-end como um monólito modular em uma única aplicação Spring Boot, organizado por funcionalidade (*package by feature*) e encapsulado via visibilidade de pacote (*package-private*).

A aplicação divide o código por áreas de negócio sob o pacote raiz `br.app.ergane.api`:
- Módulos organizados por funcionalidade: `catalogo` (com subpacotes `categoria` e `produto`), `compartilhado`, e os módulos planejados `acesso`, `vendas`, `encomendas` e `financeiro`;
- Cada pacote agrupa verticalmente todos os seus artefatos: entidade JPA, repositório, serviço, controlador REST e DTOs (records de request e response);
- Encapsulamento estrito com repositórios *package-private*: interfaces de repositório (como `CategoriaRepository` e `ProdutoRepository`) não possuem modificador `public`. Nenhum módulo tem permissão de consultar ou manipular diretamente o banco de dados de outro módulo;
- A comunicação intermódulos ocorre exclusivamente por meio dos métodos públicos de serviços de negócio (ex.: `ProdutoService` valida categorias injetando e chamando `CategoriaService.existe(id)`, jamais acessando o repositório de categorias);
- Proibição explícita de dependências circulares entre serviços de diferentes módulos.

## Alternativas consideradas
- **Organização por camada técnica (package-by-layer: controllers/, services/, repositories/):** espalha as classes de um mesmo caso de uso por múltiplos diretórios, obriga todas as interfaces e classes a serem públicas e mascara as fronteiras conceituais do domínio, aumentando o risco de acoplamento indevido.
- **Microsserviços:** traria complexidade excessiva de rede, latência, eventual consistency, múltiplos pipelines de CI/CD e governança de dados distribuída, sendo desproporcional e injustificada para a complexidade do produto e a dimensão da equipe.

## Consequências
- **Ganhos:** alta coesão e baixo acoplamento; fronteiras de domínio reforçadas em tempo de compilação pelo controle de acesso do Java; facilidade de leitura e manutenção de cada caso de uso; simplicidade operacional com deploy único; facilidade para futura extração de serviços caso o produto demande escala independente.
- **Custos e limitações:** requer disciplina contínua da equipe para não expor repositórios como públicos e evitar dependências circulares entre serviços; todos os módulos compartilham o mesmo processo de execução e o mesmo banco de dados.
