package br.app.ergane.api.identidade.repository;

import br.app.ergane.api.identidade.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    @Query(
            value = "SELECT * FROM usuarios WHERE lower(btrim(email)) = lower(btrim(:email))",
            nativeQuery = true
    )
    Optional<Usuario> findByEmailNormalizado(@Param("email") String email);

    @Query(value = "SELECT email FROM usuarios WHERE id = :id", nativeQuery = true)
    Optional<String> buscarEmailPorId(@Param("id") UUID id);

    @Query(value = "SELECT ativo FROM usuarios WHERE id = :id FOR NO KEY UPDATE", nativeQuery = true)
    Optional<Boolean> bloquearEConsultarAtivo(@Param("id") UUID id);

}
