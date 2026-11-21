package br.app.ergane.api.catalogo.dto;

import br.app.ergane.api.catalogo.model.Categoria;

import java.time.Instant;
import java.util.UUID;

public record CategoriaResponse(UUID id, String nome, Long versao, Instant criadoEm, Instant atualizadoEm) {

    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(
            categoria.getId(),
            categoria.getNome(),
            categoria.getVersao(),
            categoria.getCriadoEm(),
            categoria.getAtualizadoEm()
        );
    }

}
