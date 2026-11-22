package br.app.ergane.api.negocio.repository;

import br.app.ergane.api.negocio.model.AcessoNegocio;
import br.app.ergane.api.negocio.dto.NegocioResumoResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AcessoNegocioRepository extends JpaRepository<AcessoNegocio, UUID> {

    Optional<AcessoNegocio> findByUsuarioIdAndNegocioIdAndAtivoTrue(UUID usuarioId, UUID negocioId);

    boolean existsByUsuarioIdAndNegocioIdAndAtivoTrue(UUID usuarioId, UUID negocioId);

    @Query("""
            select new br.app.ergane.api.negocio.dto.NegocioResumoResponse(negocio.id, negocio.nome, acesso.papel)
            from AcessoNegocio acesso join Negocio negocio on negocio.id = acesso.negocioId
            where acesso.usuarioId = :usuarioId and acesso.ativo = true
            order by negocio.nome, negocio.id
            """)
    List<NegocioResumoResponse> listarAutorizados(@Param("usuarioId") UUID usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select acesso from AcessoNegocio acesso
            where acesso.negocioId = :negocioId
              and (acesso.usuarioId = :usuarioId or acesso.id = :acessoId)
            order by acesso.id
            """)
    List<AcessoNegocio> bloquearAcessosParaRevogacao(@Param("negocioId") UUID negocioId,
                                                     @Param("usuarioId") UUID usuarioId,
                                                     @Param("acessoId") UUID acessoId);

}
