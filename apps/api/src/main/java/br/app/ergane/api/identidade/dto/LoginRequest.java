package br.app.ergane.api.identidade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotBlank(message = "Erro! O email é obrigatório.")
        @Email(message = "Erro! Informe um email válido.") String email,
        @NotNull(message = "Erro! A senha é obrigatória.")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String senha
) {
    public LoginRequest {
        email = email == null ? null : email.trim();
        if (senha != null && senha.codePointCount(0, senha.length()) > 128) {
            throw new IllegalArgumentException("Erro! A senha deve ter no máximo 128 caracteres.");
        }
    }

    @Override
    public String toString() {
        return "LoginRequest[conteúdo protegido]";
    }
}
