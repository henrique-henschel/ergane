package br.app.ergane.api.identidade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record ConfirmacaoSenhaRequest(
        @NotNull(message = "Erro! A senha é obrigatória.")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String senha
) {
    public ConfirmacaoSenhaRequest {
        if (senha != null && senha.codePointCount(0, senha.length()) > 128) {
            throw new IllegalArgumentException("Erro! A senha deve ter no máximo 128 caracteres.");
        }
    }

    @Override
    public String toString() {
        return "ConfirmacaoSenhaRequest[conteúdo protegido]";
    }
}
