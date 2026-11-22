package br.app.ergane.api.compartilhado.security;

import java.security.Principal;
import java.util.UUID;

public record UsuarioAutenticado(UUID usuarioId, UUID sessaoId, UUID tokenAcessoId,
                                 UUID contaAtivaId) implements Principal {
    @Override
    public String getName() {
        return usuarioId.toString();
    }
}
