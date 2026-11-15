package br.app.ergane.api.identidade.dto;

import java.time.Instant;
import java.util.UUID;

public record SessaoResumoResponse(UUID sessaoId, boolean atual, Instant criadoEm,
                                   Instant expiraEm, Instant limiteAbsolutoEm) {
}
