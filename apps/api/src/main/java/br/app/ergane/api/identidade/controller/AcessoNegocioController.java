package br.app.ergane.api.identidade.controller;

import br.app.ergane.api.compartilhado.security.UsuarioAutenticado;
import br.app.ergane.api.identidade.dto.ConfirmacaoSenhaRequest;
import br.app.ergane.api.identidade.exception.FalhaAutenticacaoException;
import br.app.ergane.api.identidade.exception.LoginBloqueadoException;
import br.app.ergane.api.identidade.service.GestaoAcessoNegocioService;
import br.app.ergane.api.identidade.service.GestaoSessaoService.ResultadoAcao;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/negocios/{negocioId}/acessos")
public class AcessoNegocioController {

    private final GestaoAcessoNegocioService gestaoAcessoNegocioService;

    public AcessoNegocioController(GestaoAcessoNegocioService gestaoAcessoNegocioService) {
        this.gestaoAcessoNegocioService = gestaoAcessoNegocioService;
    }

    @PostMapping("/{acessoId}/revogar")
    public ResponseEntity<Void> revogar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                         @PathVariable UUID negocioId, @PathVariable UUID acessoId,
                                         @Valid @RequestBody ConfirmacaoSenhaRequest request) {
        ResultadoAcao<Void> resultado = gestaoAcessoNegocioService.revogar(usuario, negocioId, acessoId, request);
        return switch (resultado.situacao()) {
            case INVALIDO -> throw new FalhaAutenticacaoException();
            case BLOQUEADO -> throw new LoginBloqueadoException(resultado.segundosEspera());
            case SUCESSO -> ResponseEntity.noContent().build();
        };
    }
}
