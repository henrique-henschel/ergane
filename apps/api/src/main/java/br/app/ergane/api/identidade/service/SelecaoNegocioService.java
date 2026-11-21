package br.app.ergane.api.identidade.service;

import br.app.ergane.api.compartilhado.security.UsuarioAutenticado;
import br.app.ergane.api.identidade.dto.NegocioSelecionadoResponse;
import br.app.ergane.api.identidade.dto.SelecionarNegocioRequest;
import br.app.ergane.api.identidade.exception.FalhaAutenticacaoException;
import br.app.ergane.api.identidade.model.Sessao;
import br.app.ergane.api.identidade.repository.SessaoRepository;
import br.app.ergane.api.negocio.service.NegocioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class SelecaoNegocioService {

    private final SessaoRepository sessaoRepository;
    private final ValidacaoSessaoService validacaoSessaoService;
    private final NegocioService negocioService;

    public SelecaoNegocioService(SessaoRepository sessaoRepository, ValidacaoSessaoService validacaoSessaoService,
                                NegocioService negocioService) {
        this.sessaoRepository = sessaoRepository;
        this.validacaoSessaoService = validacaoSessaoService;
        this.negocioService = negocioService;
    }

    @Transactional
    public NegocioSelecionadoResponse selecionar(@NotNull UsuarioAutenticado usuario,
                                                  @NotNull @Valid SelecionarNegocioRequest request) {
        Sessao sessao = sessaoRepository.buscarPorIdComBloqueio(usuario.sessaoId())
                .orElseThrow(FalhaAutenticacaoException::new);
        validacaoSessaoService.validarEstado(sessao, usuario.usuarioId(), usuario.tokenAcessoId());
        negocioService.validarAcesso(usuario.usuarioId(), request.negocioId());
        sessao.selecionarNegocio(request.negocioId());
        sessaoRepository.flush();
        return new NegocioSelecionadoResponse(request.negocioId());
    }
}
