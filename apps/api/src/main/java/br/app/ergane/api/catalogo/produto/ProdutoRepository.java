package br.app.ergane.api.catalogo.produto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ProdutoRepository extends JpaRepository<Produto, UUID> {

    Optional<Produto> findByNegocioIdAndId(UUID negocioId, UUID id);

    @Query("""
            select p from Produto p
            where p.negocioId = :negocioId
              and (:padraoNome is null or lower(p.nome) like :padraoNome)
              and (:categoriaId is null or p.categoriaId = :categoriaId)
              and (:semCategoria = false or p.categoriaId is null)
              and (:incluirArquivados = true or p.arquivadoEm is null)
            order by lower(p.nome), p.diferenciador
            """)
    List<Produto> buscarComFiltros(@Param("negocioId") UUID negocioId,
                                   @Param("padraoNome") String padraoNome,
                                   @Param("categoriaId") UUID categoriaId,
                                   @Param("semCategoria") boolean semCategoria,
                                   @Param("incluirArquivados") boolean incluirArquivados);

    @Query("""
            select case when count(p) > 0 then true else false end
            from Produto p
            where p.negocioId = :negocioId
              and p.arquivadoEm is null
              and lower(p.nome) = lower(:nome)
              and lower(coalesce(p.diferenciador, '')) = lower(:diferenciador)
              and (:ignorarId is null or p.id <> :ignorarId)
            """)
    boolean existeAtivoComMesmoNome(@Param("negocioId") UUID negocioId,
                                    @Param("nome") String nome,
                                    @Param("diferenciador") String diferenciador,
                                    @Param("ignorarId") UUID ignorarId);

}
