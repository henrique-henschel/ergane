package br.app.ergane.api.identidade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CadastroRequest(
        @NotBlank(message = "Erro! O nome é obrigatório.")
        String nome,
        @NotBlank(message = "Erro! O email é obrigatório.")
        @Email(message = "Erro! Informe um email válido.")
        String email,
        @NotNull(message = "Erro! A senha é obrigatória.")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String senha,
        String nomeConta
) {

    public CadastroRequest {
        email = email == null ? null : email.trim();
        int tamanho = senha.codePointCount(0, senha.length());
        if (tamanho < 8 || tamanho > 128) {
            throw new IllegalArgumentException("Erro! A senha deve ter de 8 a 128 caracteres.");
        }
        if (nomeConta != null && (nomeConta.isBlank()
                || nomeConta.codePointCount(0, nomeConta.length()) > 100)) {
            throw new IllegalArgumentException("Erro! O nome do negócio deve ser preenchido e ter no máximo 100 caracteres.");
        }
    }

    @Override
    public String toString() {
        return "CadastroRequest[conteúdo protegido]";
    }

}
