package br.app.ergane.api.identidade.service;

import br.app.ergane.api.compartilhado.security.UsuarioAutenticado;
import br.app.ergane.api.identidade.dto.NegocioSelecionadoResponse;
import br.app.ergane.api.identidade.dto.SelecionarNegocioRequest;
import br.app.ergane.api.identidade.exception.FalhaAutenticacaoException;
import br.app.ergane.api.identidade.model.Sessao;
import br.app.ergane.api.identidade.repository.SessaoRepository;
import br.app.ergane.api.identidade.repository.UsuarioRepository;
import br.app.ergane.api.negocio.service.NegocioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Service
@Validated
public class SelecaoNegocioService {

    private final SessaoRepository sessaoRepository;
    private final ValidacaoSessaoService validacaoSessaoService;
    private final NegocioService negocioService;
    private final UsuarioRepository usuarioRepository;

    public SelecaoNegocioService(SessaoRepository sessaoRepository, ValidacaoSessaoService validacaoSessaoService,
                                NegocioService negocioService, UsuarioRepository usuarioRepository) {
        this.sessaoRepository = sessaoRepository;
        this.validacaoSessaoService = validacaoSessaoService;
        this.negocioService = negocioService;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public NegocioSelecionadoResponse consultar(@NotNull UsuarioAutenticado usuario) {
        UsuarioAutenticado atual = validacaoSessaoService.validar(
                usuario.usuarioId(), usuario.sessaoId(), usuario.tokenAcessoId());
        UUID negocioId = atual.negocioAtivoId();
        if (negocioId != null && !negocioService.temAcesso(atual.usuarioId(), negocioId)) {
            negocioId = null;
        }
        return new NegocioSelecionadoResponse(negocioId);
    }

    @Transactional
    public NegocioSelecionadoResponse selecionar(@NotNull UsuarioAutenticado usuario,
                                                  @NotNull @Valid SelecionarNegocioRequest request) {
        if (!usuarioRepository.bloquearEConsultarAtivo(usuario.usuarioId()).orElse(false)) {
            throw new FalhaAutenticacaoException();
        }
        Sessao sessao = sessaoRepository.buscarPorIdComBloqueio(usuario.sessaoId())
                .orElseThrow(FalhaAutenticacaoException::new);
        validacaoSessaoService.validarEstado(sessao, usuario.usuarioId(), usuario.tokenAcessoId());
        negocioService.validarAcesso(usuario.usuarioId(), request.negocioId());
        sessao.selecionarNegocio(request.negocioId());
        usuarioRepository.salvarUltimoNegocio(usuario.usuarioId(), request.negocioId());
        sessaoRepository.flush();
        return new NegocioSelecionadoResponse(request.negocioId());
    }
}
