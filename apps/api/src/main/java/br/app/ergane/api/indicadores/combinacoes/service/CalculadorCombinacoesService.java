package br.app.ergane.api.indicadores.combinacoes.service;

import br.app.ergane.api.indicadores.combinacoes.dto.CoberturaDados;
import br.app.ergane.api.indicadores.combinacoes.dto.CombinacaoObservada;
import br.app.ergane.api.indicadores.combinacoes.dto.TransacaoDetalhada;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class CalculadorCombinacoesService {

    private static final Comparator<UUID> COMPARADOR_UUID =
            Comparator.comparing(UUID::toString);

    private static final Comparator<Map.Entry<ParProdutos, Long>> COMPARADOR_RESULTADO =
            Map.Entry.<ParProdutos, Long>comparingByValue(Comparator.reverseOrder())
                    .thenComparing(entry -> entry.getKey().primeiro(), COMPARADOR_UUID)
                    .thenComparing(entry -> entry.getKey().segundo(), COMPARADOR_UUID);

    public List<CombinacaoObservada> calcular(
            List<TransacaoDetalhada> transacoesElegiveis,
            UUID produtoFiltroId,
            CoberturaDados cobertura
    ) {
        validarEntrada(transacoesElegiveis, cobertura);

        long totalTransacoesElegiveis = transacoesElegiveis.size();
        if (totalTransacoesElegiveis == 0) {
            return List.of();
        }

        Map<ParProdutos, Long> ocorrenciasPorPar = new HashMap<>();

        for (TransacaoDetalhada transacao : transacoesElegiveis) {
            List<UUID> produtosDistintos = transacao.produtoIds().stream()
                    .distinct()
                    .sorted(COMPARADOR_UUID)
                    .toList();

            int quantidadeDistintos = produtosDistintos.size();
            if (quantidadeDistintos < 2) {
                continue;
            }

            for (int i = 0; i < quantidadeDistintos - 1; i++) {
                UUID primeiro = produtosDistintos.get(i);
                for (int j = i + 1; j < quantidadeDistintos; j++) {
                    UUID segundo = produtosDistintos.get(j);
                    ParProdutos par = new ParProdutos(primeiro, segundo);

                    if (produtoFiltroId != null && !par.contem(produtoFiltroId)) {
                        continue;
                    }

                    ocorrenciasPorPar.merge(par, 1L, Long::sum);
                }
            }
        }

        if (ocorrenciasPorPar.isEmpty()) {
            return List.of();
        }

        return ocorrenciasPorPar.entrySet().stream()
                .sorted(COMPARADOR_RESULTADO)
                .map(entry -> new CombinacaoObservada(
                        entry.getKey().comoConjuntoImutavel(),
                        entry.getValue(),
                        totalTransacoesElegiveis,
                        cobertura
                ))
                .toList();
    }

    private void validarEntrada(
            List<TransacaoDetalhada> transacoesElegiveis,
            CoberturaDados cobertura
    ) {
        if (transacoesElegiveis == null) {
            throw new IllegalArgumentException(
                    "A lista de transações elegíveis não pode ser nula.");
        }
        if (cobertura == null) {
            throw new IllegalArgumentException(
                    "As informações de cobertura são obrigatórias.");
        }

        Set<UUID> vendasProcessadas = new HashSet<>();
        for (TransacaoDetalhada transacao : transacoesElegiveis) {
            if (transacao == null) {
                throw new IllegalArgumentException(
                        "A lista de transações elegíveis não pode conter transação nula.");
            }
            if (!vendasProcessadas.add(transacao.vendaId())) {
                throw new IllegalArgumentException(
                        "Não são permitidas transações com identificador de venda duplicado.");
            }
        }

        if (cobertura.registrosElegiveis() != null
                && cobertura.registrosElegiveis() != transacoesElegiveis.size()) {
            throw new IllegalArgumentException(
                    "A quantidade de registros elegíveis da cobertura deve corresponder à quantidade de transações elegíveis.");
        }
    }

    private record ParProdutos(UUID primeiro, UUID segundo) {

        boolean contem(UUID produtoId) {
            return primeiro.equals(produtoId) || segundo.equals(produtoId);
        }

        Set<UUID> comoConjuntoImutavel() {
            LinkedHashSet<UUID> conjunto = new LinkedHashSet<>(2);
            conjunto.add(primeiro);
            conjunto.add(segundo);
            return Collections.unmodifiableSet(conjunto);
        }

    }

}
