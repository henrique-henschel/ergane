package br.app.ergane.api.vendas.consulta;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record VendaResumoResponse(
        UUID id,
        Instant ocorridaEm,
        LocalDate dataOcorrencia,
        BigDecimal valor,
        String origem,
        String situacao
) {

    public static VendaResumoResponse de(Venda venda) {
        return new VendaResumoResponse(
                venda.getId(),
                venda.getRegistradaEm(),
                venda.getDataOcorrencia(),
                venda.getTotal(),
                venda.getOrigem(),
                venda.getSituacao()
        );
    }
}