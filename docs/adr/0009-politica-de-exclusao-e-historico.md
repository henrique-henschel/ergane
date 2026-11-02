# ADR-0009: Política de exclusão e histórico
- Status: Aceita
- Data: 2026-11-02

## Contexto
Em sistemas de gestão de vendas e encomendas, a integridade do histórico é um requisito regulatório e operacional inegociável (RT02). Vendas registradas no passado, relatórios analíticos e itens vendidos não podem perder seu contexto temporal caso um produto seja descontinuado ou alterado. Por outro lado, cadastros meramente organizacionais e sem dependências operacionais (como uma categoria recém-criada sem produtos) geram poluição desnecessária se não puderem ser expurgados definitivamente. É indispensável definir uma política consistente que diferencie o tratamento de dados de valor histórico de entidades sem relevância contábil, prevenindo destruição acidental de dados no banco.

## Decisão
Configurar `ON DELETE RESTRICT` em todas as chaves estrangeiras do banco de dados, aplicar exclusão lógica com preservação de estado para entidades de valor histórico e restringir a exclusão física estritamente a cadastros sem vínculos.

A política de ciclo de vida e retenção compreende:
- Proteção máxima de integridade no banco: nenhuma foreign key utiliza `CASCADE` ou `SET NULL`. Todas declaram explicitamente `ON DELETE RESTRICT`, impedindo a deleção no banco de qualquer registro pai que possua filhos relacionados;
- Exclusão lógica para entidades com valor histórico:
  - Produtos nunca são excluídos fisicamente após o cadastro: são arquivados logicamente por meio do preenchimento da coluna `arquivado_em TIMESTAMPTZ` (UC06);
  - Vendas e encomendas futuras adotarão cancelamento via status/máquina de estados, mantendo o histórico de auditoria e autoria intacto (UC13, UC19);
- Restrições a produtos arquivados: um produto arquivado não pode sofrer alterações cadastrais (`ProdutoService.atualizar` valida e rejeita a operação com erro de regra de negócio) e é automaticamente excluído da listagem padrão de seleção para novas vendas;
- Exclusão física permitida exclusivamente onde não há histórico a preservar: categorias (UC07) podem ser fisicamente removidas (`categoriaRepository.delete(...)`), desde que não possuam nenhum produto associado (ativo ou arquivado). A tentativa de exclusão com vínculos viola a constraint do banco e é convertida pela camada de serviço em uma exceção de regra de negócio com mensagem amigável ao usuário.

## Alternativas consideradas
- **ON DELETE CASCADE:** altamente arriscado para dados comerciais e financeiros. A exclusão acidental de uma categoria ou produto propagaria a deleção para centenas de itens de vendas passadas, corrompendo balanços e históricos contábeis.
- **ON DELETE SET NULL:** desvincularia transações financeiras de seus produtos ou categorias de origem, gerando registros de vendas órfãos e quebrando relatórios históricos e métricas de desempenho.
- **Exclusão lógica (*soft delete*) indiscriminada em todas as tabelas:** adicionaria complexidade injustificada a cadastros auxiliares simples (como categorias), exigindo colunas de auditoria de remoção, índices parciais adicionais e tratamento de integridade em tabelas sem relevância histórica.

## Consequências
- **Ganhos:** preservação irrevogável do histórico contábil e de vendas anteriores (RT02); proteção física contra exclusões destrutivas no banco; regras claras e intuitivas para o usuário final sobre a diferença entre arquivar e excluir.
- **Custos e limitações:** consultas de catálogo e buscas ativas precisam incluir explicitamente cláusulas de exclusão lógica (`arquivado_em IS NULL`); índices de unicidade exigem cláusulas parciais para que nomes de produtos arquivados possam ser reutilizados no futuro; o serviço precisa interceptar exceções de integridade do banco para exibir mensagens orientativas ao usuário.
