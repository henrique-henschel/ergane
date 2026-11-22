package br.app.ergane.api.compartilhado.security;

import br.app.ergane.api.compartilhado.exception.ContextoContaNaoSelecionadaException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Objects;
import java.util.UUID;

@Component
@RequestScope
public class ContextoConta {

    private UUID usuarioId;
    private UUID contaId;

    public void definirContaAutorizada(UUID usuarioId, UUID contaId) {
        if (contaId == null) {
            throw new ContextoContaNaoSelecionadaException();
        }
        this.usuarioId = Objects.requireNonNull(usuarioId, "O identificador do usuário é obrigatório.");
        this.contaId = contaId;
    }

    public UUID getContaId() {
        if (contaId == null) {
            throw new ContextoContaNaoSelecionadaException();
        }
        return contaId;
    }

    public UUID getUsuarioId() {
        if (usuarioId == null) {
            throw new ContextoContaNaoSelecionadaException();
        }
        return usuarioId;
    }
}
