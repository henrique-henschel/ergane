package br.app.ergane.api.identidade.dto;

import br.app.ergane.api.negocio.model.PapelNegocio;

import java.util.UUID;

public record CadastroResponse(UUID usuarioId, NegocioResponse negocio) {

    public record NegocioResponse(UUID negocioId, UUID acessoNegocioId, PapelNegocio papel) {
    }

}
