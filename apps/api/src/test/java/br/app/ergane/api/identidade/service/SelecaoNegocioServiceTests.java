package br.app.ergane.api.identidade.service;

import br.app.ergane.api.compartilhado.exception.RecursoNaoEncontradoException;
import br.app.ergane.api.compartilhado.security.UsuarioAutenticado;
import br.app.ergane.api.identidade.dto.SelecionarNegocioRequest;
import br.app.ergane.api.identidade.model.Sessao;
import br.app.ergane.api.identidade.repository.SessaoRepository;
import br.app.ergane.api.identidade.repository.UsuarioRepository;
import br.app.ergane.api.negocio.service.NegocioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SelecaoNegocioServiceTests {

    private static final UUID USUARIO = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SESSAO = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID TOKEN = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID NEGOCIO = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID OUTRO_NEGOCIO = UUID.fromString("00000000-0000-0000-0000-000000000005");

    private final SessaoRepository sessoes = mock(SessaoRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final NegocioService negocios = mock(NegocioService.class);
    private final ValidacaoSessaoService validacao = mock(ValidacaoSessaoService.class);
    private final SelecaoNegocioService service = new SelecaoNegocioService(sessoes, validacao, negocios, usuarios);
    private final UsuarioAutenticado usuario = new UsuarioAutenticado(USUARIO, SESSAO, TOKEN, OUTRO_NEGOCIO);

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void consultaVerificaVinculoDoContextoAtualDaSessao(boolean ativo) {
        when(validacao.validar(USUARIO, SESSAO, TOKEN))
                .thenReturn(new UsuarioAutenticado(USUARIO, SESSAO, TOKEN, NEGOCIO));
        when(negocios.temAcesso(USUARIO, NEGOCIO)).thenReturn(ativo);

        assertThat(service.consultar(usuario).negocioId()).isEqualTo(ativo ? NEGOCIO : null);
    }

    @Test
    void selecionaNegocioAutorizadoEGravaPreferenciaDoUsuario() {
        Sessao sessao = prepararSessao();

        var resposta = service.selecionar(usuario, new SelecionarNegocioRequest(NEGOCIO));

        assertThat(resposta.negocioId()).isEqualTo(NEGOCIO);
        assertThat(sessao.getNegocioAtivoId()).isEqualTo(NEGOCIO);
        verify(usuarios).salvarUltimoNegocio(USUARIO, NEGOCIO);
        verify(sessoes).flush();
    }

    @Test
    void selecaoSemAcessoMantemContextoEPreferencia() {
        Sessao sessao = prepararSessao();
        doThrow(new RecursoNaoEncontradoException("Negócio não encontrado."))
                .when(negocios).validarAcesso(USUARIO, NEGOCIO);

        assertThatThrownBy(() -> service.selecionar(usuario, new SelecionarNegocioRequest(NEGOCIO)))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        assertThat(sessao.getNegocioAtivoId()).isEqualTo(OUTRO_NEGOCIO);
        verify(usuarios, never()).salvarUltimoNegocio(USUARIO, NEGOCIO);
        verify(sessoes, never()).flush();
    }

    private Sessao prepararSessao() {
        Instant inicio = Instant.parse("2026-01-01T00:00:00Z");
        Sessao sessao = new Sessao(USUARIO, TOKEN, "a".repeat(64),
                inicio, inicio.plusSeconds(600), inicio.plusSeconds(1200));
        sessao.selecionarNegocio(OUTRO_NEGOCIO);
        when(usuarios.bloquearEConsultarAtivo(USUARIO)).thenReturn(Optional.of(true));
        when(sessoes.buscarPorIdComBloqueio(SESSAO)).thenReturn(Optional.of(sessao));
        return sessao;
    }
}
