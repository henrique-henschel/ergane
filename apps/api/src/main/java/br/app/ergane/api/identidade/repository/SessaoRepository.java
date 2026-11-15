package br.app.ergane.api.identidade.repository;

import br.app.ergane.api.identidade.model.Sessao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SessaoRepository extends JpaRepository<Sessao, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Sessao s WHERE s.id = :id")
    Optional<Sessao> buscarPorIdComBloqueio(@Param("id") UUID id);

    @Query(value = "SELECT * FROM sessoes WHERE refresh_token_hash = :hash FOR UPDATE", nativeQuery = true)
    Optional<Sessao> buscarPorRefreshComBloqueio(@Param("hash") String hash);

}
