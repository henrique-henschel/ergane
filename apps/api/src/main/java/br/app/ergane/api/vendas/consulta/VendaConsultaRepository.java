package br.app.ergane.api.vendas.consulta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface VendaConsultaRepository extends JpaRepository<Venda, UUID> {

    @Query(
        value = """
            select v.*
            from vendas v
            where v.negocio_id = :negocioId
              and (:origem is null or v.origem = :origem)
              and (:situacao is null or v.situacao = :situacao)
              and (:produtoId is null or exists (
                  select 1
                  from itens_venda i
                  where i.venda_id = v.id
                    and i.item_vendavel_id = :produtoId
              ))
              and (:formaRecebimento is null or exists (
                  select 1
                  from recebimentos r
                  where r.venda_id = v.id
                    and r.forma = :formaRecebimento
              ))
              and (
                  (v.registrada_em is not null
                   and (:deInstant is null
                        or v.registrada_em >= cast(:deInstant as timestamptz))
                   and (:ateInstant is null
                        or v.registrada_em <= cast(:ateInstant as timestamptz)))
                  or
                  (v.data_ocorrencia is not null
                   and (:deInstant is null
                        or v.data_ocorrencia >= cast(:deData as date))
                   and (:ateInstant is null
                        or v.data_ocorrencia <= cast(:ateData as date)))
              )
            -- A data UTC serve apenas como chave de agrupamento para intercalar precisões.
            -- No mesmo dia, instantes conhecidos vêm primeiro;
            -- registros só com data não recebem horário.
            order by
              coalesce(
                  (v.registrada_em at time zone 'UTC')::date,
                  v.data_ocorrencia
              ) desc,
              case when v.registrada_em is null then 1 else 0 end asc,
              v.registrada_em desc nulls last,
              v.id desc
            """,
        countQuery = """
            select count(*)
            from vendas v
            where v.negocio_id = :negocioId
              and (:origem is null or v.origem = :origem)
              and (:situacao is null or v.situacao = :situacao)
              and (:produtoId is null or exists (
                  select 1
                  from itens_venda i
                  where i.venda_id = v.id
                    and i.item_vendavel_id = :produtoId
              ))
              and (:formaRecebimento is null or exists (
                  select 1
                  from recebimentos r
                  where r.venda_id = v.id
                    and r.forma = :formaRecebimento
              ))
              and (
                  (v.registrada_em is not null
                   and (:deInstant is null
                        or v.registrada_em >= cast(:deInstant as timestamptz))
                   and (:ateInstant is null
                        or v.registrada_em <= cast(:ateInstant as timestamptz)))
                  or
                  (v.data_ocorrencia is not null
                   and (:deInstant is null
                        or v.data_ocorrencia >= cast(:deData as date))
                   and (:ateInstant is null
                        or v.data_ocorrencia <= cast(:ateData as date)))
              )
            """,
        nativeQuery = true
    )
    Page<Venda> consultar(
            @Param("negocioId") UUID negocioId,
            @Param("deInstant") Instant deInstant,
            @Param("deData") LocalDate deData,
            @Param("ateInstant") Instant ateInstant,
            @Param("ateData") LocalDate ateData,
            @Param("origem") String origem,
            @Param("situacao") String situacao,
            @Param("produtoId") UUID produtoId,
            @Param("formaRecebimento") String formaRecebimento,
            Pageable pageable
    );

    Optional<Venda> findByNegocioIdAndId(UUID negocioId, UUID id);
}
