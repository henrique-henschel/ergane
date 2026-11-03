
package br.app.ergane.api.vendas.consulta;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConsultaVendasFiltro(

        OffsetDateTime de,

        OffsetDateTime ate,

        // os valores permitidos ainda serão definidos futuramente pelo projeto, a decidir com a equipe...
        String origem,

        @Pattern(
                regexp = "^(CONFIRMADA|CANCELADA|DESFEITA)$",
                message = "Erro! Situação deve ser CONFIRMADA, CANCELADA ou DESFEITA."
        )
        String situacao,

        UUID produtoId,

        @Pattern(
                regexp = "^(DINHEIRO|PIX|CARTAO_DEBITO|CARTAO_CREDITO)$",
                message = "Erro! Forma de recebimento inválida."
        )
        String formaRecebimento,

        @Min(
                value = 0,
                message = "Erro! A página deve ser zero ou maior."
        )
        Integer pagina,

        @Min(
                value = 1,
                message = "Erro! O tamanho deve ser maior que zero."
        )
        @Max(
                value = 100,
                message = "Erro! O tamanho máximo é 100."
        )
        Integer tamanho

) {

    @AssertTrue(
            message = "Erro! A data inicial deve ser anterior ou igual à data final."
    )
    public boolean isPeriodoValido() {
        return de == null || ate == null || !de.isAfter(ate);
    }

    public int paginaEfetiva() {
        return pagina == null ? 0 : pagina;
    }

    public int tamanhoEfetivo() {
        return tamanho == null ? 20 : tamanho;
    }
}
