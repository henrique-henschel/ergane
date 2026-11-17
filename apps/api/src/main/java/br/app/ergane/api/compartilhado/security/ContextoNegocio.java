package br.app.ergane.api.compartilhado.security;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * fornece o id do negocio usado nas consultas.
 * por enquanto usa o negocio de teste definido para o mvp.
 */
@Component
public class ContextoNegocio {

    private static final UUID NEGOCIO_MVP_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    public UUID getNegocioId() {
        return NEGOCIO_MVP_ID;
    }
}
