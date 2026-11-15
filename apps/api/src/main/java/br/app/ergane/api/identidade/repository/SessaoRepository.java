package br.app.ergane.api.identidade.repository;

import br.app.ergane.api.identidade.model.Sessao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

public interface SessaoRepository extends JpaRepository<Sessao, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Sessao s WHERE s.id = :id")
    Optional<Sessao> buscarPorIdComBloqueio(@Param("id") UUID id);

    @Query(value = "SELECT * FROM sessoes WHERE refresh_token_hash = :hash FOR UPDATE", nativeQuery = true)
    Optional<Sessao> buscarPorRefreshComBloqueio(@Param("hash") String hash);

    boolean existsByIdAndUsuarioId(UUID id, UUID usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Sessao s WHERE s.id = :id AND s.usuarioId = :usuarioId")
    Optional<Sessao> buscarPropriaComBloqueio(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    @Query("""
            SELECT s FROM Sessao s
            WHERE s.usuarioId = :usuarioId AND s.revogadaEm IS NULL
              AND s.expiraEm > :agora AND s.limiteAbsolutoEm > :agora
            ORDER BY s.criadoEm DESC, s.id ASC
            """)
    List<Sessao> listarAtivas(@Param("usuarioId") UUID usuarioId, @Param("agora") Instant agora);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE sessoes SET revogada_em = greatest(:agora, criado_em)
            WHERE usuario_id = :usuarioId AND revogada_em IS NULL
              AND expira_em > :agora AND limite_absoluto_em > :agora
            """, nativeQuery = true)
    int revogarTodasAtivas(@Param("usuarioId") UUID usuarioId, @Param("agora") Instant agora);

}
