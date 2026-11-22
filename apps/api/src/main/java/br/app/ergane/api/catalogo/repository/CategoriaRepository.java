package br.app.ergane.api.catalogo.repository;

import br.app.ergane.api.catalogo.model.Categoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    List<Categoria> findAllByContaIdOrderByNomeAsc(UUID contaId);

    Optional<Categoria> findByContaIdAndId(UUID contaId, UUID id);

    boolean existsByContaIdAndNomeIgnoreCase(UUID contaId, String nome);

    boolean existsByContaIdAndNomeIgnoreCaseAndIdNot(UUID contaId, String nome, UUID id);

    boolean existsByContaIdAndId(UUID contaId, UUID id);

}
