package br.app.ergane.api.negocio.repository;

import br.app.ergane.api.negocio.model.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NegocioRepository extends JpaRepository<Negocio, UUID> {
}
