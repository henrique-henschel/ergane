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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessaoServiceTests {

    private static final UUID USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID TOKEN_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID NEGOCIO_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final String EMAIL = "ana@example.test";
    private static final String HASH_ANTERIOR = "a".repeat(64);
    private static final String HASH_NOVO = "b".repeat(64);

    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final TentativaLoginRepository tentativas = mock(TentativaLoginRepository.class);
    private final SessaoRepository sessoes = mock(SessaoRepository.class);
    private final PasswordEncoder senhas = mock(PasswordEncoder.class);
    private final TokenSessaoService tokens = mock(TokenSessaoService.class);
    private final NegocioService negocios = mock(NegocioService.class);
    private final SessaoService service = new SessaoService(usuarios, tentativas, sessoes, senhas, tokens, negocios);

    @Test
    void loginValidoCriaSessaoComHashEReiniciaTentativas() {
        TentativaLogin tentativa = new TentativaLogin(EMAIL, Instant.now());
        tentativa.registrarFalha(Instant.now());
        when(tentativas.buscarComBloqueio(EMAIL)).thenReturn(tentativa);
        Usuario usuario = usuarioAtivo();
        when(usuarios.findByEmailNormalizado(EMAIL)).thenReturn(Optional.of(usuario));
        when(usuarios.bloquearEConsultarAtivo(USUARIO_ID)).thenReturn(Optional.of(true));
        when(senhas.matches("senha-correta", "hash-senha")).thenReturn(true);
        when(sessoes.saveAndFlush(any(Sessao.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        prepararEmissao();

        var resultado = service.acessar(new LoginRequest(EMAIL, "senha-correta"));

        var sessaoCriada = ArgumentCaptor.forClass(Sessao.class);
        verify(sessoes).saveAndFlush(sessaoCriada.capture());
        assertThat(resultado.situacao()).isEqualTo(SessaoService.SituacaoLogin.SUCESSO);
        assertThat(resultado.sessao().refreshToken()).isEqualTo("refresh-novo");
        assertThat(sessaoCriada.getValue().getUsuarioId()).isEqualTo(USUARIO_ID);
        assertThat(sessaoCriada.getValue().getRefreshTokenHash()).isEqualTo(HASH_NOVO);
        assertThat(tentativa.getFalhas()).isZero();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void loginRestauraPreferenciaApenasParaVinculoAtivo(boolean vinculoAtivo) {
        when(tentativas.buscarComBloqueio(EMAIL)).thenReturn(new TentativaLogin(EMAIL, Instant.now()));
        Usuario usuario = usuarioAtivo();
        when(usuarios.findByEmailNormalizado(EMAIL)).thenReturn(Optional.of(usuario));
        when(usuarios.bloquearEConsultarAtivo(USUARIO_ID)).thenReturn(Optional.of(true));
        when(usuarios.buscarUltimoNegocioPorId(USUARIO_ID)).thenReturn(Optional.of(NEGOCIO_ID));
        when(negocios.temAcesso(USUARIO_ID, NEGOCIO_ID)).thenReturn(vinculoAtivo);
        when(senhas.matches("senha-correta", "hash-senha")).thenReturn(true);
        when(sessoes.saveAndFlush(any(Sessao.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        prepararEmissao();

        var resultado = service.acessar(new LoginRequest(EMAIL, "senha-correta"));

        assertThat(resultado.situacao()).isEqualTo(SessaoService.SituacaoLogin.SUCESSO);
        var sessaoCriada = ArgumentCaptor.forClass(Sessao.class);
        verify(sessoes).saveAndFlush(sessaoCriada.capture());
        assertThat(sessaoCriada.getValue().getNegocioAtivoId()).isEqualTo(vinculoAtivo ? NEGOCIO_ID : null);
    }

    @Test
    void quintaSenhaIncorretaBloqueiaLoginENaoAbreSessao() {
        TentativaLogin tentativa = new TentativaLogin(EMAIL, Instant.now());
        when(tentativas.buscarComBloqueio(EMAIL)).thenReturn(tentativa);
        Usuario usuario = usuarioAtivo();
        when(usuarios.findByEmailNormalizado(EMAIL)).thenReturn(Optional.of(usuario));
        LoginRequest request = new LoginRequest(EMAIL, "senha-incorreta");

        for (int i = 0; i < 4; i++) {
            assertThat(service.acessar(request).situacao()).isEqualTo(SessaoService.SituacaoLogin.INVALIDO);
        }
        var resultado = service.acessar(request);

        assertThat(resultado.situacao()).isEqualTo(SessaoService.SituacaoLogin.BLOQUEADO);
        assertThat(resultado.segundosEspera()).isPositive();
        assertThat(resultado.sessao()).isNull();
        verify(sessoes, never()).saveAndFlush(any(Sessao.class));
    }

    @Test
    void renovarTrocaCredenciaisERespeitaLimiteAbsoluto() {
        Instant agora = Instant.now();
        Sessao sessao = new Sessao(USUARIO_ID, TOKEN_ID, HASH_ANTERIOR,
                agora.minusSeconds(120), agora.plusSeconds(600), agora.plusSeconds(1200));
        when(tokens.calcularHash("refresh-anterior")).thenReturn(HASH_ANTERIOR);
        when(sessoes.buscarPorRefreshComBloqueio(HASH_ANTERIOR)).thenReturn(Optional.of(sessao));
        Usuario usuario = usuarioAtivo();
        when(usuarios.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        prepararEmissao();

        SessaoResponse resultado = service.renovar(new RenovacaoSessaoRequest("refresh-anterior"));

        assertThat(resultado.refreshToken()).isEqualTo("refresh-novo");
        assertThat(sessao.getRefreshTokenHash()).isEqualTo(HASH_NOVO);
        assertThat(sessao.getTokenAcessoId()).isNotEqualTo(TOKEN_ID);
        assertThat(sessao.getExpiraEm()).isEqualTo(sessao.getLimiteAbsolutoEm());
        assertThatThrownBy(() -> new ValidacaoSessaoService(sessoes, usuarios)
                .validarEstado(sessao, USUARIO_ID, TOKEN_ID)).isInstanceOf(FalhaAutenticacaoException.class);
        verify(sessoes).flush();
    }

    @Test
    void renovarRejeitaSessaoExpiradaOuRefreshDesconhecido() {
        Instant passado = Instant.parse("2020-01-01T12:00:00Z");
        Sessao expirada = new Sessao(USUARIO_ID, TOKEN_ID, HASH_ANTERIOR,
                passado, passado.plusSeconds(60), passado.plusSeconds(120));
        when(tokens.calcularHash("refresh-expirado")).thenReturn(HASH_ANTERIOR);
        when(tokens.calcularHash("refresh-desconhecido")).thenReturn(HASH_NOVO);
        when(sessoes.buscarPorRefreshComBloqueio(HASH_ANTERIOR)).thenReturn(Optional.of(expirada));

        assertThatThrownBy(() -> service.renovar(new RenovacaoSessaoRequest("refresh-expirado")))
                .isInstanceOf(FalhaAutenticacaoException.class);
        assertThatThrownBy(() -> service.renovar(new RenovacaoSessaoRequest("refresh-desconhecido")))
                .isInstanceOf(FalhaAutenticacaoException.class);
        verify(tokens, never()).gerarRefreshToken();
    }

    private Usuario usuarioAtivo() {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(USUARIO_ID);
        when(usuario.getSenhaHash()).thenReturn("hash-senha");
        when(usuario.isAtivo()).thenReturn(true);
        return usuario;
    }

    private void prepararEmissao() {
        when(tokens.gerarRefreshToken()).thenReturn("refresh-novo");
        when(tokens.calcularHash("refresh-novo")).thenReturn(HASH_NOVO);
        when(tokens.emitir(any(Sessao.class), any(String.class), any(Instant.class))).thenAnswer(invocacao -> {
            Sessao sessao = invocacao.getArgument(0);
            return new SessaoResponse(sessao.getUsuarioId(), sessao.getId(), "jwt-simulado", "Bearer",
                    invocacao.getArgument(1), null, sessao.getExpiraEm(), sessao.getLimiteAbsolutoEm());
        });
    }
}
