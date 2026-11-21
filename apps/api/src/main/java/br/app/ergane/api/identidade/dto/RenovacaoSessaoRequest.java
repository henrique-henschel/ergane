package br.app.ergane.api.identidade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RenovacaoSessaoRequest(
        @NotNull(message = "Erro! O refresh token é obrigatório.")
        @Pattern(regexp = "[A-Za-z0-9_-]{43}", message = "Erro! O refresh token é inválido.")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String refreshToken
) {
    @Override
    public String toString() {
        return "RenovacaoSessaoRequest[conteúdo protegido]";
    }
}
