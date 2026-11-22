package br.app.ergane.api.identidade.service;

import br.app.ergane.api.identidade.dto.LoginRequest;
import br.app.ergane.api.identidade.dto.RenovacaoSessaoRequest;
import br.app.ergane.api.identidade.dto.SessaoResponse;
import br.app.ergane.api.identidade.exception.FalhaAutenticacaoException;
import br.app.ergane.api.identidade.model.Sessao;
import br.app.ergane.api.identidade.model.TentativaLogin;
import br.app.ergane.api.identidade.model.Usuario;
import br.app.ergane.api.identidade.repository.SessaoRepository;
import br.app.ergane.api.identidade.repository.TentativaLoginRepository;
import br.app.ergane.api.identidade.repository.UsuarioRepository;
import br.app.ergane.api.negocio.service.NegocioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@Validated
public class SessaoService {

    private final UsuarioRepository usuarioRepository;
    private final TentativaLoginRepository tentativaRepository;
    private final SessaoRepository sessaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenSessaoService tokens;
    private final NegocioService negocioService;
    private final String hashSimulado;

    public SessaoService(UsuarioRepository usuarioRepository, TentativaLoginRepository tentativaRepository,
                        SessaoRepository sessaoRepository, PasswordEncoder passwordEncoder, TokenSessaoService tokens,
                        NegocioService negocioService) {
        this.usuarioRepository = usuarioRepository;
        this.tentativaRepository = tentativaRepository;
        this.sessaoRepository = sessaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.negocioService = negocioService;
        this.hashSimulado = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public ResultadoLogin acessar(@NotNull @Valid LoginRequest request) {
        tentativaRepository.criarSeAusente(request.email(), Instant.now());
        TentativaLogin tentativa = tentativaRepository.buscarComBloqueio(request.email());
        Instant agora = Instant.now();
        if (tentativa.estaBloqueada(agora)) {
            return ResultadoLogin.bloqueado(segundosRestantes(agora, tentativa.getBloqueadoAte()));
        }

        Usuario usuario = usuarioRepository.findByEmailNormalizado(request.email()).orElse(null);
        String hash = usuario == null ? null : usuario.getSenhaHash();
        boolean possuiHash = hash != null && !hash.isBlank();
        boolean senhaCorreta = passwordEncoder.matches(request.senha(), possuiHash ? hash : hashSimulado);
        agora = Instant.now();
        if (usuario == null || !usuario.isAtivo() || !possuiHash || !senhaCorreta) {
            tentativa.registrarFalha(agora);
            if (tentativa.estaBloqueada(agora)) {
                return ResultadoLogin.bloqueado(segundosRestantes(agora, tentativa.getBloqueadoAte()));
            }
            return ResultadoLogin.invalido();
        }

        boolean ativo = usuarioRepository.bloquearEConsultarAtivo(usuario.getId()).orElse(false);
        agora = Instant.now();
        if (!ativo) {
            tentativa.registrarFalha(agora);
            if (tentativa.estaBloqueada(agora)) {
                return ResultadoLogin.bloqueado(segundosRestantes(agora, tentativa.getBloqueadoAte()));
            }
            return ResultadoLogin.invalido();
        }

        String refresh = tokens.gerarRefreshToken();
        Sessao sessao = new Sessao(usuario.getId(), UUID.randomUUID(),
                tokens.calcularHash(refresh), agora, agora.plus(Duration.ofDays(30)), agora.plus(Duration.ofDays(90)));
        usuarioRepository.buscarUltimoNegocioPorId(usuario.getId())
                .filter(negocioId -> negocioService.temAcesso(usuario.getId(), negocioId))
                .ifPresent(sessao::selecionarNegocio);
        SessaoResponse response = tokens.emitir(sessaoRepository.saveAndFlush(sessao), refresh, agora);
        tentativa.reiniciar(agora);
        return ResultadoLogin.sucesso(response);
    }

    @Transactional
    public SessaoResponse renovar(@NotNull @Valid RenovacaoSessaoRequest request) {
        Sessao sessao = sessaoRepository.buscarPorRefreshComBloqueio(tokens.calcularHash(request.refreshToken()))
                .orElseThrow(FalhaAutenticacaoException::new);
        Instant agora = Instant.now();
        if (!sessao.estaValida(agora)) {
            throw new FalhaAutenticacaoException();
        }
        Usuario usuario = usuarioRepository.findById(sessao.getUsuarioId())
                .orElseThrow(FalhaAutenticacaoException::new);
        if (!usuario.isAtivo()) {
            throw new FalhaAutenticacaoException();
        }

        String novoRefresh = tokens.gerarRefreshToken();
        Instant novaExpiracao = agora.plus(Duration.ofDays(30));
        if (novaExpiracao.isAfter(sessao.getLimiteAbsolutoEm())) {
            novaExpiracao = sessao.getLimiteAbsolutoEm();
        }
        sessao.renovar(UUID.randomUUID(), tokens.calcularHash(novoRefresh), agora, novaExpiracao);
        sessaoRepository.flush();
        return tokens.emitir(sessao, novoRefresh, agora);
    }

    private long segundosRestantes(Instant agora, Instant bloqueadoAte) {
        Duration restante = Duration.between(agora, bloqueadoAte);
        return Math.max(1, restante.getSeconds() + (restante.getNano() > 0 ? 1 : 0));
    }

    public enum SituacaoLogin {
        SUCESSO, INVALIDO, BLOQUEADO
    }

    public record ResultadoLogin(SituacaoLogin situacao, SessaoResponse sessao, long segundosEspera) {
        public static ResultadoLogin sucesso(SessaoResponse sessao) {
            return new ResultadoLogin(SituacaoLogin.SUCESSO, sessao, 0);
        }

        public static ResultadoLogin invalido() {
            return new ResultadoLogin(SituacaoLogin.INVALIDO, null, 0);
        }

        public static ResultadoLogin bloqueado(long segundos) {
            return new ResultadoLogin(SituacaoLogin.BLOQUEADO, null, segundos);
        }
    }
}
