package br.app.ergane.api.identidade.dto;

import java.time.Instant;
import java.util.UUID;

public record RevogacaoSessaoResponse(UUID sessaoId, Instant revogadaEm) {
}
