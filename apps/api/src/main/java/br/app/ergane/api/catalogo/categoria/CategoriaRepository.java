package br.app.ergane.api.catalogo.categoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    List<Categoria> findAllByNegocioIdOrderByNomeAsc(UUID negocioId);

    Optional<Categoria> findByNegocioIdAndId(UUID negocioId, UUID id);

    boolean existsByNegocioIdAndNomeIgnoreCase(UUID negocioId, String nome);

    boolean existsByNegocioIdAndNomeIgnoreCaseAndIdNot(UUID negocioId, String nome, UUID id);

}
