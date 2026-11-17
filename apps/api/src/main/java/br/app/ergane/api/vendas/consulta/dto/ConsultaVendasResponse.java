package br.app.ergane.api.vendas.consulta.dto;

import java.util.List;

/**
 * traz os resultados da pagina, os dados da paginacao e a cobertura conhecida
 * do historico.
 */
public record ConsultaVendasResponse(
        List<VendaResumoResponse> registros,
        int pagina,
        int tamanho,
        long totalRegistros,
        int totalPaginas,
        CoberturaHistoricoResponse cobertura
) {
    /** impede que a lista de resultados seja alterada depois da resposta criada. */
    public ConsultaVendasResponse {
        registros = List.copyOf(registros);
    }
}
