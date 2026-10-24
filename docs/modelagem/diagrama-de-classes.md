# Diagrama de Classes

```mermaid
classDiagram

%% ==================================================================
%% ERGANE APP — Diagrama de Classes (v3 · MVP)
%% Persistência relacional · sem integrações (fiscal/adquirentes) no MVP
%%
%% Convenções
%%  - Atributos no formato  nome: Tipo
%%  - A --> B  : A guarda o id (UUID) de B — referência entre agregados
%%  - A *-- B  : B é parte de A (composição); no domínio B não guarda o id de A
%%               (a FK existe só no banco)
%%  - A ..> E  : A usa o enum / tipo / serviço E
%%  - Todo agregado raiz tem contaId (tenant do SaaS) para isolar os dados
%%  - Dinheiro, Quantidade e Percentual são usados em todo o diagrama e não
%%    têm setas próprias, para não poluir o desenho
%%  - Atributos opcionais: Conta.whatsapp, Cliente.telefone/documento/endereco/
%%    limiteCredito (nulo = sem limite), ItemVendavel.categoriaId/codigo/
%%    precoCusto (nulo = "não sei")/emoji/fotoUrl, Venda.clienteId/sessaoCaixaId/
%%    desconto/cancelamento/desfeitaEm, ItemVenda.itemVendavelId (nulo = avulso)/
%%    custoUnitario, Recebimento.valorEntregue (só dinheiro), Devolucao.sessaoCaixaId,
%%    SessaoCaixa.fechadaEm/saldoContado/saldoEsperadoNoFechamento,
%%    MovimentoEstoque.motivoPerda/custoUnitario/custoAnterior/origemId,
%%    MovimentoCaixa.origemId, Despesa.sessaoCaixaId,
%%    BaixaParcela.forma/sessaoCaixaId/origemId, RecebimentoFiado.sessaoCaixaId/desfeitoEm,
%%    ContaReceber.canceladaEm
%%  - Instant = data/hora absoluta (timestamptz); LocalDate = dia civil no
%%    fuso da Conta (Conta.hoje / Conta.diaCivilDe)
%%  - Status de fiado e situação de estoque são CALCULADOS, nunca gravados
%%  - A venda é montada no app (carrinho) e chega pronta em um único comando:
%%    não existe rascunho de venda no servidor
%% ==================================================================

namespace COMPARTILHADO {
    class Dinheiro {
        <<ValueObject>>
        -valor: BigDecimal
        +de(valor: BigDecimal) Dinheiro$
        +zero() Dinheiro$
        +somar(outro: Dinheiro) Dinheiro
        +subtrair(outro: Dinheiro) Dinheiro
        +multiplicar(quantidade: Quantidade) Dinheiro
        +aplicar(percentual: Percentual) Dinheiro
        +menor(outro: Dinheiro) Dinheiro
        +isZero() boolean
        +isNegativo() boolean
    }
    class Quantidade {
        <<ValueObject>>
        -valor: BigDecimal
        +de(valor: BigDecimal, unidade: UnidadeMedida) Quantidade$
        +somar(outra: Quantidade) Quantidade
        +subtrair(outra: Quantidade) Quantidade
        +isPositiva() boolean
        +isNegativaOuZero() boolean
    }
    class Percentual {
        <<ValueObject>>
        -valor: BigDecimal
        +de(valor: BigDecimal) Percentual$
        +zero() Percentual$
        +isZero() boolean
    }
    class Telefone {
        <<ValueObject>>
        -digitos: String
        +de(texto: String) Telefone$
        +formatado() String
        +linkWhatsApp(mensagem: String) String
    }
    class Endereco {
        <<ValueObject>>
        -cep: String
        -logradouro: String
        -numero: String
        -complemento: String
        -bairro: String
        -cidade: String
        -uf: String
        +formatarCompleto() String
    }
    class CpfCnpj {
        <<ValueObject>>
        -valor: String
        +isCpf() boolean
        +isCnpj() boolean
        +formatado() String
    }
    class FormaPagamento {
        <<enumeration>>
        DINHEIRO
        PIX
        CARTAO_DEBITO
        CARTAO_CREDITO
        OUTRO
        +afetaCaixa() boolean
        +permiteTroco() boolean
        +isCartao() boolean
    }
}

namespace CADASTROS {
    class Conta {
        -id: UUID
        -nome: String
        -nomeResponsavel: String
        -whatsapp: Telefone
        -fusoHorario: ZoneId
        -ramos: Set~String~
        -boasVindasConcluidas: boolean
        -ativa: boolean
        -criadaEm: Instant
        +hoje() LocalDate
        +diaCivilDe(instante: Instant) LocalDate
        +alterarDados(nome: String, whatsapp: Telefone) void
        +concluirBoasVindas(ramos: Set~String~) void
        +ativar() void
        +desativar() void
    }
    class ConfiguracaoNegocio {
        -id: UUID
        -contaId: UUID
        -formasAtivas: Set~FormaPagamento~
        -venderFiado: boolean
        -taxaDebito: Percentual
        -taxaCreditoAVista: Percentual
        -taxaCreditoParcelado: Percentual
        -chavePix: String
        -prazoPadraoFiadoDias: int
        -bloquearVendaSemEstoque: boolean
        -trocoInicialPadrao: Dinheiro
        +padrao(contaId: UUID) ConfiguracaoNegocio$
        +isFormaAtiva(forma: FormaPagamento) boolean
        +ativarForma(forma: FormaPagamento) void
        +desativarForma(forma: FormaPagamento) void
        +ligarFiado() void
        +desligarFiado() void
        +quantidadeFormasAtivas() int
        +alterarTaxas(debito: Percentual, creditoAVista: Percentual, creditoParcelado: Percentual) void
        +taxaPara(forma: FormaPagamento, parcelas: int) Percentual
        +alterarPrazoPadraoFiado(dias: int) void
        +vencimentoPadrao(hoje: LocalDate) LocalDate
        +alterarTrocoInicialPadrao(valor: Dinheiro) void
        +alterarChavePix(chave: String) void
    }
    class Loja {
        -id: UUID
        -contaId: UUID
        -nome: String
        -endereco: Endereco
        -ativa: boolean
        +ativar() void
        +desativar() void
    }
    class PontoDeVenda {
        -id: UUID
        -contaId: UUID
        -lojaId: UUID
        -nome: String
        -ativo: boolean
        +ativar() void
        +desativar() void
    }
    class Usuario {
        -id: UUID
        -contaId: UUID
        -nome: String
        -email: String
        -senhaHash: String
        -papeis: Set~Papel~
        -lojasAutorizadas: Set~UUID~
        -percentualMaximoDesconto: Percentual
        -ativo: boolean
        +possuiPapel(papel: Papel) boolean
        +podeVenderNaLoja(lojaId: UUID) boolean
        +podeOperarCaixaNaLoja(lojaId: UUID) boolean
        +podeConcederDesconto(percentual: Percentual) boolean
        +ativar() void
        +desativar() void
    }
    class Cliente {
        -id: UUID
        -contaId: UUID
        -nome: String
        -telefone: Telefone
        -observacao: String
        -documento: CpfCnpj
        -endereco: Endereco
        -limiteCredito: Dinheiro
        -bloqueadoParaFiado: boolean
        -saldoCredito: Dinheiro
        -arquivado: boolean
        -criadoEm: Instant
        +temLimite() boolean
        +ultrapassariaLimite(saldoDevedor: Dinheiro, novoFiado: Dinheiro) boolean
        +bloquearFiado() void
        +desbloquearFiado() void
        +creditar(valor: Dinheiro, tipo: TipoLancamentoCredito, origemId: UUID) LancamentoCredito
        +usarCredito(valorMaximo: Dinheiro, origemId: UUID) LancamentoCredito
        +podeSerCobrado() boolean
        +arquivar() void
        +desarquivar() void
        +podeSerExcluido(saldoEmAberto: Dinheiro) boolean
    }
    class Papel {
        <<enumeration>>
        PROPRIETARIO
        GERENTE
        VENDEDOR
        OPERADOR_CAIXA
    }
}

namespace CATALOGO {
    class ItemVendavel {
        -id: UUID
        -contaId: UUID
        -categoriaId: UUID
        -codigo: String
        -descricao: String
        -tipo: TipoItem
        -unidade: UnidadeMedida
        -precoVenda: Dinheiro
        -precoCusto: Dinheiro
        -controlaEstoque: boolean
        -emoji: String
        -fotoUrl: String
        -cor: CorCartao
        -ativo: boolean
        -criadoEm: Instant
        -atualizadoEm: Instant
        +alterarPrecoVenda(novoPreco: Dinheiro) void
        +alterarPrecoCusto(novoCusto: Dinheiro) void
        +definirControleEstoque(controla: boolean) void
        +temCusto() boolean
        +margem() Percentual
        +lucroPorUnidade() Dinheiro
        +isVendendoNoPrejuizo() boolean
        +ativar() void
        +desativar() void
    }
    class Categoria {
        -id: UUID
        -contaId: UUID
        -nome: String
        -emoji: String
    }
    class TipoItem {
        <<enumeration>>
        PRODUTO
        SERVICO
    }
    class UnidadeMedida {
        <<enumeration>>
        UNIDADE
        QUILOGRAMA
        GRAMA
        LITRO
        PACOTE
        CAIXA
        DUZIA
        +permiteFracionar() boolean
        +casasDecimais() int
    }
    class CorCartao {
        <<enumeration>>
        VERDE
        AZUL
        AMARELO
        LARANJA
        VERMELHO
        ROSA
        ROXO
        CINZA
    }
}

namespace VENDAS {
    class Venda {
        -id: UUID
        -contaId: UUID
        -lojaId: UUID
        -vendedorId: UUID
        -clienteId: UUID
        -sessaoCaixaId: UUID
        -numero: long
        -situacao: SituacaoVenda
        -desconto: Desconto
        -observacao: String
        -itens: List~ItemVenda~
        -recebimentos: List~Recebimento~
        -alertasFiadoIgnorados: Set~AlertaFiado~
        -cancelamento: Cancelamento
        -registradaEm: Instant
        -desfeitaEm: Instant
        +registrar(contaId: UUID, lojaId: UUID, vendedorId: UUID, numero: long, itens: List~ItemVenda~, desconto: Desconto, recebimentos: List~Recebimento~, clienteId: UUID, sessaoCaixaId: UUID, permitirTotalZero: boolean, alertasIgnorados: Set~AlertaFiado~) Venda$
        +podeSerDesfeita(agora: Instant) boolean
        +desfazer(agora: Instant) void
        +cancelar(cancelamento: Cancelamento) void
        +subtotal() Dinheiro
        +valorDesconto() Dinheiro
        +total() Dinheiro
        +isBrinde() boolean
        +totalRecebido() Dinheiro
        +totalPorForma(forma: FormaPagamento) Dinheiro
        +troco() Dinheiro
        +valorFiado() Dinheiro
        +isFiado() boolean
        +totalTaxas() Dinheiro
        +custoMercadorias() Dinheiro
        +temItemSemCusto() boolean
        +contaNoFaturamento() boolean
    }
    class ItemVenda {
        -id: UUID
        -itemVendavelId: UUID
        -descricao: String
        -unidade: UnidadeMedida
        -controlaEstoque: boolean
        -quantidade: Quantidade
        -precoUnitario: Dinheiro
        -custoUnitario: Dinheiro
        +deItemVendavel(item: ItemVendavel, quantidade: Quantidade) ItemVenda$
        +avulso(valor: Dinheiro, descricao: String) ItemVenda$
        +isAvulso() boolean
        +subtotal() Dinheiro
        +temCusto() boolean
        +custoTotal() Dinheiro
    }
    class Recebimento {
        -id: UUID
        -forma: FormaPagamento
        -valor: Dinheiro
        -valorEntregue: Dinheiro
        -parcelasCartao: int
        -taxa: Dinheiro
        -recebidoEm: Instant
        +emDinheiro(valor: Dinheiro, valorEntregue: Dinheiro) Recebimento$
        +emCartao(forma: FormaPagamento, valor: Dinheiro, parcelas: int, taxa: Percentual) Recebimento$
        +de(forma: FormaPagamento, valor: Dinheiro) Recebimento$
        +troco() Dinheiro
        +faltaReceber() Dinheiro
    }
    class Desconto {
        <<ValueObject>>
        -tipo: TipoDesconto
        -valor: BigDecimal
        +emReais(valor: Dinheiro) Desconto$
        +emPercentual(percentual: Percentual) Desconto$
        +calcularSobre(subtotal: Dinheiro) Dinheiro
    }
    class Cancelamento {
        <<ValueObject>>
        -motivo: MotivoCancelamento
        -observacao: String
        -canceladoPorId: UUID
        -canceladoEm: Instant
    }
    class Devolucao {
        -id: UUID
        -contaId: UUID
        -vendaId: UUID
        -registradaPorId: UUID
        -sessaoCaixaId: UUID
        -formaReembolso: FormaReembolso
        -motivo: String
        -itens: List~ItemDevolvido~
        -registradaEm: Instant
        +adicionarItem(item: ItemDevolvido) void
        +valorTotal() Dinheiro
    }
    class ItemDevolvido {
        -id: UUID
        -itemVendaId: UUID
        -itemVendavelId: UUID
        -quantidade: Quantidade
        -valorUnitario: Dinheiro
        -retornaAoEstoque: boolean
        +subtotal() Dinheiro
    }
    class SituacaoVenda {
        <<enumeration>>
        CONFIRMADA
        CANCELADA
        DESFEITA
        +contaNoFaturamento() boolean
        +apareceNoHistorico() boolean
    }
    class TipoDesconto {
        <<enumeration>>
        VALOR
        PERCENTUAL
    }
    class MotivoCancelamento {
        <<enumeration>>
        REGISTRO_ERRADO
        CLIENTE_DESISTIU
        DEVOLUCAO
        PAGAMENTO_NAO_CAIU
        OUTRO
    }
    class FormaReembolso {
        <<enumeration>>
        DINHEIRO
        PIX
        ABATIMENTO_FIADO
        CREDITO_CLIENTE
        +afetaCaixa() boolean
    }
}

namespace ESTOQUE {
    class Estoque {
        -id: UUID
        -contaId: UUID
        -lojaId: UUID
        -itemVendavelId: UUID
        -quantidade: Quantidade
        -quantidadeMinima: Quantidade
        +registrarEntrada(quantidade: Quantidade, motivo: MotivoMovimento, origemId: UUID, custoUnitario: Dinheiro, custoAnterior: Dinheiro, usuarioId: UUID, observacao: String) MovimentoEstoque
        +registrarSaida(quantidade: Quantidade, motivo: MotivoMovimento, origemId: UUID, permitirNegativo: boolean, usuarioId: UUID) MovimentoEstoque
        +registrarPerda(quantidade: Quantidade, motivo: MotivoPerda, usuarioId: UUID, observacao: String) MovimentoEstoque
        +ajustar(quantidadeContada: Quantidade, usuarioId: UUID, observacao: String) MovimentoEstoque
        +desfazerUltimo(movimento: MovimentoEstoque, agora: Instant) void
        +definirMinimo(quantidade: Quantidade) void
        +faltaPara(quantidade: Quantidade) Quantidade
        +situacao() SituacaoEstoque
    }
    class MovimentoEstoque {
        -id: UUID
        -contaId: UUID
        -estoqueId: UUID
        -tipo: TipoMovimento
        -motivo: MotivoMovimento
        -motivoPerda: MotivoPerda
        -quantidade: Quantidade
        -saldoApos: Quantidade
        -custoUnitario: Dinheiro
        -custoAnterior: Dinheiro
        -origemId: UUID
        -usuarioId: UUID
        -observacao: String
        -ocorridoEm: Instant
        +isManual() boolean
        +podeSerDesfeito(agora: Instant) boolean
    }
    class TipoMovimento {
        <<enumeration>>
        ENTRADA
        SAIDA
    }
    class MotivoMovimento {
        <<enumeration>>
        COMPRA
        VENDA
        CANCELAMENTO_VENDA
        DEVOLUCAO
        AJUSTE
        PERDA
        TRANSFERENCIA
    }
    class MotivoPerda {
        <<enumeration>>
        VENCIDO
        QUEBRADO
        OUTRO
    }
    class SituacaoEstoque {
        <<enumeration>>
        OK
        ACABANDO
        ACABOU
        +gravidade() int
    }
}

namespace CAIXA {
    class SessaoCaixa {
        -id: UUID
        -contaId: UUID
        -pontoVendaId: UUID
        -operadorId: UUID
        -dataReferencia: LocalDate
        -saldoInicial: Dinheiro
        -saldoContado: Dinheiro
        -saldoEsperadoNoFechamento: Dinheiro
        -observacaoFechamento: String
        -movimentos: List~MovimentoCaixa~
        -abertaEm: Instant
        -fechadaEm: Instant
        +abrir(contaId: UUID, pontoVendaId: UUID, operadorId: UUID, dataReferencia: LocalDate, saldoInicial: Dinheiro) SessaoCaixa$
        +alterarSaldoInicial(valor: Dinheiro) void
        +registrarMovimento(tipo: TipoMovimentoCaixa, valor: Dinheiro, origemId: UUID, descricao: String) void
        +removerMovimentosDe(origemId: UUID) void
        +fechar(saldoContado: Dinheiro, observacao: String) void
        +reabrir() void
        +isAberta() boolean
        +saldoEsperado() Dinheiro
        +divergencia() Dinheiro
        +resultadoFechamento() ResultadoFechamento
        +esperadoMudouDepoisDoFechamento() boolean
    }
    class MovimentoCaixa {
        -id: UUID
        -tipo: TipoMovimentoCaixa
        -valor: Dinheiro
        -origemId: UUID
        -descricao: String
        -ocorridoEm: Instant
    }
    class Despesa {
        -id: UUID
        -contaId: UUID
        -lojaId: UUID
        -sessaoCaixaId: UUID
        -registradaPorId: UUID
        -descricao: String
        -categoria: CategoriaDespesa
        -valor: Dinheiro
        -forma: FormaDespesa
        -ocorridaEm: Instant
        +registrar(contaId: UUID, lojaId: UUID, sessaoCaixaId: UUID, registradaPorId: UUID, descricao: String, categoria: CategoriaDespesa, valor: Dinheiro, forma: FormaDespesa) Despesa$
        +afetaGaveta() boolean
        +descricaoExibida() String
        +podeSerDesfeita(agora: Instant) boolean
    }
    class TipoMovimentoCaixa {
        <<enumeration>>
        VENDA
        RECEBIMENTO_FIADO
        DESPESA
        SUPRIMENTO
        SANGRIA
        ESTORNO
        REEMBOLSO
        +isEntrada() boolean
    }
    class ResultadoFechamento {
        <<enumeration>>
        BATEU
        SOBROU
        FALTOU
    }
    class CategoriaDespesa {
        <<enumeration>>
        MERCADORIA
        TRANSPORTE
        ALIMENTACAO
        PONTO
        TAXAS
        OUTROS
    }
    class FormaDespesa {
        <<enumeration>>
        DINHEIRO_DA_GAVETA
        OUTRA
    }
}

namespace FINANCEIRO {
    class ContaReceber {
        -id: UUID
        -contaId: UUID
        -clienteId: UUID
        -vendaId: UUID
        -valorTotal: Dinheiro
        -situacao: SituacaoContaReceber
        -parcelas: List~Parcela~
        -criadaEm: Instant
        -canceladaEm: Instant
        +abrir(contaId: UUID, clienteId: UUID, vendaId: UUID, valorFiadoDaVenda: Dinheiro, parcelas: List~Parcela~) ContaReceber$
        +baixar(parcelaId: UUID, valor: Dinheiro, tipo: TipoBaixa, forma: FormaPagamento, sessaoCaixaId: UUID, origemId: UUID) void
        +estornarBaixasDe(origemId: UUID) void
        +abaterPorDevolucao(valor: Dinheiro, devolucaoId: UUID) void
        +cancelar(quando: Instant) Dinheiro
        +saldo() Dinheiro
        +valorPago() Dinheiro
        +parcelasAbertas() List~Parcela~
        +status(hoje: LocalDate) StatusFiado
    }
    class Parcela {
        -id: UUID
        -numero: int
        -vencimento: LocalDate
        -valor: Dinheiro
        -criadaEm: Instant
        -baixas: List~BaixaParcela~
        +valorPago() Dinheiro
        +saldo() Dinheiro
        +isPaga() boolean
        +status(hoje: LocalDate) StatusFiado
        +diasDeAtraso(hoje: LocalDate) int
    }
    class BaixaParcela {
        -id: UUID
        -tipo: TipoBaixa
        -valor: Dinheiro
        -forma: FormaPagamento
        -sessaoCaixaId: UUID
        -origemId: UUID
        -registradaEm: Instant
    }
    class RecebimentoFiado {
        -id: UUID
        -contaId: UUID
        -clienteId: UUID
        -registradoPorId: UUID
        -sessaoCaixaId: UUID
        -forma: FormaPagamento
        -valor: Dinheiro
        -creditoUsado: Dinheiro
        -creditoGerado: Dinheiro
        -recebidoEm: Instant
        -desfeitoEm: Instant
        +registrar(cliente: Cliente, contasAbertas: List~ContaReceber~, valor: Dinheiro, forma: FormaPagamento, sessaoCaixaId: UUID, registradoPorId: UUID) RecebimentoFiado$
        +totalAbatido() Dinheiro
        +podeSerDesfeito(isUltimoDoCliente: boolean, hoje: LocalDate) boolean
        +desfazer(agora: Instant) void
    }
    class AlocacaoFiado {
        <<ValueObject>>
        -contaReceberId: UUID
        -parcelaId: UUID
        -valor: Dinheiro
    }
    class LancamentoCredito {
        -id: UUID
        -contaId: UUID
        -clienteId: UUID
        -tipo: TipoLancamentoCredito
        -valor: Dinheiro
        -origemId: UUID
        -ocorridoEm: Instant
    }
    class AlocacaoFifo {
        <<DomainService>>
        +ordenar(parcelasAbertas: List~Parcela~) List~Parcela~
        +alocar(contasAbertas: List~ContaReceber~, valorDisponivel: Dinheiro) List~AlocacaoFiado~
    }
    class AvaliacaoFiado {
        <<DomainService>>
        +saldoDevedor(cliente: Cliente, contas: List~ContaReceber~) Dinheiro
        +status(contas: List~ContaReceber~, hoje: LocalDate) StatusFiado
        +maiorAtraso(contas: List~ContaReceber~, hoje: LocalDate) int
        +alertas(cliente: Cliente, contas: List~ContaReceber~, novoFiado: Dinheiro, hoje: LocalDate) Set~AlertaFiado~
    }
    class IndicadoresFiado {
        <<DomainService>>
        +totalAReceber(contas: List~ContaReceber~) Dinheiro
        +totalAtrasado(contas: List~ContaReceber~, hoje: LocalDate) Dinheiro
        +percentualInadimplencia(contas: List~ContaReceber~, hoje: LocalDate) Percentual
        +clientesDevendo(contas: List~ContaReceber~) int
    }
    class CobrancaWhatsApp {
        <<DomainService>>
        +mensagem(conta: Conta, cliente: Cliente, saldoDevedor: Dinheiro, vencimentoMaisAntigo: LocalDate, chavePix: String) String
        +link(cliente: Cliente, mensagem: String) String
    }
    class SituacaoContaReceber {
        <<enumeration>>
        ABERTA
        QUITADA
        CANCELADA
    }
    class TipoBaixa {
        <<enumeration>>
        PAGAMENTO
        USO_CREDITO
        ABATIMENTO_DEVOLUCAO
    }
    class StatusFiado {
        <<enumeration>>
        QUITADO
        EM_DIA
        VENCE_EM_BREVE
        VENCE_HOJE
        ATRASADO
        +calcular(saldo: Dinheiro, vencimento: LocalDate, hoje: LocalDate) StatusFiado$
        +gravidade() int
    }
    class AlertaFiado {
        <<enumeration>>
        BLOQUEADO
        ATRASADO
        LIMITE
    }
    class TipoLancamentoCredito {
        <<enumeration>>
        EXCEDENTE_RECEBIMENTO
        CANCELAMENTO_VENDA
        DEVOLUCAO
        USO_EM_RECEBIMENTO
        ESTORNO
        +isEntrada() boolean
    }
}

%% ===== Cadastros =====
Loja "*" --> "1" Conta
PontoDeVenda "*" --> "1" Loja
Usuario "*" --> "1" Conta
Usuario "*" --> "*" Loja : autorizado em
Cliente "*" --> "1" Conta
ConfiguracaoNegocio "1" --> "1" Conta
Conta *-- Telefone
Loja *-- Endereco
Cliente *-- Endereco
Cliente *-- CpfCnpj
Cliente *-- Telefone
Usuario ..> Papel
ConfiguracaoNegocio ..> FormaPagamento

%% ===== Catálogo =====
ItemVendavel "*" --> "1" Conta
Categoria "*" --> "1" Conta
ItemVendavel "*" --> "0..1" Categoria
ItemVendavel ..> TipoItem
ItemVendavel ..> UnidadeMedida
ItemVendavel ..> CorCartao

%% ===== Vendas =====
Venda "*" --> "1" Loja
Venda "*" --> "1" Usuario : vendedor
Venda "*" --> "0..1" Cliente : cliente
Venda "*" --> "0..1" SessaoCaixa : registrada em
Venda "1" *-- "1..*" ItemVenda
Venda "1" *-- "*" Recebimento
Venda *-- "0..1" Desconto
Venda *-- "0..1" Cancelamento
ItemVenda "*" --> "0..1" ItemVendavel : snapshot
Recebimento ..> FormaPagamento
Venda ..> SituacaoVenda
Venda ..> AlertaFiado
Desconto ..> TipoDesconto
Cancelamento ..> MotivoCancelamento
Devolucao "*" --> "1" Venda
Devolucao "*" --> "1" Usuario : registrada por
Devolucao "*" --> "0..1" SessaoCaixa : reembolso em dinheiro
Devolucao "1" *-- "1..*" ItemDevolvido
ItemDevolvido "*" --> "1" ItemVenda
Devolucao ..> FormaReembolso

%% ===== Estoque =====
Estoque "*" --> "1" Loja
Estoque "*" --> "1" ItemVendavel
MovimentoEstoque "*" --> "1" Estoque
MovimentoEstoque "*" --> "1" Usuario : responsável
MovimentoEstoque ..> TipoMovimento
MovimentoEstoque ..> MotivoMovimento
MovimentoEstoque ..> MotivoPerda
Estoque ..> SituacaoEstoque

%% ===== Caixa =====
SessaoCaixa "*" --> "1" PontoDeVenda
SessaoCaixa "*" --> "1" Usuario : operador
SessaoCaixa "1" *-- "*" MovimentoCaixa
MovimentoCaixa ..> TipoMovimentoCaixa
SessaoCaixa ..> ResultadoFechamento
Despesa "*" --> "1" Loja
Despesa "*" --> "1" Usuario : registrada por
Despesa "*" --> "0..1" SessaoCaixa : paga com a gaveta
Despesa ..> CategoriaDespesa
Despesa ..> FormaDespesa

%% ===== Financeiro (fiado) =====
ContaReceber "*" --> "1" Cliente
ContaReceber "0..1" --> "1" Venda : originada de
ContaReceber "1" *-- "1..*" Parcela
Parcela "1" *-- "*" BaixaParcela
ContaReceber ..> SituacaoContaReceber
Parcela ..> StatusFiado
BaixaParcela ..> TipoBaixa
BaixaParcela ..> FormaPagamento
RecebimentoFiado "*" --> "1" Cliente
RecebimentoFiado "*" --> "0..1" SessaoCaixa : dinheiro na gaveta
AlocacaoFifo ..> AlocacaoFiado
AlocacaoFiado "*" --> "1" ContaReceber
RecebimentoFiado ..> FormaPagamento
RecebimentoFiado ..> AlocacaoFifo
LancamentoCredito "*" --> "1" Cliente
LancamentoCredito ..> TipoLancamentoCredito
AvaliacaoFiado ..> AlertaFiado
AvaliacaoFiado ..> StatusFiado
IndicadoresFiado ..> ContaReceber
CobrancaWhatsApp ..> Telefone
```
