package br.app.ergane.api.vendas.consulta;

import br.app.ergane.api.compartilhado.RecursoNaoEncontradoException;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ConsultaVendasServiceTests {

    private static final UUID NEGOCIO_DEV =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ConsultaVendasService service;

    // FILTRO POR PRODUTO E PRESERVAÇÃO DO HISTÓRICO

    @Test
    void filtroPorProdutoExcluiVendaSemItensEDevolveSnapshot() {

        UUID itemVendavelId = UUID.randomUUID();

        UUID vendaDetalhada = vendaComInstante(
                Instant.parse("2026-10-01T12:00:00Z"),
                "balcão"
        );

        UUID vendaSemItens = vendaComData(
                LocalDate.of(2026, 10, 1),
                "complemento"
        );

        inserirItem(
                vendaDetalhada,
                itemVendavelId,
                "Café",
                "PACOTE",
                new BigDecimal("2.000"),
                new BigDecimal("8.50")
        );

        var resultado = service.listar(
                filtro(null, null, null, null, itemVendavelId, null, 0, 20)
        );

        var detalhe = service.detalhar(vendaDetalhada);
        var vendaSemDetalhes = service.detalhar(vendaSemItens);

        assertThat(resultado.totalRegistros()).isEqualTo(1);

        assertThat(resultado.registros())
                .extracting(venda -> venda.id())
                .containsExactly(vendaDetalhada);

        assertThat(detalhe.itensIndividualizados()).isTrue();

        assertThat(detalhe.itens()).singleElement().satisfies(item -> {
            assertThat(item.descricao()).isEqualTo("Café");
            assertThat(item.unidade()).isEqualTo("PACOTE");
            assertThat(item.quantidade()).isEqualByComparingTo("2.000");
            assertThat(item.precoUnitario()).isEqualByComparingTo("8.50");
            assertThat(item.custoUnitario()).isEqualByComparingTo("5.00");
            assertThat(item.itemVendavelId()).isEqualTo(itemVendavelId);
        });

        assertThat(vendaSemDetalhes.itensIndividualizados()).isFalse();
        assertThat(vendaSemDetalhes.itens()).isEmpty();
        assertThat(vendaSemDetalhes.ocorridaEm()).isNull();
        assertThat(vendaSemDetalhes.dataOcorrencia())
                .isEqualTo(LocalDate.of(2026, 10, 1));
    }

    // FILTRO POR FORMA DE RECEBIMENTO

    @Test
    void formaDeRecebimentoNaoIncluiVendaSemRecebimento() {

        UUID semRecebimento = vendaComInstante(
                Instant.parse("2026-10-02T10:00:00Z"),
                null
        );

        UUID comRecebimento = vendaComInstante(
                Instant.parse("2026-10-02T11:00:00Z"),
                null
        );

        inserirRecebimento(
                comRecebimento,
                "PIX",
                new BigDecimal("12.00")
        );

        var resultado = service.listar(
                filtro(null, null, null, null, null, "PIX", 0, 20)
        );

        assertThat(resultado.totalRegistros()).isEqualTo(1);

        assertThat(resultado.registros())
                .extracting(venda -> venda.id())
                .containsExactly(comRecebimento);

        assertThat(service.detalhar(semRecebimento).recebimentos())
                .isEmpty();
    }

    // FILTRO POR SITUAÇÃO

    @Test
    void filtroPorSituacaoRetornaSomenteVendasCorrespondentes() {

        UUID confirmada = vendaComSituacao(
                "CONFIRMADA",
                Instant.parse("2026-10-02T10:00:00Z")
        );

        vendaComSituacao(
                "CANCELADA",
                Instant.parse("2026-10-02T11:00:00Z")
        );

        var resultado = service.listar(
                filtro(null, null, null, "CONFIRMADA", null, null, 0, 20)
        );

        assertThat(resultado.totalRegistros()).isEqualTo(1);

        assertThat(resultado.registros())
                .extracting(venda -> venda.id())
                .containsExactly(confirmada);
    }

    // PAGINAÇÃO E ORDENAÇÃO

    @Test
    void periodoEPaginacaoUsamIdComoDesempateDeterministico() {

        Instant instante = Instant.parse("2026-10-03T12:00:00Z");

        UUID menorId = new UUID(0, 10);
        UUID maiorId = new UUID(0, 20);

        inserirVenda(menorId, NEGOCIO_DEV, instante, null, null);
        inserirVenda(maiorId, NEGOCIO_DEV, instante, null, null);

        var de = OffsetDateTime.parse("2026-10-03T09:00:00-03:00");
        var ate = OffsetDateTime.parse("2026-10-03T23:59:59-03:00");

        var primeira = service.listar(
                filtro(de, ate, null, null, null, null, 0, 1)
        );

        var segunda = service.listar(
                filtro(de, ate, null, null, null, null, 1, 1)
        );

        assertThat(primeira.totalRegistros()).isEqualTo(2);

        assertThat(primeira.registros())
                .extracting(venda -> venda.id())
                .containsExactly(maiorId);

        assertThat(segunda.registros())
                .extracting(venda -> venda.id())
                .containsExactly(menorId);
    }

    @Test
    void paginaForaDoIntervaloRetornaListaVazia() {

        vendaComInstante(
                Instant.parse("2026-10-03T12:00:00Z"),
                null
        );

        var resultado = service.listar(
                filtro(null, null, null, null, null, null, 5, 20)
        );

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.totalRegistros()).isEqualTo(1);
        assertThat(resultado.pagina()).isEqualTo(5);
        assertThat(resultado.totalPaginas()).isEqualTo(1);
    }

    // ORDENAÇÃO ENTRE INSTANTES E DATAS CIVIS

    @Test
    void intercalaInstantesEDataCivilPorDiaSemCriarHorario() {

        UUID instanteMaisRecente = new UUID(0, 101);
        UUID instanteMaisAntigo = new UUID(0, 102);

        UUID dataCivilIdMenor = new UUID(0, 103);
        UUID dataCivilIdMaior = new UUID(0, 104);

        UUID instanteDoDiaAnterior = new UUID(0, 105);
        UUID dataCivilDoDiaAnterior = new UUID(0, 106);

        inserirVenda(
                instanteMaisRecente,
                NEGOCIO_DEV,
                Instant.parse("2026-10-03T12:00:00Z"),
                null,
                null
        );

        inserirVenda(
                instanteMaisAntigo,
                NEGOCIO_DEV,
                Instant.parse("2026-10-03T09:00:00Z"),
                null,
                null
        );

        inserirVenda(
                dataCivilIdMenor,
                NEGOCIO_DEV,
                null,
                LocalDate.of(2026, 10, 3),
                null
        );

        inserirVenda(
                dataCivilIdMaior,
                NEGOCIO_DEV,
                null,
                LocalDate.of(2026, 10, 3),
                null
        );

        inserirVenda(
                instanteDoDiaAnterior,
                NEGOCIO_DEV,
                Instant.parse("2026-10-02T20:00:00Z"),
                null,
                null
        );

        inserirVenda(
                dataCivilDoDiaAnterior,
                NEGOCIO_DEV,
                null,
                LocalDate.of(2026, 10, 2),
                null
        );

        var resultado = service.listar(
                filtro(null, null, null, null, null, null, 0, 20)
        );

        assertThat(resultado.registros())
                .extracting(venda -> venda.id())
                .containsExactly(
                        instanteMaisRecente,
                        instanteMaisAntigo,
                        dataCivilIdMaior,
                        dataCivilIdMenor,
                        instanteDoDiaAnterior,
                        dataCivilDoDiaAnterior
                );
    }

    // FILTRO POR PERÍODO

    @Test
    void periodoIncluiInstantesNosLimitesEDatasCivisDosDiasLimite() {

        UUID antesDoLimite = vendaComInstante(
                Instant.parse("2026-10-10T11:59:59Z"),
                null
        );

        UUID noLimiteInicial = vendaComInstante(
                Instant.parse("2026-10-10T12:00:00Z"),
                null
        );

        UUID noLimiteFinal = vendaComInstante(
                Instant.parse("2026-10-10T14:00:00Z"),
                null
        );

        UUID depoisDoLimite = vendaComInstante(
                Instant.parse("2026-10-10T14:00:01Z"),
                null
        );

        UUID dataAnterior = vendaComData(
                LocalDate.of(2026, 10, 9),
                null
        );

        UUID dataNoDiaLimite = vendaComData(
                LocalDate.of(2026, 10, 10),
                null
        );

        UUID dataPosterior = vendaComData(
                LocalDate.of(2026, 10, 11),
                null
        );

        var de = OffsetDateTime.parse("2026-10-10T09:00:00-03:00");
        var ate = OffsetDateTime.parse("2026-10-10T11:00:00-03:00");

        var resultado = service.listar(
                filtro(de, ate, null, null, null, null, 0, 20)
        );

        assertThat(resultado.totalRegistros()).isEqualTo(3);

        assertThat(resultado.registros())
                .extracting(venda -> venda.id())
                .containsExactly(
                        noLimiteFinal,
                        noLimiteInicial,
                        dataNoDiaLimite
                );

        assertThat(resultado.registros())
                .extracting(venda -> venda.id())
                .doesNotContain(
                        antesDoLimite,
                        depoisDoLimite,
                        dataAnterior,
                        dataPosterior
                );
    }

    // ISOLAMENTO ENTRE NEGÓCIOS

    @Test
    void vendaDeOutroNegocioNaoApareceNaListagem() {

        UUID outroNegocio = criarOutroNegocio();

        UUID vendaAlheia = UUID.randomUUID();

        UUID vendaPropria = vendaComInstante(
                Instant.parse("2026-10-04T12:00:00Z"),
                null
        );

        inserirVenda(
                vendaAlheia,
                outroNegocio,
                Instant.parse("2026-10-04T13:00:00Z"),
                null,
                null
        );

        var resultado = service.listar(
                filtro(null, null, null, null, null, null, 0, 20)
        );

        assertThat(resultado.registros())
                .extracting(venda -> venda.id())
                .containsExactly(vendaPropria);

        assertThat(resultado.registros())
                .extracting(venda -> venda.id())
                .doesNotContain(vendaAlheia);
    }

    @Test
    void vendaDeOutroNegocioETratadaComoInexistente() {

        UUID outroNegocio = criarOutroNegocio();

        UUID vendaAlheia = UUID.randomUUID();

        inserirVenda(
                vendaAlheia,
                outroNegocio,
                Instant.parse("2026-10-04T12:00:00Z"),
                null,
                null
        );

        assertThatThrownBy(() -> service.detalhar(vendaAlheia))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Erro! Venda não encontrada.");

        assertThatThrownBy(() -> service.detalhar(UUID.randomUUID()))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Erro! Venda não encontrada.");
    }

    // CONSULTA SEM RESULTADOS

    @Test
    void consultaSemResultadosRetornaListaVazia() {

        var resultado = service.listar(
                filtro(
                        null,
                        null,
                        "origem-inexistente",
                        null,
                        null,
                        null,
                        0,
                        20
                )
        );

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.totalRegistros()).isZero();
        assertThat(resultado.totalPaginas()).isZero();
    }

    // CONSULTA DOS RECEBIMENTOS

    @Test
    void detalhesRetornamDadosDosRecebimentos() {

        UUID vendaId = vendaComInstante(
                Instant.parse("2026-10-05T12:00:00Z"),
                null
        );

        Instant instanteRecebimento =
                Instant.parse("2026-10-05T13:00:00Z");

        inserirRecebimento(
                vendaId,
                "PIX",
                new BigDecimal("12.00"),
                instanteRecebimento
        );

        var detalhe = service.detalhar(vendaId);

        assertThat(detalhe.recebimentos()).singleElement().satisfies(recebimento -> {
            assertThat(recebimento.forma()).isEqualTo("PIX");
            assertThat(recebimento.valor()).isEqualByComparingTo("12.00");
            assertThat(recebimento.recebidoEm())
                    .isEqualTo(instanteRecebimento);
        });
    }

    // ORIGEM E COBERTURA HISTÓRICA

    @Test
    void origemEhFiltradaComoTextoEApiNaoAfirmaCoberturaCompleta() {

        inserirVenda(
                UUID.randomUUID(),
                NEGOCIO_DEV,
                Instant.parse("2026-10-05T12:00:00Z"),
                null,
                "balcão"
        );

        inserirVenda(
                UUID.randomUUID(),
                NEGOCIO_DEV,
                Instant.parse("2026-10-05T13:00:00Z"),
                null,
                "WhatsApp"
        );

        var resultado = service.listar(
                filtro(null, null, "WhatsApp", null, null, null, 0, 20)
        );

        assertThat(resultado.totalRegistros()).isEqualTo(1);

        assertThat(resultado.cobertura().completa()).isNull();
        assertThat(resultado.cobertura().atualizadoEm()).isNull();
    }

    // MÉTODOS AUXILIARES

    private UUID vendaComInstante(Instant instante, String origem) {

        UUID id = UUID.randomUUID();

        inserirVenda(id, NEGOCIO_DEV, instante, null, origem);

        return id;
    }

    private UUID vendaComData(LocalDate data, String origem) {

        UUID id = UUID.randomUUID();

        inserirVenda(id, NEGOCIO_DEV, null, data, origem);

        return id;
    }

    private UUID vendaComSituacao(String situacao, Instant instante) {

        UUID id = UUID.randomUUID();

        inserirVenda(
                id,
                NEGOCIO_DEV,
                instante,
                null,
                null,
                situacao
        );

        return id;
    }

    private UUID criarOutroNegocio() {

        UUID id = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO negocios (id, nome) VALUES (?, ?)",
                id,
                "Outro negócio"
        );

        return id;
    }

    private void inserirVenda(
            UUID id,
            UUID negocioId,
            Instant instante,
            LocalDate data,
            String origem
    ) {

        inserirVenda(
                id,
                negocioId,
                instante,
                data,
                origem,
                "CONFIRMADA"
        );
    }

    private void inserirVenda(
            UUID id,
            UUID negocioId,
            Instant instante,
            LocalDate data,
            String origem,
            String situacao
    ) {

        jdbcTemplate.update("""
                INSERT INTO vendas
                    (id, negocio_id, registrada_em, data_ocorrencia,
                     total, origem, situacao)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                negocioId,
                instante == null
                        ? null
                        : OffsetDateTime.ofInstant(instante, ZoneOffset.UTC),
                data,
                new BigDecimal("12.00"),
                origem,
                situacao
        );
    }

    private void inserirItem(
            UUID vendaId,
            UUID itemVendavelId,
            String descricao,
            String unidade,
            BigDecimal quantidade,
            BigDecimal precoUnitario
    ) {

        jdbcTemplate.update("""
                INSERT INTO itens_venda
                    (id, venda_id, item_vendavel_id,
                     descricao_snapshot, unidade_snapshot,
                     quantidade, preco_unitario, custo_unitario)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                vendaId,
                itemVendavelId,
                descricao,
                unidade,
                quantidade,
                precoUnitario,
                new BigDecimal("5.00")
        );
    }

    private void inserirRecebimento(
            UUID vendaId,
            String forma,
            BigDecimal valor
    ) {

        inserirRecebimento(
                vendaId,
                forma,
                valor,
                Instant.parse("2026-10-02T12:00:00Z")
        );
    }

    private void inserirRecebimento(
            UUID vendaId,
            String forma,
            BigDecimal valor,
            Instant instante
    ) {

        jdbcTemplate.update("""
                INSERT INTO recebimentos
                    (id, venda_id, forma, valor, recebido_em)
                VALUES (?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                vendaId,
                forma,
                valor,
                OffsetDateTime.ofInstant(instante, ZoneOffset.UTC)
        );
    }

    private ConsultaVendasFiltro filtro(
            OffsetDateTime de,
            OffsetDateTime ate,
            String origem,
            String situacao,
            UUID produtoId,
            String forma,
            Integer pagina,
            Integer tamanho
    ) {

        return new ConsultaVendasFiltro(
                de,
                ate,
                origem,
                situacao,
                produtoId,
                forma,
                pagina,
                tamanho
        );
    }
}
