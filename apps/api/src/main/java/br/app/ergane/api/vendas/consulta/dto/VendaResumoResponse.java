package br.app.ergane.api.vendas.consulta.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * mostra os dados principais da venda na lista. {@code detalhada} indica que
 * existe pelo menos um item, mas nao garante que o historico esteja completo.
 */
public record VendaResumoResponse(
        UUID id,
        LocalDate dataOcorrencia,
        LocalTime horaOcorrencia,
        String fusoOcorrencia,
        BigDecimal valor,
        String origem,
        String situacao,
        boolean detalhada
) { }
