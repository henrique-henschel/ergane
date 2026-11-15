package br.app.ergane.api.identidade.repository;

import br.app.ergane.api.identidade.model.TentativaLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface TentativaLoginRepository extends JpaRepository<TentativaLogin, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO tentativas_login (id, email_normalizado, falhas, janela_inicio_em)
            VALUES (gen_random_uuid(), lower(btrim(:email)), 0, :agora)
            ON CONFLICT (email_normalizado) DO NOTHING
            """, nativeQuery = true)
    void criarSeAusente(@Param("email") String email, @Param("agora") Instant agora);

    @Query(value = """
            SELECT * FROM tentativas_login
            WHERE email_normalizado = lower(btrim(:email)) FOR UPDATE
            """, nativeQuery = true)
    TentativaLogin buscarComBloqueio(@Param("email") String email);

}
