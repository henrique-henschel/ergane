package br.app.ergane.api.vendas.consulta.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

/**
 * guarda os filtros da lista de vendas. as datas inicial e final entram na
 * busca. a pagina comeca em zero e o tamanho padrao e 20.
 */
public record ConsultaVendasFiltro(
        LocalDate de,
        LocalDate ate,
        String origem,
        @Pattern(regexp = "^(CONFIRMADA|CANCELADA|DESFEITA)$")
        String situacao,
        UUID produtoId,
        @Pattern(regexp = "^(DINHEIRO|PIX|CARTAO_DEBITO|CARTAO_CREDITO)$")
        String formaPagamentoInformada,
        @Min(0) Integer pagina,
        @Min(1) @Max(100) Integer tamanho
) {
    @AssertTrue(message = "A data inicial deve ser anterior ou igual à data final.")
    public boolean isPeriodoValido() {
        return de == null || ate == null || !de.isAfter(ate);
    }

    /**
     * ignora origem vazia e tira os espacos extras do valor informado.
     */
    public String origemEfetiva() {
        return origem == null || origem.isBlank() ? null : origem.trim();
    }

    public int paginaEfetiva() { return pagina == null ? 0 : pagina; }
    public int tamanhoEfetivo() { return tamanho == null ? 20 : tamanho; }
}
