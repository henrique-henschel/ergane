package br.app.ergane.api.compartilhado.security;

import br.app.ergane.api.compartilhado.exception.ContextoContaNaoSelecionadaException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Component
@RequestScope
public class ContextoConta {

    private UUID contaId;

    public void definirContaAutorizada(UUID contaId) {
        if (contaId == null) {
            throw new ContextoContaNaoSelecionadaException();
        }
        this.contaId = contaId;
    }

    public UUID getContaId() {
        if (contaId == null) {
            throw new ContextoContaNaoSelecionadaException();
        }
        return contaId;
    }
}
