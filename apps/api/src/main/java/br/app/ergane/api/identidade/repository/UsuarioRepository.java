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

}
