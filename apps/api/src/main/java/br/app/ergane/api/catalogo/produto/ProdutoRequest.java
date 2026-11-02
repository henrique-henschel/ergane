package br.app.ergane.api.catalogo.produto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProdutoRequest(

        @NotBlank(message = "Erro! O nome é obrigatório.")
        @Size(max = 100, message = "Erro! O nome deve ter no máximo 100 caracteres.")
        String nome,

        @Size(max = 50, message = "Erro! O diferenciador deve ter no máximo 50 caracteres.")
        String diferenciador,

        @Size(max = 255, message = "Erro! A descrição deve ter no máximo 255 caracteres.")
        String descricao,

        @NotNull(message = "Erro! O preço é obrigatório.")
        @DecimalMin(value = "0.00", message = "Erro! O preço não pode ser negativo.")
        @Digits(integer = 10, fraction = 2, message = "Erro! Preço inválido: até 10 dígitos inteiros e 2 casas decimais.")
        BigDecimal preco,

        @DecimalMin(value = "0.00", message = "Erro! O custo não pode ser negativo.")
        @Digits(integer = 10, fraction = 2, message = "Erro! Custo inválido: até 10 dígitos inteiros e 2 casas decimais.")
        BigDecimal custo,

        LocalDate validade,

        UUID categoriaId

) {}
