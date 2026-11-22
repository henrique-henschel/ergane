package br.app.ergane.api.identidade.service;

import br.app.ergane.api.compartilhado.exception.RecursoNaoEncontradoException;
import br.app.ergane.api.compartilhado.security.UsuarioAutenticado;
import br.app.ergane.api.identidade.dto.ConfirmacaoSenhaRequest;
import br.app.ergane.api.identidade.dto.RevogacaoSessaoResponse;
import br.app.ergane.api.identidade.dto.RevogacaoTodasSessoesResponse;
import br.app.ergane.api.identidade.dto.SessaoResumoResponse;
import br.app.ergane.api.identidade.exception.FalhaAutenticacaoException;
import br.app.ergane.api.identidade.model.Sessao;
import br.app.ergane.api.identidade.model.TentativaLogin;
import br.app.ergane.api.identidade.model.Usuario;
import br.app.ergane.api.identidade.repository.SessaoRepository;
import br.app.ergane.api.identidade.repository.TentativaLoginRepository;
import br.app.ergane.api.identidade.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Validated
public class GestaoSessaoService {

    private final SessaoRepository sessaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TentativaLoginRepository tentativaRepository;
    private final ValidacaoSessaoService validacaoSessaoService;
    private final PasswordEncoder passwordEncoder;
    private final String hashSimulado;

    public GestaoSessaoService(SessaoRepository sessaoRepository, UsuarioRepository usuarioRepository,
                               TentativaLoginRepository tentativaRepository,
                               ValidacaoSessaoService validacaoSessaoService, PasswordEncoder passwordEncoder) {
        this.sessaoRepository = sessaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.tentativaRepository = tentativaRepository;
        this.validacaoSessaoService = validacaoSessaoService;
        this.passwordEncoder = passwordEncoder;
        this.hashSimulado = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional(readOnly = true)
    public List<SessaoResumoResponse> listar(@NotNull UsuarioAutenticado usuario) {
        validacaoSessaoService.validar(usuario.usuarioId(), usuario.sessaoId(), usuario.tokenAcessoId());
        return sessaoRepository.listarAtivas(usuario.usuarioId(), Instant.now()).stream()
                .map(sessao -> new SessaoResumoResponse(sessao.getId(), sessao.getId().equals(usuario.sessaoId()),
                        sessao.getCriadoEm(), sessao.getExpiraEm(), sessao.getLimiteAbsolutoEm()))
                .toList();
    }

    @Transactional
    public RevogacaoSessaoResponse revogarAtual(@NotNull UsuarioAutenticado usuario) {
        bloquearUsuario(usuario.usuarioId());
        Sessao atual = bloquearSessaoAtual(usuario);
        atual.revogar(Instant.now());
        sessaoRepository.flush();
        return new RevogacaoSessaoResponse(atual.getId(), atual.getRevogadaEm());
    }

    @Transactional
    public ResultadoAcao<RevogacaoSessaoResponse> revogar(@NotNull UsuarioAutenticado usuario,
                                                        @NotNull UUID id,
                                                        @NotNull @Valid ConfirmacaoSenhaRequest request) {
        if (!sessaoRepository.existsByIdAndUsuarioId(id, usuario.usuarioId())) {
            throw naoEncontrada();
        }
        Confirmacao confirmacao = confirmarSenha(usuario, request.senha());
        if (confirmacao.situacao() != SituacaoAcao.SUCESSO) {
            return new ResultadoAcao<>(confirmacao.situacao(), null, confirmacao.segundosEspera());
        }
        Sessao sessao = sessaoRepository.buscarPropriaComBloqueio(id, usuario.usuarioId())
                .orElseThrow(this::naoEncontrada);
        sessao.revogar(Instant.now());
        sessaoRepository.flush();
        return ResultadoAcao.sucesso(new RevogacaoSessaoResponse(sessao.getId(), sessao.getRevogadaEm()));
    }

    @Transactional
    public ResultadoAcao<RevogacaoTodasSessoesResponse> revogarTodas(@NotNull UsuarioAutenticado usuario,
                                                                   @NotNull @Valid ConfirmacaoSenhaRequest request) {
        Confirmacao confirmacao = confirmarSenha(usuario, request.senha());
        if (confirmacao.situacao() != SituacaoAcao.SUCESSO) {
            return new ResultadoAcao<>(confirmacao.situacao(), null, confirmacao.segundosEspera());
        }
        int quantidade = sessaoRepository.revogarTodasAtivas(usuario.usuarioId(), Instant.now());
        return ResultadoAcao.sucesso(new RevogacaoTodasSessoesResponse(quantidade));
    }

    private Confirmacao confirmarSenha(UsuarioAutenticado autenticado, String senha) {
        String email = usuarioRepository.buscarEmailPorId(autenticado.usuarioId())
                .orElseThrow(FalhaAutenticacaoException::new);
        tentativaRepository.criarSeAusente(email, Instant.now());
        TentativaLogin tentativa = tentativaRepository.buscarComBloqueio(email);
        bloquearUsuario(autenticado.usuarioId());
        bloquearSessaoAtual(autenticado);
        Instant agora = Instant.now();
        if (tentativa.estaBloqueada(agora)) {
            return new Confirmacao(SituacaoAcao.BLOQUEADO, segundosRestantes(agora, tentativa.getBloqueadoAte()));
        }

        Usuario usuario = usuarioRepository.findById(autenticado.usuarioId())
                .orElseThrow(FalhaAutenticacaoException::new);
        String hash = usuario.getSenhaHash();
        boolean possuiHash = hash != null && !hash.isBlank();
        boolean correta = passwordEncoder.matches(senha, possuiHash ? hash : hashSimulado);
        agora = Instant.now();
        if (!possuiHash || !correta) {
            tentativa.registrarFalha(agora);
            if (tentativa.estaBloqueada(agora)) {
                return new Confirmacao(SituacaoAcao.BLOQUEADO, segundosRestantes(agora, tentativa.getBloqueadoAte()));
            }
            return new Confirmacao(SituacaoAcao.INVALIDO, 0);
        }
        tentativa.reiniciar(agora);
        return new Confirmacao(SituacaoAcao.SUCESSO, 0);
    }

    private void bloquearUsuario(UUID usuarioId) {
        if (!usuarioRepository.bloquearEConsultarAtivo(usuarioId).orElse(false)) {
            throw new FalhaAutenticacaoException();
        }
    }

    private Sessao bloquearSessaoAtual(UsuarioAutenticado usuario) {
        Sessao atual = sessaoRepository.buscarPorIdComBloqueio(usuario.sessaoId())
                .orElseThrow(FalhaAutenticacaoException::new);
        validacaoSessaoService.validarEstado(atual, usuario.usuarioId(), usuario.tokenAcessoId());
        return atual;
    }

    private RecursoNaoEncontradoException naoEncontrada() {
        return new RecursoNaoEncontradoException("Erro! Sessão não encontrada.");
    }

    private long segundosRestantes(Instant agora, Instant bloqueadoAte) {
        Duration restante = Duration.between(agora, bloqueadoAte);
        return Math.max(1, restante.getSeconds() + (restante.getNano() > 0 ? 1 : 0));
    }

    private record Confirmacao(SituacaoAcao situacao, long segundosEspera) {
    }

    public enum SituacaoAcao {
        SUCESSO, INVALIDO, BLOQUEADO
    }

    public record ResultadoAcao<T>(SituacaoAcao situacao, T resposta, long segundosEspera) {
        public static <T> ResultadoAcao<T> sucesso(T resposta) {
            return new ResultadoAcao<>(SituacaoAcao.SUCESSO, resposta, 0);
        }
    }
}
