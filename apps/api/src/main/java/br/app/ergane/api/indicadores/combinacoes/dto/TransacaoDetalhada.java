package br.app.ergane.api.indicadores.combinacoes.dto;

import java.util.List;
import java.util.UUID;

public record TransacaoDetalhada(
        UUID vendaId,
        List<UUID> produtoIds
) {

    public TransacaoDetalhada {
        if (vendaId == null) {
            throw new IllegalArgumentException(
                    "O identificador da venda é obrigatório.");
        }
        if (produtoIds == null) {
            throw new IllegalArgumentException(
                    "A coleção de produtos da transação não pode ser nula.");
        }
        for (UUID produtoId : produtoIds) {
            if (produtoId == null) {
                throw new IllegalArgumentException(
                        "A transação não pode conter identificador de produto nulo.");
            }
        }
        produtoIds = List.copyOf(produtoIds);
    }

}
