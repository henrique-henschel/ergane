package br.app.ergane.api.identidade.repository;

import br.app.ergane.api.identidade.model.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SessaoRepository extends JpaRepository<Sessao, UUID> {

    @Query(value = "SELECT * FROM sessoes WHERE refresh_token_hash = :hash FOR UPDATE", nativeQuery = true)
    Optional<Sessao> buscarPorRefreshComBloqueio(@Param("hash") String hash);

}
