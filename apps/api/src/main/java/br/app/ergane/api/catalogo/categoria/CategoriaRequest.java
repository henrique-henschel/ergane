package br.app.ergane.api.catalogo.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
    @NotBlank(message = "Erro! O nome é obrigatório.")
    @Size(max = 100, message = "Erro! O nome deve ter no máximo 100 caracteres.")
    String nome
) {}
