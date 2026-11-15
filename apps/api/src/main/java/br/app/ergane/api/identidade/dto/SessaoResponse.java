package br.app.ergane.api.identidade.dto;

import java.time.Instant;
import java.util.UUID;

public record SessaoResponse(UUID usuarioId, UUID sessaoId, String accessToken, String tokenType,
                             String refreshToken, Instant accessTokenExpiraEm,
                             Instant refreshTokenExpiraEm, Instant limiteAbsolutoEm) {
    @Override
    public String toString() {
        return "SessaoResponse[credenciais protegidas]";
    }
}
