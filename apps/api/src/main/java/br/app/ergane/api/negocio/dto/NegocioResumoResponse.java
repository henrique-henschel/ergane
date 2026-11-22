package br.app.ergane.api.negocio.dto;

import br.app.ergane.api.negocio.model.PapelNegocio;

import java.util.UUID;

public record NegocioResumoResponse(UUID negocioId, String nome, PapelNegocio papel) {
}
