package br.app.ergane.api.vendas.consulta;

import java.util.List;

public record ConsultaVendasResponse(
        List<VendaResumoResponse> registros,
        int pagina,
        int tamanho,
        long totalRegistros,
        int totalPaginas,
        CoberturaHistoricoResponse cobertura
) {

    public ConsultaVendasResponse {
        registros = List.copyOf(registros);
    }
}