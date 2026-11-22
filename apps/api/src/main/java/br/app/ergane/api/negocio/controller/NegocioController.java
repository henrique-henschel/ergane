package br.app.ergane.api.negocio.controller;

import br.app.ergane.api.compartilhado.security.UsuarioAutenticado;
import br.app.ergane.api.negocio.dto.NegocioResumoResponse;
import br.app.ergane.api.negocio.service.NegocioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/negocios")
public class NegocioController {

    private final NegocioService negocioService;

    public NegocioController(NegocioService negocioService) {
        this.negocioService = negocioService;
    }

    @GetMapping
    public List<NegocioResumoResponse> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return negocioService.listarAutorizados(usuario.usuarioId());
    }
}
