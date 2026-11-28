package br.app.ergane.api.operacao.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * mostra um item da venda com os dados e valores guardados na hora da venda.
 */
public record ItemVendaResponse(
        UUID id,
        UUID produtoId,
        String nome,
        String diferenciador,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal custoUnitario
) { }
