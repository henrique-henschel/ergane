
package br.app.ergane.api.vendas.consulta;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ConsultaVendasFiltroTests {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void configurarValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fecharValidator() {
        if (factory != null) {
            factory.close();
        }
    }

    @Test
    void rejeitaSituacaoForaDosValoresAprovados() {
        var filtro = filtro("CONCLUIDA", null, null, null);

        assertThat(propriedadesInvalidas(filtro))
                .contains("situacao");
    }

    @Test
    void rejeitaFormaDeRecebimentoForaDosValoresAprovados() {
        var filtro = filtro(null, "OUTRO", null, null);

        assertThat(propriedadesInvalidas(filtro))
                .contains("formaRecebimento");
    }

    @Test
    void aceitaSituacoesAprovadas() {
        for (String situacao : Set.of(
                "CONFIRMADA",
                "CANCELADA",
                "DESFEITA"
        )) {
            assertThat(propriedadesInvalidas(
                    filtro(situacao, null, null, null)
            ))
                    .as("situação %s", situacao)
                    .isEmpty();
        }
    }

    @Test
    void aceitaFormasDeRecebimentoAprovadasSemOutro() {
        for (String forma : Set.of(
                "DINHEIRO",
                "PIX",
                "CARTAO_DEBITO",
                "CARTAO_CREDITO"
        )) {
            assertThat(propriedadesInvalidas(
                    filtro(null, forma, null, null)
            ))
                    .as("forma %s", forma)
                    .isEmpty();
        }
    }

    @Test
    void validaPeriodoInicialEFinalPreservandoLimitesInclusivos() {
        var inicio = OffsetDateTime.parse(
                "2026-10-10T09:00:00-03:00"
        );

        var fim = OffsetDateTime.parse(
                "2026-10-10T11:00:00-03:00"
        );

        // Período invertido deve ser rejeitado.
        assertThat(propriedadesInvalidas(
                filtro(null, null, fim, inicio)
        ))
                .contains("periodoValido");

        // Datas iguais devem ser aceitas.
        assertThat(propriedadesInvalidas(
                filtro(null, null, inicio, inicio)
        ))
                .isEmpty();

        // Data inicial anterior à final deve ser aceita.
        assertThat(propriedadesInvalidas(
                filtro(null, null, inicio, fim)
        ))
                .isEmpty();
    }

    @Test
    void mantemOrigemComoTextoLivreEOsPadroesDePaginacao() {
        var filtro = new ConsultaVendasFiltro(
                null,
                null,
                "origem que ainda não tem enum",
                null,
                null,
                null,
                null,
                null
        );

        assertThat(propriedadesInvalidas(filtro))
                .isEmpty();

        assertThat(filtro.paginaEfetiva())
                .isZero();

        assertThat(filtro.tamanhoEfetivo())
                .isEqualTo(20);
    }

    @Test
    void rejeitaPaginaNegativa() {
        var filtro = new ConsultaVendasFiltro(
                null, null, null, null,
                null, null, -1, 20
        );

        assertThat(propriedadesInvalidas(filtro))
                .contains("pagina");
    }

    @Test
    void rejeitaTamanhoDePaginaIgualAZero() {
        var filtro = new ConsultaVendasFiltro(
                null, null, null, null,
                null, null, 0, 0
        );

        assertThat(propriedadesInvalidas(filtro))
                .contains("tamanho");
    }

    @Test
    void rejeitaTamanhoDePaginaAcimaDoLimite() {
        var filtro = new ConsultaVendasFiltro(
                null, null, null, null,
                null, null, 0, 101
        );

        assertThat(propriedadesInvalidas(filtro))
                .contains("tamanho");
    }

    private static Set<String> propriedadesInvalidas(
            ConsultaVendasFiltro filtro
    ) {
        return validator.validate(filtro).stream()
                .map(violacao ->
                        violacao.getPropertyPath().toString()
                )
                .collect(Collectors.toSet());
    }

    private static ConsultaVendasFiltro filtro(
            String situacao,
            String forma,
            OffsetDateTime de,
            OffsetDateTime ate
    ) {
        return new ConsultaVendasFiltro(
                de,
                ate,
                null,
                situacao,
                null,
                forma,
                0,
                20
        );
    }
}
