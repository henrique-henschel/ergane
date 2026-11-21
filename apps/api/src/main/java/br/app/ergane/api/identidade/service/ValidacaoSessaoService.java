package br.app.ergane.api.identidade.service;

import br.app.ergane.api.compartilhado.security.UsuarioAutenticado;
import br.app.ergane.api.identidade.exception.FalhaAutenticacaoException;
import br.app.ergane.api.identidade.model.Sessao;
import br.app.ergane.api.identidade.repository.SessaoRepository;
import br.app.ergane.api.identidade.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ValidacaoSessaoService {

    private final SessaoRepository sessaoRepository;
    private final UsuarioRepository usuarioRepository;

    public ValidacaoSessaoService(SessaoRepository sessaoRepository, UsuarioRepository usuarioRepository) {
        this.sessaoRepository = sessaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public UsuarioAutenticado validar(UUID usuarioId, UUID sessaoId, UUID tokenAcessoId) {
        if (usuarioId == null || sessaoId == null || tokenAcessoId == null) {
            throw new FalhaAutenticacaoException();
        }
        Sessao sessao = sessaoRepository.findById(sessaoId).orElseThrow(FalhaAutenticacaoException::new);
        return validarEstado(sessao, usuarioId, tokenAcessoId);
    }

    @Transactional(readOnly = true)
    public UsuarioAutenticado validarEstado(Sessao sessao, UUID usuarioId, UUID tokenAcessoId) {
        if (sessao == null || !sessao.getUsuarioId().equals(usuarioId)
                || !sessao.getTokenAcessoId().equals(tokenAcessoId) || !sessao.estaValida(Instant.now())) {
            throw new FalhaAutenticacaoException();
        }
        boolean ativo = usuarioRepository.findById(usuarioId)
                .map(usuario -> usuario.isAtivo()).orElse(false);
        if (!ativo) {
            throw new FalhaAutenticacaoException();
        }
        return new UsuarioAutenticado(usuarioId, sessao.getId(), tokenAcessoId, sessao.getNegocioAtivoId());
    }
}
