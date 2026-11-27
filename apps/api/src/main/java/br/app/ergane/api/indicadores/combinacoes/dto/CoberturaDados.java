package br.app.ergane.api.indicadores.combinacoes.dto;

import java.time.LocalDate;
import java.util.List;

public record CoberturaDados(
        LocalDate inicio,
        LocalDate fim,
        Boolean temCacheCompleto,
        Long registrosElegiveis,
        Long registrosSemHora,
        List<String> limitacoes
) {

    public CoberturaDados {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new IllegalArgumentException(
                    "A data inicial da cobertura não pode ser posterior à data final.");
        }
        if (registrosElegiveis != null && registrosElegiveis < 0) {
            throw new IllegalArgumentException(
                    "A quantidade de registros elegíveis não pode ser negativa.");
        }
        if (registrosSemHora != null && registrosSemHora < 0) {
            throw new IllegalArgumentException(
                    "A quantidade de registros sem hora não pode ser negativa.");
        }
        if (limitacoes == null) {
            throw new IllegalArgumentException(
                    "A lista de limitações da cobertura não pode ser nula.");
        }
        for (String limitacao : limitacoes) {
            if (limitacao == null) {
                throw new IllegalArgumentException(
                        "A lista de limitações da cobertura não pode conter elemento nulo.");
            }
        }
        limitacoes = List.copyOf(limitacoes);
    }

}
