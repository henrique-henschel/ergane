package br.app.ergane.api.identidade.controller;

import br.app.ergane.api.identidade.dto.LoginRequest;
import br.app.ergane.api.identidade.dto.RenovacaoSessaoRequest;
import br.app.ergane.api.identidade.dto.SessaoResponse;
import br.app.ergane.api.identidade.dto.ConfirmacaoSenhaRequest;
import br.app.ergane.api.identidade.dto.RevogacaoSessaoResponse;
import br.app.ergane.api.identidade.dto.RevogacaoTodasSessoesResponse;
import br.app.ergane.api.identidade.dto.SessaoResumoResponse;
import br.app.ergane.api.identidade.service.GestaoSessaoService;
import br.app.ergane.api.identidade.service.GestaoSessaoService.ResultadoAcao;
import br.app.ergane.api.identidade.dto.SelecionarNegocioRequest;
import br.app.ergane.api.identidade.dto.NegocioSelecionadoResponse;
import br.app.ergane.api.compartilhado.security.UsuarioAutenticado;
import br.app.ergane.api.identidade.service.SelecaoNegocioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import br.app.ergane.api.identidade.exception.FalhaAutenticacaoException;
import br.app.ergane.api.identidade.exception.LoginBloqueadoException;
import br.app.ergane.api.identidade.service.SessaoService;
import br.app.ergane.api.identidade.service.SessaoService.ResultadoLogin;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessoes")
public class SessaoController {

    private final SessaoService sessaoService;
    private final SelecaoNegocioService selecaoNegocioService;
    private final GestaoSessaoService gestaoSessaoService;

    public SessaoController(SessaoService sessaoService, SelecaoNegocioService selecaoNegocioService,
                            GestaoSessaoService gestaoSessaoService) {
        this.sessaoService = sessaoService;
        this.selecaoNegocioService = selecaoNegocioService;
        this.gestaoSessaoService = gestaoSessaoService;
    }

    @GetMapping
    public ResponseEntity<List<SessaoResumoResponse>> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header(HttpHeaders.PRAGMA, "no-cache")
                .body(gestaoSessaoService.listar(usuario));
    }

    @PostMapping("/atual/revogar")
    public ResponseEntity<RevogacaoSessaoResponse> revogarAtual(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header(HttpHeaders.PRAGMA, "no-cache")
                .body(gestaoSessaoService.revogarAtual(usuario));
    }

    @PostMapping("/{id}/revogar")
    public ResponseEntity<RevogacaoSessaoResponse> revogar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                         @PathVariable UUID id,
                                                         @Valid @RequestBody ConfirmacaoSenhaRequest request) {
        return responderGestao(gestaoSessaoService.revogar(usuario, id, request));
    }

    @PostMapping("/revogar-todas")
    public ResponseEntity<RevogacaoTodasSessoesResponse> revogarTodas(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                                    @Valid @RequestBody ConfirmacaoSenhaRequest request) {
        return responderGestao(gestaoSessaoService.revogarTodas(usuario, request));
    }

    private <T> ResponseEntity<T> responderGestao(ResultadoAcao<T> resultado) {
        switch (resultado.situacao()) {
            case INVALIDO -> throw new FalhaAutenticacaoException();
            case BLOQUEADO -> throw new LoginBloqueadoException(resultado.segundosEspera());
            default -> {
                return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header(HttpHeaders.PRAGMA, "no-cache")
                        .body(resultado.resposta());
            }
        }
    }

    @GetMapping("/atual/negocio")
    public ResponseEntity<NegocioSelecionadoResponse> consultarNegocio(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache").body(selecaoNegocioService.consultar(usuario));
    }

    @PutMapping("/atual/negocio")
    public ResponseEntity<NegocioSelecionadoResponse> selecionarNegocio(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody SelecionarNegocioRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache").body(selecaoNegocioService.selecionar(usuario, request));
    }

    @PostMapping
    public ResponseEntity<SessaoResponse> acessar(@Valid @RequestBody LoginRequest request) {
        ResultadoLogin resultado = sessaoService.acessar(request);
        switch (resultado.situacao()) {
            case INVALIDO -> throw new FalhaAutenticacaoException();
            case BLOQUEADO -> throw new LoginBloqueadoException(resultado.segundosEspera());
            default -> {
                return ResponseEntity.created(URI.create("/api/sessoes/" + resultado.sessao().sessaoId()))
                        .cacheControl(CacheControl.noStore()).header(HttpHeaders.PRAGMA, "no-cache")
                        .body(resultado.sessao());
            }
        }
    }

    @PostMapping("/renovacoes")
    public ResponseEntity<SessaoResponse> renovar(@Valid @RequestBody RenovacaoSessaoRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache").body(sessaoService.renovar(request));
    }
}
