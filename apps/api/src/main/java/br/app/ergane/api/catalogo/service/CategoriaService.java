package br.app.ergane.api.catalogo.service;

import br.app.ergane.api.catalogo.model.Categoria;
import br.app.ergane.api.catalogo.repository.CategoriaRepository;
import br.app.ergane.api.compartilhado.exception.RecursoNaoEncontradoException;
import br.app.ergane.api.compartilhado.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoriaService {

    // Temporario: sera trocado pela conta do usuario logado no UC01
    private static final UUID CONTA_FIXA = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categoriaRepository.findAllByContaIdOrderByNomeAsc(CONTA_FIXA);
    }

    @Transactional(readOnly = true)
    public Categoria buscar(UUID id) {
        return categoriaRepository.findByContaIdAndId(CONTA_FIXA, id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Erro! Categoria não encontrada."));
    }

    @Transactional
    public Categoria criar(String nome) {
        String nomeLimpo = nome.trim();

        if (categoriaRepository.existsByContaIdAndNomeIgnoreCase(CONTA_FIXA, nomeLimpo)) {
            throw new RegraNegocioException("Erro! Já existe uma categoria com este nome.");
        }

        return categoriaRepository.save(new Categoria(CONTA_FIXA, nomeLimpo));
    }

    @Transactional
    public Categoria renomear(UUID id, String nome) {
        Categoria categoria = buscar(id);
        String nomeLimpo = nome.trim();

        if (categoriaRepository.existsByContaIdAndNomeIgnoreCaseAndIdNot(CONTA_FIXA, nomeLimpo, id)) {
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
