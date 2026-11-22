package br.app.ergane.api.identidade.service;

import br.app.ergane.api.compartilhado.security.UsuarioAutenticado;
import br.app.ergane.api.identidade.dto.ConfirmacaoSenhaRequest;
import br.app.ergane.api.negocio.service.NegocioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GestaoAcessoNegocioServiceTests {

    private static final UUID USUARIO = UUID.fromString("00000000-0000-0000-0000-000000000031");
    private static final UUID SESSAO = UUID.fromString("00000000-0000-0000-0000-000000000032");
    private static final UUID TOKEN = UUID.fromString("00000000-0000-0000-0000-000000000033");
    private static final UUID NEGOCIO = UUID.fromString("00000000-0000-0000-0000-000000000034");
    private static final UUID ACESSO = UUID.fromString("00000000-0000-0000-0000-000000000035");

    private final GestaoSessaoService sessoes = mock(GestaoSessaoService.class);
    private final NegocioService negocios = mock(NegocioService.class);
    private final GestaoAcessoNegocioService service = new GestaoAcessoNegocioService(sessoes, negocios);
    private final UsuarioAutenticado usuario = new UsuarioAutenticado(USUARIO, SESSAO, TOKEN, null);

    @ParameterizedTest
    @CsvSource({"INVALIDO, 0", "BLOQUEADO, 30"})
    void confirmacaoNegadaNaoAlteraVinculo(GestaoSessaoService.SituacaoAcao situacao, long espera) {
        when(sessoes.confirmarSenha(usuario, "senha")).thenReturn(new GestaoSessaoService.Confirmacao(situacao, espera));

        var resultado = service.revogar(usuario, NEGOCIO, ACESSO, new ConfirmacaoSenhaRequest("senha"));

        assertThat(resultado.situacao()).isEqualTo(situacao);
        assertThat(resultado.segundosEspera()).isEqualTo(espera);
        verifyNoInteractions(negocios);
    }

    @Test
    void confirmaSenhaAntesDeRevogarComIdentidadeDoTitular() {
        when(sessoes.confirmarSenha(usuario, "senha"))
                .thenReturn(new GestaoSessaoService.Confirmacao(GestaoSessaoService.SituacaoAcao.SUCESSO, 0));

        var resultado = service.revogar(usuario, NEGOCIO, ACESSO, new ConfirmacaoSenhaRequest("senha"));

        assertThat(resultado.situacao()).isEqualTo(GestaoSessaoService.SituacaoAcao.SUCESSO);
        var ordem = inOrder(sessoes, negocios);
        ordem.verify(sessoes).confirmarSenha(usuario, "senha");
        ordem.verify(negocios).revogarAcesso(USUARIO, NEGOCIO, ACESSO);
        ordem.verifyNoMoreInteractions();
    }
}
