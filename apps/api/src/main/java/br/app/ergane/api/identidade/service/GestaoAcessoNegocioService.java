package br.app.ergane.api.identidade.service;

import br.app.ergane.api.compartilhado.security.UsuarioAutenticado;
import br.app.ergane.api.identidade.dto.ConfirmacaoSenhaRequest;
import br.app.ergane.api.identidade.service.GestaoSessaoService.Confirmacao;
import br.app.ergane.api.identidade.service.GestaoSessaoService.ResultadoAcao;
import br.app.ergane.api.identidade.service.GestaoSessaoService.SituacaoAcao;
import br.app.ergane.api.negocio.service.NegocioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Service
@Validated
public class GestaoAcessoNegocioService {

    private final GestaoSessaoService gestaoSessaoService;
    private final NegocioService negocioService;

    public GestaoAcessoNegocioService(GestaoSessaoService gestaoSessaoService, NegocioService negocioService) {
        this.gestaoSessaoService = gestaoSessaoService;
        this.negocioService = negocioService;
    }

    @Transactional
    public ResultadoAcao<Void> revogar(@NotNull UsuarioAutenticado usuario, @NotNull UUID negocioId,
                                      @NotNull UUID acessoId, @NotNull @Valid ConfirmacaoSenhaRequest request) {
        Confirmacao confirmacao = gestaoSessaoService.confirmarSenha(usuario, request.senha());
        if (confirmacao.situacao() != SituacaoAcao.SUCESSO) {
            return new ResultadoAcao<>(confirmacao.situacao(), null, confirmacao.segundosEspera());
        }
        negocioService.revogarAcesso(usuario.usuarioId(), negocioId, acessoId);
        return ResultadoAcao.sucesso(null);
    }
}
