package br.app.ergane.api.catalogo.repository;

import br.app.ergane.api.catalogo.model.Produto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {

    Optional<Produto> findByContaIdAndId(UUID contaId, UUID id);

    @Query("""
            select p from Produto p
            where p.contaId = :contaId
              and (:padraoNome is null or lower(p.nome) like :padraoNome)
              and (:categoriaId is null or p.categoriaId = :categoriaId)
              and (:semCategoria = false or p.categoriaId is null)
              and (:incluirArquivados = true or p.arquivadoEm is null)
            order by lower(p.nome), p.diferenciador, p.id
            """)
    List<Produto> buscarComFiltros(@Param("contaId") UUID contaId,
                                   @Param("padraoNome") String padraoNome,
                                   @Param("categoriaId") UUID categoriaId,
                                   @Param("semCategoria") boolean semCategoria,
                                   @Param("incluirArquivados") boolean incluirArquivados);

    @Query("""
            select case when count(p) > 0 then true else false end
            from Produto p
            where p.contaId = :contaId
              and p.arquivadoEm is null
              and lower(p.nome) = lower(:nome)
              and lower(coalesce(p.diferenciador, '')) = lower(:diferenciador)
              and (:ignorarId is null or p.id <> :ignorarId)
            """)
    boolean existeAtivoComMesmoNome(@Param("contaId") UUID contaId,
                                    @Param("nome") String nome,
                                    @Param("diferenciador") String diferenciador,
                                    @Param("ignorarId") UUID ignorarId);

}
