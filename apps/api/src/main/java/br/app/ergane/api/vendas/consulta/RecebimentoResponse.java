package br.app.ergane.api.vendas.consulta;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RecebimentoResponse(
        UUID id,
        String forma,
        BigDecimal valor,
        Instant recebidoEm
) {

    public static RecebimentoResponse de(Recebimento recebimento) {
        return new RecebimentoResponse(
                recebimento.getId(),
                recebimento.getForma(),
                recebimento.getValor(),
                recebimento.getRecebidoEm()
        );
    }
}