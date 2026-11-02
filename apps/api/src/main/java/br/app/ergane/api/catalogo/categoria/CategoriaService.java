package br.app.ergane.api.catalogo.categoria;

import br.app.ergane.api.compartilhado.ContextoNegocio;
import br.app.ergane.api.compartilhado.RecursoNaoEncontradoException;
import br.app.ergane.api.compartilhado.RegraNegocioException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ContextoNegocio contextoNegocio;

    public CategoriaService(CategoriaRepository categoriaRepository, ContextoNegocio contextoNegocio) {
        this.categoriaRepository = categoriaRepository;
        this.contextoNegocio = contextoNegocio;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categoriaRepository.findAllByNegocioIdOrderByNomeAsc(contextoNegocio.negocioId());
    }

    @Transactional(readOnly = true)
    public Categoria buscar(UUID id) {
        return categoriaRepository.findByNegocioIdAndId(contextoNegocio.negocioId(), id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Erro! Categoria não encontrada."));
    }

    @Transactional
    public Categoria criar(String nome) {
        String nomeLimpo = nome.trim();

        if (categoriaRepository.existsByNegocioIdAndNomeIgnoreCase(contextoNegocio.negocioId(), nomeLimpo)) {
            throw new RegraNegocioException("Erro! Já existe uma categoria com este nome.");
        }

        return categoriaRepository.save(new Categoria(contextoNegocio.negocioId(), nomeLimpo));
    }

    @Transactional
    public Categoria renomear(UUID id, String nome) {
        Categoria categoria = buscar(id);
        String nomeLimpo = nome.trim();

        if (categoriaRepository.existsByNegocioIdAndNomeIgnoreCaseAndIdNot(contextoNegocio.negocioId(), nomeLimpo, id)) {
            throw new RegraNegocioException("Erro! Já existe uma categoria com este nome.");
        }

        categoria.renomear(nomeLimpo);
        return categoria;
    }

    @Transactional
    public void excluir(UUID id) {
        Categoria categoria = buscar(id);

        try {
            categoriaRepository.delete(categoria);
            categoriaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new RegraNegocioException("Erro! A categoria possui produtos vinculados (inclusive arquivados) e não pode ser excluída. Renomeie-a se precisar reaproveitá-la.");
        }
    }

    @Transactional(readOnly = true)
    public boolean existe(UUID id) {
        return categoriaRepository.existsByNegocioIdAndId(contextoNegocio.negocioId(), id);
    }

}
