package br.app.ergane.api.identidade.controller;

import br.app.ergane.api.identidade.dto.LoginRequest;
import br.app.ergane.api.identidade.dto.RenovacaoSessaoRequest;
import br.app.ergane.api.identidade.dto.SessaoResponse;
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

@RestController
@RequestMapping("/api/sessoes")
public class SessaoController {

    private final SessaoService sessaoService;

    public SessaoController(SessaoService sessaoService) {
        this.sessaoService = sessaoService;
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
