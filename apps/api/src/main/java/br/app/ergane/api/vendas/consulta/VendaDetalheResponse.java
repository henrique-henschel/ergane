package br.app.ergane.api.vendas.consulta;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record VendaDetalheResponse(
        UUID id,
        Instant ocorridaEm,
        LocalDate dataOcorrencia,
        BigDecimal valor,
        String origem,
        String situacao,
        boolean itensIndividualizados,
        List<ItemVendaResponse> itens,
        List<RecebimentoResponse> recebimentos,
        CoberturaHistoricoResponse cobertura
) {

    public VendaDetalheResponse {
        itens = List.copyOf(itens);
        recebimentos = List.copyOf(recebimentos);
    }
}
