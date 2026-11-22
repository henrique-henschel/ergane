package br.app.ergane.api.catalogo.service;

import br.app.ergane.api.catalogo.model.Categoria;
import br.app.ergane.api.catalogo.repository.CategoriaRepository;
import br.app.ergane.api.compartilhado.exception.RecursoNaoEncontradoException;
import br.app.ergane.api.compartilhado.security.ContextoNegocio;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class CategoriaIsolamentoTests {

    private static final UUID PADARIA = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID MERCEARIA = UUID.fromString("00000000-0000-0000-0000-000000000020");
    private static final UUID CATEGORIA_ALHEIA = UUID.fromString("00000000-0000-0000-0000-000000000021");

    private final CategoriaRepository repository = mock(CategoriaRepository.class);
    private final ContextoNegocio contexto = new ContextoNegocio();
    private final CategoriaService service = new CategoriaService(repository, contexto);

    @Test
    void listaSomenteCategoriasDoNegocioSelecionado() {
        contexto.definirNegocioAutorizado(PADARIA);
        Categoria paes = new Categoria(PADARIA, "Pães");
        when(repository.findAllByNegocioIdOrderByNomeAsc(PADARIA)).thenReturn(List.of(paes));

        assertThat(service.listar()).containsExactly(paes);
    }

    @Test
    void conhecerIdAlheioNaoPermiteConsultarRenomearOuExcluir() {
        Categoria cereais = new Categoria(MERCEARIA, "Cereais");
        when(repository.findByNegocioIdAndId(MERCEARIA, CATEGORIA_ALHEIA)).thenReturn(Optional.of(cereais));
        contexto.definirNegocioAutorizado(MERCEARIA);
        assertThat(service.buscar(CATEGORIA_ALHEIA)).isSameAs(cereais);

        contexto.definirNegocioAutorizado(PADARIA);

        assertThatThrownBy(() -> service.buscar(CATEGORIA_ALHEIA)).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.renomear(CATEGORIA_ALHEIA, "Outro nome"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.excluir(CATEGORIA_ALHEIA)).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThat(cereais.getNome()).isEqualTo("Cereais");
        verify(repository).findByNegocioIdAndId(MERCEARIA, CATEGORIA_ALHEIA);
        verify(repository, times(3)).findByNegocioIdAndId(PADARIA, CATEGORIA_ALHEIA);
        verifyNoMoreInteractions(repository);
    }
}
