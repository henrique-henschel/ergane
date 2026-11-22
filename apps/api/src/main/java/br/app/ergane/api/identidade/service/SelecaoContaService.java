package br.app.ergane.api.identidade.service;

import br.app.ergane.api.compartilhado.security.UsuarioAutenticado;
import br.app.ergane.api.identidade.dto.ContaSelecionadaResponse;
import br.app.ergane.api.identidade.dto.SelecionarContaRequest;
import br.app.ergane.api.identidade.exception.FalhaAutenticacaoException;
import br.app.ergane.api.identidade.model.Sessao;
import br.app.ergane.api.identidade.repository.SessaoRepository;
import br.app.ergane.api.identidade.service.ContaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class SelecaoContaService {

    private final SessaoRepository sessaoRepository;
    private final ValidacaoSessaoService validacaoSessaoService;
    private final ContaService contaService;

    public SelecaoContaService(SessaoRepository sessaoRepository, ValidacaoSessaoService validacaoSessaoService,
                                ContaService contaService) {
        this.sessaoRepository = sessaoRepository;
        this.validacaoSessaoService = validacaoSessaoService;
        this.contaService = contaService;
    }

    @Transactional
    public ContaSelecionadaResponse selecionar(@NotNull UsuarioAutenticado usuario,
                                                  @NotNull @Valid SelecionarContaRequest request) {
        Sessao sessao = sessaoRepository.buscarPorIdComBloqueio(usuario.sessaoId())
                .orElseThrow(FalhaAutenticacaoException::new);
        validacaoSessaoService.validarEstado(sessao, usuario.usuarioId(), usuario.tokenAcessoId());
        contaService.validarAcesso(usuario.usuarioId(), request.contaId());
        sessao.selecionarConta(request.contaId());
        sessaoRepository.flush();
        return new ContaSelecionadaResponse(request.contaId());
    }
}
