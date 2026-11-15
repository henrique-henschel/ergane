package br.app.ergane.api.compartilhado.security;

import br.app.ergane.api.compartilhado.exception.ContextoNegocioNaoSelecionadoException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Component
@RequestScope
public class ContextoNegocio {

    private UUID negocioId;

    public void definirNegocioAutorizado(UUID negocioId) {
        if (negocioId == null) {
            throw new ContextoNegocioNaoSelecionadoException();
        }
        this.negocioId = negocioId;
    }

    public UUID getNegocioId() {
        if (negocioId == null) {
            throw new ContextoNegocioNaoSelecionadoException();
        }
        return negocioId;
    }
}
