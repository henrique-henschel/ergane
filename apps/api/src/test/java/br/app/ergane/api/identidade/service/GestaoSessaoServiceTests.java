package br.app.ergane.api.identidade.service;

import br.app.ergane.api.compartilhado.exception.RecursoNaoEncontradoException;
import br.app.ergane.api.compartilhado.security.UsuarioAutenticado;
import br.app.ergane.api.identidade.dto.ConfirmacaoSenhaRequest;
import br.app.ergane.api.identidade.exception.FalhaAutenticacaoException;
import br.app.ergane.api.identidade.model.Sessao;
import br.app.ergane.api.identidade.model.TentativaLogin;
import br.app.ergane.api.identidade.model.Usuario;
import br.app.ergane.api.identidade.repository.SessaoRepository;
import br.app.ergane.api.identidade.repository.TentativaLoginRepository;
import br.app.ergane.api.identidade.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GestaoSessaoServiceTests {

    private static final UUID USUARIO = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ATUAL = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID OUTRA = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID TOKEN = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final String EMAIL = "ana@example.test";

    private final SessaoRepository sessoes = mock(SessaoRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final TentativaLoginRepository tentativas = mock(TentativaLoginRepository.class);
    private final PasswordEncoder senhas = mock(PasswordEncoder.class);
    private final ValidacaoSessaoService validacao = new ValidacaoSessaoService(sessoes, usuarios);
    private final GestaoSessaoService service = new GestaoSessaoService(sessoes, usuarios, tentativas, validacao, senhas);
    private final UsuarioAutenticado autenticado = new UsuarioAutenticado(USUARIO, ATUAL, TOKEN, null);

    @Test
    void listaSessoesAtivasDoTitularEIdentificaAAtual() {
        Sessao atual = prepararSessaoAtual();
        when(sessoes.listarAtivas(eq(USUARIO), any(Instant.class))).thenReturn(List.of(atual, sessao(OUTRA)));

        var resultado = service.listar(autenticado);

        assertThat(resultado).satisfiesExactly(
                resumo -> {
                    assertThat(resumo.sessaoId()).isEqualTo(ATUAL);
                    assertThat(resumo.atual()).isTrue();
                },
                resumo -> {
                    assertThat(resumo.sessaoId()).isEqualTo(OUTRA);
                    assertThat(resumo.atual()).isFalse();
                });
    }

    @Test
    void revogarAtualTornaTokenInutilizavel() {
        Sessao atual = prepararSessaoAtual();

        var resultado = service.revogarAtual(autenticado);

        assertThat(resultado.sessaoId()).isEqualTo(ATUAL);
        assertThat(atual.getRevogadaEm()).isNotNull();
        assertThatThrownBy(() -> validacao.validar(USUARIO, ATUAL, TOKEN))
                .isInstanceOf(FalhaAutenticacaoException.class);
        verify(sessoes).flush();
    }

    @Test
    void naoRevogaSessaoAlheiaMesmoComIdConhecido() {
        quandoSessaoDeOutroUsuario();

        assertThatThrownBy(() -> service.revogar(autenticado, OUTRA, new ConfirmacaoSenhaRequest("senha")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(sessoes, never()).flush();
    }

    @Test
    void revogacaoIndividualExigeSenhaERevogaApenasASessaoAlvo() {
        Sessao atual = prepararSessaoAtual();
        Sessao outra = sessao(OUTRA);
        prepararConfirmacao();
        when(sessoes.existsByIdAndUsuarioId(OUTRA, USUARIO)).thenReturn(true);
        when(sessoes.buscarPropriaComBloqueio(OUTRA, USUARIO)).thenReturn(Optional.of(outra));

        var negada = service.revogar(autenticado, OUTRA, new ConfirmacaoSenhaRequest("incorreta"));

        assertThat(negada.situacao()).isEqualTo(GestaoSessaoService.SituacaoAcao.INVALIDO);
        assertThat(outra.getRevogadaEm()).isNull();
        var aceita = service.revogar(autenticado, OUTRA, new ConfirmacaoSenhaRequest("correta"));

        assertThat(aceita.situacao()).isEqualTo(GestaoSessaoService.SituacaoAcao.SUCESSO);
        assertThat(aceita.resposta().sessaoId()).isEqualTo(OUTRA);
        assertThat(outra.getRevogadaEm()).isNotNull();
        assertThat(atual.getRevogadaEm()).isNull();
    }

    @Test
    void revogacaoGlobalUsaTitularDaSessaoEInformaQuantidade() {
        prepararSessaoAtual();
        prepararConfirmacao();
        when(sessoes.revogarTodasAtivas(eq(USUARIO), any(Instant.class))).thenReturn(2);

        var negada = service.revogarTodas(autenticado, new ConfirmacaoSenhaRequest("incorreta"));
        assertThat(negada.situacao()).isEqualTo(GestaoSessaoService.SituacaoAcao.INVALIDO);
        verify(sessoes, never()).revogarTodasAtivas(eq(USUARIO), any(Instant.class));

        var aceita = service.revogarTodas(autenticado, new ConfirmacaoSenhaRequest("correta"));

        assertThat(aceita.situacao()).isEqualTo(GestaoSessaoService.SituacaoAcao.SUCESSO);
        assertThat(aceita.resposta().sessoesRevogadas()).isEqualTo(2);
        verify(sessoes).revogarTodasAtivas(eq(USUARIO), any(Instant.class));
    }

    private Sessao prepararSessaoAtual() {
        Sessao atual = sessao(ATUAL);
        when(sessoes.findById(ATUAL)).thenReturn(Optional.of(atual));
        when(sessoes.buscarPorIdComBloqueio(ATUAL)).thenReturn(Optional.of(atual));
        when(usuarios.bloquearEConsultarAtivo(USUARIO)).thenReturn(Optional.of(true));
        when(usuarios.findById(USUARIO)).thenReturn(Optional.of(new Usuario("Ana", EMAIL, "hash-senha")));
        return atual;
    }

    private void prepararConfirmacao() {
        when(usuarios.buscarEmailPorId(USUARIO)).thenReturn(Optional.of(EMAIL));
        when(tentativas.buscarComBloqueio(EMAIL)).thenReturn(new TentativaLogin(EMAIL, Instant.now()));
        when(senhas.matches("correta", "hash-senha")).thenReturn(true);
    }

    private void quandoSessaoDeOutroUsuario() {
        when(sessoes.existsByIdAndUsuarioId(OUTRA, USUARIO)).thenReturn(false);
    }

    private Sessao sessao(UUID id) {
        Instant agora = Instant.now();
        Sessao sessao = new Sessao(USUARIO, TOKEN, "a".repeat(64),
                agora.minusSeconds(60), agora.plusSeconds(600), agora.plusSeconds(1200));
        ReflectionTestUtils.setField(sessao, "id", id);
        return sessao;
    }
}
