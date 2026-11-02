package br.app.ergane.api.compartilhado;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ContextoNegocio {

    // Temporario: no UC01 passara a devolver o negocio do usuario autenticado
    private static final UUID NEGOCIO_FIXO = UUID.fromString("00000000-0000-0000-0000-000000000001");

    public UUID negocioId() {
        return NEGOCIO_FIXO;
    }

}
