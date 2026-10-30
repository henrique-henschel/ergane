package br.app.ergane.api.catalogo.categoria;

import br.app.ergane.api.compartilhado.RecursoNaoEncontradoException;
import br.app.ergane.api.compartilhado.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoriaService {

    // Temporario: sera trocado pelo negocio do usuario logado no UC01
    private static final UUID NEGOCIO_FIXO = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categoriaRepository.findAllByNegocioIdOrderByNomeAsc(NEGOCIO_FIXO);
    }

    @Transactional(readOnly = true)
    public Categoria buscar(UUID id) {
        return categoriaRepository.findByNegocioIdAndId(NEGOCIO_FIXO, id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Erro! Categoria não encontrada."));
    }

    @Transactional
    public Categoria criar(String nome) {
        String nomeLimpo = nome.trim();

        if (categoriaRepository.existsByNegocioIdAndNomeIgnoreCase(NEGOCIO_FIXO, nomeLimpo)) {
            throw new RegraNegocioException("Erro! Já existe uma categoria com este nome.");
        }

        return categoriaRepository.save(new Categoria(NEGOCIO_FIXO, nomeLimpo));
    }

    @Transactional
    public Categoria renomear(UUID id, String nome) {
        Categoria categoria = buscar(id);
        String nomeLimpo = nome.trim();

        if (categoriaRepository.existsByNegocioIdAndNomeIgnoreCaseAndIdNot(NEGOCIO_FIXO, nomeLimpo, id)) {
            throw new RegraNegocioException("Erro! Já existe uma categoria com este nome.");
        }

        categoria.renomear(nomeLimpo);
        return categoria;
    }

    @Transactional
    public void excluir(UUID id) {
        Categoria categoria = buscar(id);
        // TODO UC03: bloquear a exclusao se houver produtos vinculados
        categoriaRepository.delete(categoria);
    }

}
