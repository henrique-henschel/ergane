package br.app.ergane.api.identidade.repository;

import br.app.ergane.api.identidade.model.AcessoNegocio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AcessoNegocioRepository extends JpaRepository<AcessoNegocio, UUID> {

    Optional<AcessoNegocio> findByUsuarioIdAndContaIdAndAtivoTrue(UUID usuarioId, UUID contaId);

}
