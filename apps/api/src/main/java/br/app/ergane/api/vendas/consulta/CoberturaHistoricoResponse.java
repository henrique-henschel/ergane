package br.app.ergane.api.vendas.consulta;

import java.time.Instant;
import java.time.LocalDate;

public record CoberturaHistoricoResponse(
        Boolean completa,
        LocalDate periodoDisponivelDe,
        LocalDate periodoDisponivelAte,
        Instant atualizadoEm
) {

    public static CoberturaHistoricoResponse desconhecida() {
        // sem metadados de sincronização, não há evidência para afirmar cobertura completa.
        return new CoberturaHistoricoResponse(null, null, null, null);
    }
}