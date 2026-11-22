package br.app.ergane.api.identidade.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SelecionarContaRequest(
        @NotNull(message = "Erro! O identificador do negócio é obrigatório.") UUID contaId
) {
}
