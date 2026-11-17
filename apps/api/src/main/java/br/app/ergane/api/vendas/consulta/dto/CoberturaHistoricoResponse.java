package br.app.ergane.api.vendas.consulta.dto;

import java.time.Instant;
import java.time.LocalDate;

/**
 * mostra o periodo do historico que sabemos que esta disponivel. valores nulos
 * significam que nao sabemos se o historico esta completo.
 */
public record CoberturaHistoricoResponse(
        Boolean completa,
        LocalDate periodoDisponivelDe,
        LocalDate periodoDisponivelAte,
        Instant atualizadoEm
) {
    /**
     * cria uma resposta para quando ainda nao temos uma fonte confiavel sobre
     * o periodo disponivel.
     */
    public static CoberturaHistoricoResponse desconhecida() {
        return new CoberturaHistoricoResponse(null, null, null, null);
    }
}
