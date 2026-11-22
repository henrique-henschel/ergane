package br.app.ergane.api.negocio;

import br.app.ergane.api.compartilhado.exception.RecursoNaoEncontradoException;
import br.app.ergane.api.negocio.model.AcessoNegocio;
import br.app.ergane.api.negocio.model.PapelNegocio;
import br.app.ergane.api.negocio.repository.AcessoNegocioRepository;
import br.app.ergane.api.negocio.repository.NegocioRepository;
import br.app.ergane.api.negocio.service.NegocioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NegocioServiceTests {

    private static final UUID NEGOCIO = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID GESTOR = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final UUID USUARIO_ALVO = UUID.fromString("00000000-0000-0000-0000-000000000013");
    private static final UUID ACESSO_GESTOR = UUID.fromString("00000000-0000-0000-0000-000000000014");
    private static final UUID ACESSO_ALVO = UUID.fromString("00000000-0000-0000-0000-000000000015");

    private final AcessoNegocioRepository acessos = mock(AcessoNegocioRepository.class);
    private final NegocioService service = new NegocioService(mock(NegocioRepository.class), acessos);

    @ParameterizedTest
    @CsvSource({
            "PROPRIETARIO, ADMINISTRADOR",
            "PROPRIETARIO, FUNCIONARIO",
            "ADMINISTRADOR, FUNCIONARIO"
    })
    void gestorAutorizadoRevogaSomenteVinculoAlvo(PapelNegocio papelGestor, PapelNegocio papelAlvo) {
        AcessoNegocio gestor = vinculo(GESTOR, ACESSO_GESTOR, papelGestor);
        AcessoNegocio alvo = vinculo(USUARIO_ALVO, ACESSO_ALVO, papelAlvo);
        when(acessos.bloquearAcessosParaRevogacao(NEGOCIO, GESTOR, ACESSO_ALVO))
                .thenReturn(List.of(gestor, alvo));

        service.revogarAcesso(GESTOR, NEGOCIO, ACESSO_ALVO);

        assertThat(gestor.isAtivo()).isTrue();
        assertThat(alvo.isAtivo()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "ADMINISTRADOR, ADMINISTRADOR",
            "ADMINISTRADOR, PROPRIETARIO",
            "FUNCIONARIO, FUNCIONARIO"
    })
    void hierarquiaNegaRevogacaoSemAlterarVinculo(PapelNegocio papelGestor, PapelNegocio papelAlvo) {
        AcessoNegocio gestor = vinculo(GESTOR, ACESSO_GESTOR, papelGestor);
        AcessoNegocio alvo = vinculo(USUARIO_ALVO, ACESSO_ALVO, papelAlvo);
        when(acessos.bloquearAcessosParaRevogacao(NEGOCIO, GESTOR, ACESSO_ALVO))
                .thenReturn(List.of(gestor, alvo));

        assertThatThrownBy(() -> service.revogarAcesso(GESTOR, NEGOCIO, ACESSO_ALVO))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThat(alvo.isAtivo()).isTrue();
    }

    @Test
    void administradorComVinculoRevogadoNaoPodeRevogarFuncionario() {
        AcessoNegocio gestor = vinculo(GESTOR, ACESSO_GESTOR, PapelNegocio.ADMINISTRADOR);
        gestor.revogar();
        AcessoNegocio alvo = vinculo(USUARIO_ALVO, ACESSO_ALVO, PapelNegocio.FUNCIONARIO);
        when(acessos.bloquearAcessosParaRevogacao(NEGOCIO, GESTOR, ACESSO_ALVO))
                .thenReturn(List.of(gestor, alvo));

        assertThatThrownBy(() -> service.revogarAcesso(GESTOR, NEGOCIO, ACESSO_ALVO))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThat(alvo.isAtivo()).isTrue();
    }

    private AcessoNegocio vinculo(UUID usuario, UUID id, PapelNegocio papel) {
        AcessoNegocio acesso = new AcessoNegocio(usuario, NEGOCIO, papel);
        ReflectionTestUtils.setField(acesso, "id", id);
        return acesso;
    }
}
