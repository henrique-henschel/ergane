package br.app.ergane.api.identidade.dto;

import br.app.ergane.api.identidade.model.PapelNegocio;

import java.util.UUID;

public record CadastroResponse(UUID usuarioId, ContaResponse conta) {

    public record ContaResponse(UUID contaId, UUID acessoNegocioId, PapelNegocio papel) {
    }

}
