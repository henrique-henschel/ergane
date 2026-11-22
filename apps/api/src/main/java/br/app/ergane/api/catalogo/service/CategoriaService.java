package br.app.ergane.api.catalogo.service;

import br.app.ergane.api.catalogo.model.Categoria;
import br.app.ergane.api.catalogo.repository.CategoriaRepository;
import br.app.ergane.api.compartilhado.exception.RecursoNaoEncontradoException;
import br.app.ergane.api.compartilhado.exception.RegraNegocioException;
import br.app.ergane.api.compartilhado.security.ContextoConta;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ContextoConta contextoConta;

    public CategoriaService(CategoriaRepository categoriaRepository, ContextoConta contextoConta) {
        this.categoriaRepository = categoriaRepository;
        this.contextoConta = contextoConta;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categoriaRepository.findAllByContaIdOrderByNomeAsc(contextoConta.getContaId());
    }

    @Transactional(readOnly = true)
    public Categoria buscar(UUID id) {
        return categoriaRepository.findByContaIdAndId(contextoConta.getContaId(), id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Erro! Categoria não encontrada."));
    }

    @Transactional
    public Categoria criar(String nome) {
        String nomeLimpo = nome.trim();

        if (categoriaRepository.existsByContaIdAndNomeIgnoreCase(contextoConta.getContaId(), nomeLimpo)) {
            throw new RegraNegocioException("Erro! Já existe uma categoria com este nome.");
        }

        return categoriaRepository.save(new Categoria(contextoConta.getContaId(), nomeLimpo));
    }

    @Transactional
    public Categoria renomear(UUID id, String nome) {
        Categoria categoria = buscar(id);
        String nomeLimpo = nome.trim();

        if (categoriaRepository.existsByContaIdAndNomeIgnoreCaseAndIdNot(contextoConta.getContaId(), nomeLimpo, id)) {
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
        return categoriaRepository.existsByContaIdAndId(contextoConta.getContaId(), id);
    }

}
