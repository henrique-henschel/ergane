package br.app.ergane.api.indicadores.combinacoes.dto;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record CombinacaoObservada(
        Set<UUID> produtoIds,
        Long ocorrencias,
        Long transacoesElegiveis,
        CoberturaDados cobertura
) {

    private static final Comparator<UUID> COMPARADOR_UUID =
            Comparator.comparing(UUID::toString);

    public CombinacaoObservada {
        if (produtoIds == null) {
            throw new IllegalArgumentException(
                    "O conjunto de produtos da combinação não pode ser nulo.");
        }
        for (UUID produtoId : produtoIds) {
            if (produtoId == null) {
                throw new IllegalArgumentException(
                        "A combinação não pode conter identificador de produto nulo.");
            }
        }
        List<UUID> ordenados = produtoIds.stream()
                .sorted(COMPARADOR_UUID)
                .toList();
        if (ordenados.size() != 2) {
            throw new IllegalArgumentException(
                    "A combinação observada deve conter exatamente dois produtos distintos.");
        }
        if (ocorrencias == null || ocorrencias <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade de ocorrências deve ser maior que zero.");
        }
        if (transacoesElegiveis == null || transacoesElegiveis < ocorrencias) {
            throw new IllegalArgumentException(
                    "A quantidade de transações elegíveis deve ser maior ou igual às ocorrências.");
        }
        if (cobertura == null) {
            throw new IllegalArgumentException(
                    "As informações de cobertura são obrigatórias.");
        }
        if (cobertura.registrosElegiveis() != null
                && !cobertura.registrosElegiveis().equals(transacoesElegiveis)) {
            throw new IllegalArgumentException(
                    "A quantidade de transações elegíveis deve corresponder aos registros elegíveis da cobertura.");
        }

        produtoIds = Collections.unmodifiableSet(new LinkedHashSet<>(ordenados));
    }

}
