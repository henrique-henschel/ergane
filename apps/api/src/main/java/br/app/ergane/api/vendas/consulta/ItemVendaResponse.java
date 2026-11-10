package br.app.ergane.api.vendas.consulta;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemVendaResponse(
        UUID id,
        UUID itemVendavelId,
        String descricao,
        String unidade,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal custoUnitario
) {

    public static ItemVendaResponse de(ItemVenda item) {
        return new ItemVendaResponse(
                item.getId(),
                item.getItemVendavelId(),
                item.getDescricao(),
                item.getUnidade(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getCustoUnitario()
        );
    }
}