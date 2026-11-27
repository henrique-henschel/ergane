package br.app.ergane.api.indicadores.combinacoes;

import br.app.ergane.api.indicadores.combinacoes.dto.CoberturaDados;
import br.app.ergane.api.indicadores.combinacoes.dto.CombinacaoObservada;
import br.app.ergane.api.indicadores.combinacoes.dto.TransacaoDetalhada;
import br.app.ergane.api.indicadores.combinacoes.service.CalculadorCombinacoesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculadorCombinacoesServiceTest {

    private static final UUID PRODUTO_A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID PRODUTO_B = UUID.fromString("00000000-0000-0000-0000-00000000000b");
    private static final UUID PRODUTO_C = UUID.fromString("00000000-0000-0000-0000-00000000000c");
    private static final UUID PRODUTO_D = UUID.fromString("00000000-0000-0000-0000-00000000000d");

    private CalculadorCombinacoesService service;

    @BeforeEach
    void setUp() {
        service = new CalculadorCombinacoesService();
    }

    private CoberturaDados coberturaComRegistros(Long quantidade) {
        return new CoberturaDados(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 30),
                true,
                quantidade,
                0L,
                List.of()
        );
    }

    @Test
    @DisplayName("Deve identificar par de produtos presentes na mesma venda")
    void deveIdentificarParDeProdutosNaMesmaVenda() {
        List<TransacaoDetalhada> transacoes = List.of(
                new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_A, PRODUTO_B))
        );
        CoberturaDados cobertura = coberturaComRegistros(1L);

        List<CombinacaoObservada> resultado = service.calcular(transacoes, null, cobertura);

        assertThat(resultado).hasSize(1);
        CombinacaoObservada combinacao = resultado.getFirst();
        assertThat(combinacao.produtoIds()).containsExactly(PRODUTO_A, PRODUTO_B);
        assertThat(combinacao.ocorrencias()).isEqualTo(1L);
        assertThat(combinacao.transacoesElegiveis()).isEqualTo(1L);
        assertThat(combinacao.cobertura()).isEqualTo(cobertura);
    }

    @Test
    @DisplayName("CA1: Não deve combinar produtos presentes somente em vendas diferentes")
    void naoDeveCombinarProdutosEmVendasDiferentes() {
        List<TransacaoDetalhada> transacoes = List.of(
                new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_A)),
                new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_B))
        );

        List<CombinacaoObservada> resultado = service.calcular(
                transacoes,
                null,
                coberturaComRegistros(2L)
        );

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("CA2: Deve retornar 3 ocorrências e denominador 10 quando par aparece em 3 de 10 transações elegíveis")
    void deveRetornarTresOcorrenciasEmDezTransacoesElegiveis() {
        List<TransacaoDetalhada> transacoes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            transacoes.add(new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_A, PRODUTO_B)));
        }
        for (int i = 0; i < 7; i++) {
            transacoes.add(new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_C)));
        }

        List<CombinacaoObservada> resultado = service.calcular(
                transacoes,
                null,
                coberturaComRegistros(10L)
        );

        assertThat(resultado).hasSize(1);
        CombinacaoObservada combinacao = resultado.getFirst();
        assertThat(combinacao.produtoIds()).containsExactly(PRODUTO_A, PRODUTO_B);
        assertThat(combinacao.ocorrencias()).isEqualTo(3L);
        assertThat(combinacao.transacoesElegiveis()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Não deve inflar ocorrências quando o mesmo produto aparece repetido na mesma venda")
    void naoDeveInflarOcorrenciasComProdutoRepetidoNaMesmaVenda() {
        List<TransacaoDetalhada> transacoes = List.of(
                new TransacaoDetalhada(
                        UUID.randomUUID(),
                        List.of(PRODUTO_A, PRODUTO_A, PRODUTO_B, PRODUTO_B, PRODUTO_A)
                )
        );

        List<CombinacaoObservada> resultado = service.calcular(
                transacoes,
                null,
                coberturaComRegistros(1L)
        );

        assertThat(resultado).hasSize(1);
        CombinacaoObservada combinacao = resultado.getFirst();
        assertThat(combinacao.produtoIds()).containsExactly(PRODUTO_A, PRODUTO_B);
        assertThat(combinacao.ocorrencias()).isEqualTo(1L);
        assertThat(combinacao.transacoesElegiveis()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Não deve formar par de um produto consigo mesmo")
    void naoDeveFormarParDeProdutoConsigoMesmo() {
        List<TransacaoDetalhada> transacoes = List.of(
                new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_A, PRODUTO_A, PRODUTO_A))
        );

        List<CombinacaoObservada> resultado = service.calcular(
                transacoes,
                null,
                coberturaComRegistros(1L)
        );

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve aplicar filtro opcional por produto preservando o denominador geral de transações elegíveis")
    void deveFiltrarPorProdutoPreservandoDenominadorGeral() {
        List<TransacaoDetalhada> transacoes = List.of(
                new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_A, PRODUTO_B, PRODUTO_C)),
                new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_B, PRODUTO_C)),
                new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_D))
        );

        List<CombinacaoObservada> resultado = service.calcular(
                transacoes,
                PRODUTO_A,
                coberturaComRegistros(3L)
        );

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(CombinacaoObservada::produtoIds)
                .containsExactly(
                        Set.of(PRODUTO_A, PRODUTO_B),
                        Set.of(PRODUTO_A, PRODUTO_C)
                );
        assertThat(resultado)
                .extracting(CombinacaoObservada::transacoesElegiveis)
                .containsOnly(3L);
    }

    @Test
    @DisplayName("Deve manter transações elegíveis com apenas um ou nenhum produto identificado no denominador")
    void deveManterTransacoesComUmOuNenhumProdutoNoDenominador() {
        List<TransacaoDetalhada> transacoes = List.of(
                new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_A, PRODUTO_B)),
                new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_C)),
                new TransacaoDetalhada(UUID.randomUUID(), List.of())
        );

        List<CombinacaoObservada> resultado = service.calcular(
                transacoes,
                null,
                coberturaComRegistros(3L)
        );

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().ocorrencias()).isEqualTo(1L);
        assertThat(resultado.getFirst().transacoesElegiveis()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver transações elegíveis sem erro de divisão por zero")
    void deveRetornarListaVaziaQuandoNaoHouverTransacoesElegiveis() {
        List<CombinacaoObservada> resultado = service.calcular(
                List.of(),
                null,
                coberturaComRegistros(0L)
        );

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve aceitar cobertura com registrosElegiveis nulo")
    void deveAceitarCoberturaComRegistrosElegiveisNulo() {
        List<TransacaoDetalhada> transacoes = List.of(
                new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_A, PRODUTO_B))
        );

        List<CombinacaoObservada> resultado = service.calcular(
                transacoes,
                null,
                coberturaComRegistros(null)
        );

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().transacoesElegiveis()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve ordenar resultados por maior quantidade de ocorrências e desempatar pelos UUIDs dos produtos")
    void deveOrdenarResultadosDeFormaDeterministica() {
        List<TransacaoDetalhada> transacoes = List.of(
                new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_C, PRODUTO_D)),
                new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_B, PRODUTO_C)),
                new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_B, PRODUTO_C)),
                new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_A, PRODUTO_D))
        );

        List<CombinacaoObservada> resultado = service.calcular(
                transacoes,
                null,
                coberturaComRegistros(4L)
        );

        assertThat(resultado).hasSize(3);
        assertThat(resultado.get(0).produtoIds()).containsExactly(PRODUTO_B, PRODUTO_C);
        assertThat(resultado.get(0).ocorrencias()).isEqualTo(2L);

        assertThat(resultado.get(1).produtoIds()).containsExactly(PRODUTO_A, PRODUTO_D);
        assertThat(resultado.get(1).ocorrencias()).isEqualTo(1L);

        assertThat(resultado.get(2).produtoIds()).containsExactly(PRODUTO_C, PRODUTO_D);
        assertThat(resultado.get(2).ocorrencias()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve rejeitar transações com identificador de venda duplicado")
    void deveRejeitarIdentificadoresDeVendaDuplicados() {
        UUID vendaDuplicadaId = UUID.randomUUID();
        List<TransacaoDetalhada> transacoes = List.of(
                new TransacaoDetalhada(vendaDuplicadaId, List.of(PRODUTO_A, PRODUTO_B)),
                new TransacaoDetalhada(vendaDuplicadaId, List.of(PRODUTO_B, PRODUTO_C))
        );

        assertThatThrownBy(() -> service.calcular(transacoes, null, coberturaComRegistros(2L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identificador de venda duplicado");
    }

    @Test
    @DisplayName("Deve rejeitar divergência entre cobertura.registrosElegiveis e quantidade de transações")
    void deveRejeitarDivergenciaEntreCoberturaEQuantidadeDeTransacoes() {
        List<TransacaoDetalhada> transacoes = List.of(
                new TransacaoDetalhada(UUID.randomUUID(), List.of(PRODUTO_A, PRODUTO_B))
        );

        assertThatThrownBy(() -> service.calcular(transacoes, null, coberturaComRegistros(10L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("registros elegíveis da cobertura deve corresponder");
    }

    @Test
    @DisplayName("Deve rejeitar divergência entre cobertura.registrosElegiveis e lista vazia de transações")
    void deveRejeitarDivergenciaEntreCoberturaEListaVaziaDeTransacoes() {
        assertThatThrownBy(() -> service.calcular(List.of(), null, coberturaComRegistros(5L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("registros elegíveis da cobertura deve corresponder");
    }

    @Test
    @DisplayName("Deve rejeitar entradas nulas no CalculadorCombinacoesService")
    void deveRejeitarEntradasNulasNoCalculador() {
        assertThatThrownBy(() -> service.calcular(null, null, coberturaComRegistros(0L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lista de transações elegíveis não pode ser nula");

        assertThatThrownBy(() -> service.calcular(List.of(), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("informações de cobertura são obrigatórias");

        List<TransacaoDetalhada> listaComTransacaoNula = Arrays.asList((TransacaoDetalhada) null);
        assertThatThrownBy(() -> service.calcular(listaComTransacaoNula, null, coberturaComRegistros(1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode conter transação nula");
    }

    @Test
    @DisplayName("Deve validar datas, contagens e limitações em CoberturaDados")
    void deveValidarCoberturaDados() {
        assertThatThrownBy(() -> new CoberturaDados(
                LocalDate.of(2026, 10, 30),
                LocalDate.of(2026, 10, 1),
                true,
                1L,
                0L,
                List.of()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data inicial da cobertura não pode ser posterior");

        assertThatThrownBy(() -> new CoberturaDados(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 30),
                true,
                -1L,
                0L,
                List.of()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("registros elegíveis não pode ser negativa");

        assertThatThrownBy(() -> new CoberturaDados(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 30),
                true,
                1L,
                -1L,
                List.of()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("registros sem hora não pode ser negativa");

        assertThatThrownBy(() -> new CoberturaDados(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 30),
                true,
                1L,
                0L,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lista de limitações da cobertura não pode ser nula");

        List<String> limitacoesComNulo = Arrays.asList((String) null);
        assertThatThrownBy(() -> new CoberturaDados(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 30),
                true,
                1L,
                0L,
                limitacoesComNulo
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode conter elemento nulo");
    }

    @Test
    @DisplayName("Deve validar produtos, ocorrências, denominador e cobertura em CombinacaoObservada")
    void deveValidarCombinacaoObservada() {
        CoberturaDados coberturaDoisRegistros = coberturaComRegistros(2L);

        assertThatThrownBy(() -> new CombinacaoObservada(null, 1L, 2L, coberturaDoisRegistros))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("conjunto de produtos da combinação não pode ser nulo");

        Set<UUID> conjuntoComNulo = new HashSet<>(Arrays.asList(PRODUTO_A, null));
        assertThatThrownBy(() -> new CombinacaoObservada(conjuntoComNulo, 1L, 2L, coberturaDoisRegistros))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identificador de produto nulo");

        assertThatThrownBy(() -> new CombinacaoObservada(Set.of(PRODUTO_A), 1L, 2L, coberturaDoisRegistros))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exatamente dois produtos distintos");

        assertThatThrownBy(() -> new CombinacaoObservada(
                Set.of(PRODUTO_A, PRODUTO_B, PRODUTO_C), 1L, 2L, coberturaDoisRegistros))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exatamente dois produtos distintos");

        assertThatThrownBy(() -> new CombinacaoObservada(
                Set.of(PRODUTO_A, PRODUTO_B), 0L, 2L, coberturaDoisRegistros))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ocorrências deve ser maior que zero");

        assertThatThrownBy(() -> new CombinacaoObservada(
                Set.of(PRODUTO_A, PRODUTO_B), 3L, 2L, coberturaDoisRegistros))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("transações elegíveis deve ser maior ou igual às ocorrências");

        assertThatThrownBy(() -> new CombinacaoObservada(
                Set.of(PRODUTO_A, PRODUTO_B), 1L, 2L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("informações de cobertura são obrigatórias");

        assertThatThrownBy(() -> new CombinacaoObservada(
                Set.of(PRODUTO_A, PRODUTO_B), 1L, 2L, coberturaComRegistros(10L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deve corresponder aos registros elegíveis da cobertura");
    }

    @Test
    @DisplayName("Deve validar vendaId, produtoIds e imutabilidade em TransacaoDetalhada e CombinacaoObservada")
    void deveValidarTransacaoDetalhadaEImutabilidade() {
        assertThatThrownBy(() -> new TransacaoDetalhada(null, List.of(PRODUTO_A)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identificador da venda é obrigatório");

        assertThatThrownBy(() -> new TransacaoDetalhada(UUID.randomUUID(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("coleção de produtos da transação não pode ser nula");

        List<UUID> produtosComNulo = Arrays.asList(PRODUTO_A, null);
        assertThatThrownBy(() -> new TransacaoDetalhada(UUID.randomUUID(), produtosComNulo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identificador de produto nulo");

        List<UUID> listaMutavel = new ArrayList<>(List.of(PRODUTO_B, PRODUTO_A));
        TransacaoDetalhada transacao = new TransacaoDetalhada(UUID.randomUUID(), listaMutavel);
        listaMutavel.add(PRODUTO_C);

        assertThat(transacao.produtoIds()).containsExactly(PRODUTO_B, PRODUTO_A);
        assertThatThrownBy(() -> transacao.produtoIds().add(PRODUTO_C))
                .isInstanceOf(UnsupportedOperationException.class);

        CombinacaoObservada combinacao = new CombinacaoObservada(
                Set.of(PRODUTO_B, PRODUTO_A),
                1L,
                2L,
                coberturaComRegistros(2L)
        );
        assertThat(combinacao.produtoIds()).containsExactly(PRODUTO_A, PRODUTO_B);
        assertThatThrownBy(() -> combinacao.produtoIds().add(PRODUTO_C))
                .isInstanceOf(UnsupportedOperationException.class);
    }

}
