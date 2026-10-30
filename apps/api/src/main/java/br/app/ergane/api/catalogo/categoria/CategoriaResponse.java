package br.app.ergane.api.catalogo.categoria;

import java.time.Instant;
import java.util.UUID;

public record CategoriaResponse(UUID id, String nome, Instant criadoEm, Instant atualizadoEm) {

    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(
            categoria.getId(),
            categoria.getNome(),
            categoria.getCriadoEm(),
            categoria.getAtualizadoEm()
        );
    }

}
