package br.app.ergane.api.negocio.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.app.ergane.api.negocio.model.AcessoNegocio;
import br.app.ergane.api.negocio.dto.NegocioResumoResponse;
import br.app.ergane.api.negocio.model.Negocio;
import br.app.ergane.api.negocio.model.PapelNegocio;
import br.app.ergane.api.negocio.repository.AcessoNegocioRepository;
import br.app.ergane.api.negocio.repository.NegocioRepository;
import br.app.ergane.api.compartilhado.exception.RecursoNaoEncontradoException;

@Service
public class NegocioService {

    private final NegocioRepository negocioRepository;
    private final AcessoNegocioRepository acessoNegocioRepository;

    public NegocioService(NegocioRepository negocioRepository, AcessoNegocioRepository acessoNegocioRepository) {
        this.negocioRepository = negocioRepository;
        this.acessoNegocioRepository = acessoNegocioRepository;
    }

    @Transactional
    public NegocioCriado criarComProprietario(UUID usuarioId, String nome) {
        Negocio negocio = negocioRepository.saveAndFlush(new Negocio(nome));

        //automaticamente cria o acesso do usuário que criou o negócio como proprietário
        AcessoNegocio acesso = acessoNegocioRepository.saveAndFlush(
                new AcessoNegocio(usuarioId, negocio.getId(), PapelNegocio.PROPRIETARIO));
        return new NegocioCriado(negocio.getId(), acesso.getId(), acesso.getPapel());
    }

    public record NegocioCriado(UUID negocioId, UUID acessoNegocioId, PapelNegocio papel) {

    }

    @Transactional(readOnly = true)
    public void validarAcesso(UUID usuarioId, UUID negocioId) {
        acessoNegocioRepository.findByUsuarioIdAndNegocioIdAndAtivoTrue(usuarioId, negocioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Erro! Negócio não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<NegocioResumoResponse> listarAutorizados(UUID usuarioId) {
        return acessoNegocioRepository.listarAutorizados(usuarioId);
    }

    @Transactional(readOnly = true)
    public boolean temAcesso(UUID usuarioId, UUID negocioId) {
        return acessoNegocioRepository.existsByUsuarioIdAndNegocioIdAndAtivoTrue(usuarioId, negocioId);
    }

    @Transactional
    public void revogarAcesso(UUID usuarioId, UUID negocioId, UUID acessoId) {
        List<AcessoNegocio> acessos = acessoNegocioRepository.bloquearAcessosParaRevogacao(negocioId, usuarioId, acessoId);
        AcessoNegocio gestor = acessos.stream()
                .filter(acesso -> acesso.getUsuarioId().equals(usuarioId) && acesso.isAtivo())
                .findFirst().orElseThrow(this::acessoNaoEncontrado);
        AcessoNegocio alvo = acessos.stream()
                .filter(acesso -> acesso.getId().equals(acessoId))
                .findFirst().orElseThrow(this::acessoNaoEncontrado);
        boolean podeRevogar = switch (alvo.getPapel()) {
            case PROPRIETARIO -> false;
            case ADMINISTRADOR -> gestor.getPapel() == PapelNegocio.PROPRIETARIO;
            case FUNCIONARIO -> gestor.getPapel() == PapelNegocio.PROPRIETARIO
                    || gestor.getPapel() == PapelNegocio.ADMINISTRADOR;
        };
        if (!podeRevogar) {
            throw acessoNaoEncontrado();
        }
        alvo.revogar();
    }

    private RecursoNaoEncontradoException acessoNaoEncontrado() {
        return new RecursoNaoEncontradoException("Erro! Acesso ao negócio não encontrado.");
    }

}
