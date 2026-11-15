package br.app.ergane.api.identidade.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SelecionarNegocioRequest(
        @NotNull(message = "Erro! O identificador do negócio é obrigatório.") UUID negocioId
) {
}
