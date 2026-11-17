package br.app.ergane.api.vendas.consulta.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * mostra os dados da venda, seus itens guardados e a cobertura conhecida do
 * historico. o campo de itens detalhados indica que existe pelo menos um item,
 * mas nao garante que todos os itens do historico estejam disponiveis.
 */
public record VendaDetalheResponse(
        UUID id,
        LocalDate dataOcorrencia,
        LocalTime horaOcorrencia,
        String fusoOcorrencia,
        Instant registradaEm,
        BigDecimal totalRegistrado,
        String origem,
        String situacao,
        String formaPagamentoInformada,
        UUID encomendaOrigemId,
        boolean itensDetalhados,
        List<ItemVendaResponse> itens,
        CoberturaHistoricoResponse cobertura
) {
    /** impede que a lista de itens seja alterada depois da resposta criada. */
    public VendaDetalheResponse {
        itens = List.copyOf(itens);
    }
}
