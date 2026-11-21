package br.app.ergane.api.negocio.repository;

import br.app.ergane.api.negocio.model.AcessoNegocio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AcessoNegocioRepository extends JpaRepository<AcessoNegocio, UUID> {

    Optional<AcessoNegocio> findByUsuarioIdAndNegocioIdAndAtivoTrue(UUID usuarioId, UUID negocioId);

}
