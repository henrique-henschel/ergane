package br.app.ergane.api.vendas.consulta.repository;

import br.app.ergane.api.vendas.consulta.dto.ConsultaVendasFiltro;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * faz as consultas sql de vendas e limita os resultados ao negocio informado
 * pelo servico.
 */
@Repository
public class ConsultaVendasRepository {

    private static final RowMapper<VendaResumoLinha> RESUMO_MAPPER = (rs, row) ->
            new VendaResumoLinha(uuid(rs, "id"), rs.getObject("ocorrencia_data", LocalDate.class),
                    rs.getObject("ocorrencia_hora", LocalTime.class), rs.getString("ocorrencia_fuso"),
                    rs.getBigDecimal("total_registrado"), rs.getString("origem"),
                    rs.getString("situacao"), rs.getBoolean("detalhada"));

    private final JdbcTemplate jdbcTemplate;

    public ConsultaVendasRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PaginaConsulta consultar(UUID negocioId, ConsultaVendasFiltro filtro) {
        FiltroSql where = montarFiltro(negocioId, filtro);
        String fromAndWhere = " from vendas v " + where.sql();
        long total = jdbcTemplate.queryForObject(
                "select count(*)" + fromAndWhere,
                Long.class,
                where.parametros().toArray());

        int pagina = filtro.paginaEfetiva();
        int tamanho = filtro.tamanhoEfetivo();
        long offset = (long) pagina * tamanho;
        List<Object> parametros = new ArrayList<>(where.parametros());
        parametros.add(tamanho);
        parametros.add(offset);

        String sql = """
                select v.id, v.ocorrencia_data, v.ocorrencia_hora, v.ocorrencia_fuso,
                       v.total_registrado, v.origem, v.situacao,
                       exists (select 1 from itens_venda i where i.venda_id = v.id) as detalhada
                """ + fromAndWhere + """
                 order by v.ocorrencia_data desc, v.ocorrencia_hora desc nulls last, v.id desc
                 limit ? offset ?
                """;

        List<VendaResumoLinha> registros = jdbcTemplate.query(sql, RESUMO_MAPPER, parametros.toArray());
        int totalPaginas = total == 0 ? 0 : (int) ((total + tamanho - 1) / tamanho);
        return new PaginaConsulta(registros, total, pagina, tamanho, totalPaginas);
    }

    public Optional<VendaDetalheLinha> buscarDetalhe(UUID negocioId, UUID vendaId) {
        List<VendaDetalheLinha> resultados = jdbcTemplate.query("""
                select id, ocorrencia_data, ocorrencia_hora, ocorrencia_fuso, registrada_em,
                       total_registrado, origem, situacao, forma_pagamento_informada,
                       encomenda_origem_id
                from vendas
                where negocio_id = ? and id = ?
                """, (rs, row) -> new VendaDetalheLinha(
                uuid(rs, "id"),
                rs.getObject("ocorrencia_data", LocalDate.class),
                rs.getObject("ocorrencia_hora", LocalTime.class),
                rs.getString("ocorrencia_fuso"),
                instant(rs, "registrada_em"),
                rs.getBigDecimal("total_registrado"),
                rs.getString("origem"),
                rs.getString("situacao"),
                rs.getString("forma_pagamento_informada"),
                uuid(rs, "encomenda_origem_id")
        ), negocioId, vendaId);
        return resultados.stream().findFirst();
    }

    public List<ItemLinha> listarItens(UUID negocioId, UUID vendaId) {
        return jdbcTemplate.query("""
                select i.id, i.produto_id, i.nome_snapshot, i.diferenciador_snapshot,
                       i.quantidade, i.preco_unitario_snapshot, i.custo_unitario_snapshot
                from itens_venda i
                join vendas v on v.id = i.venda_id
                where v.negocio_id = ? and v.id = ?
                order by i.id asc
                """, (rs, row) -> new ItemLinha(
                uuid(rs, "id"), uuid(rs, "produto_id"), rs.getString("nome_snapshot"),
                rs.getString("diferenciador_snapshot"), rs.getBigDecimal("quantidade"),
                rs.getBigDecimal("preco_unitario_snapshot"), rs.getBigDecimal("custo_unitario_snapshot")
        ), negocioId, vendaId);
    }

    private FiltroSql montarFiltro(UUID negocioId, ConsultaVendasFiltro filtro) {
        StringBuilder sql = new StringBuilder("where v.negocio_id = ?");
        List<Object> parametros = new ArrayList<>();
        parametros.add(negocioId);
        adicionar(sql, parametros, "v.ocorrencia_data >= ?", filtro.de());
        adicionar(sql, parametros, "v.ocorrencia_data <= ?", filtro.ate());
        adicionar(sql, parametros, "v.origem = ?", filtro.origemEfetiva());
        adicionar(sql, parametros, "v.situacao = ?", filtro.situacao());
        adicionar(sql, parametros, "v.forma_pagamento_informada = ?", filtro.formaPagamentoInformada());
        if (filtro.produtoId() != null) {
            sql.append(" and exists (select 1 from itens_venda i where i.venda_id = v.id and i.produto_id = ?)");
            parametros.add(filtro.produtoId());
        }
        return new FiltroSql(sql.toString(), parametros);
    }

    private static void adicionar(StringBuilder sql, List<Object> parametros, String condicao, Object valor) {
        if (valor != null) {
            sql.append(" and ").append(condicao);
            parametros.add(valor);
        }
    }

    private static UUID uuid(ResultSet rs, String coluna) throws SQLException {
        return rs.getObject(coluna, UUID.class);
    }

    /**
     * converte o horario do banco para um instante. se estiver vazio, retorna
     * nulo.
     */
    private static Instant instant(ResultSet rs, String coluna) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(coluna);
        return timestamp == null ? null : timestamp.toInstant();
    }

    /** guarda as condicoes e os valores usados na consulta sql. */
    private record FiltroSql(String sql, List<Object> parametros) { }

    /** guarda os resultados da consulta paginada antes de criar a resposta. */
    public record PaginaConsulta(List<VendaResumoLinha> registros, long totalRegistros,
                                 int pagina, int tamanho, int totalPaginas) { }

    /** guarda os dados da venda usados no resumo da lista. */
    public record VendaResumoLinha(UUID id, LocalDate dataOcorrencia, LocalTime horaOcorrencia,
                                   String fusoOcorrencia, BigDecimal valor, String origem,
                                   String situacao, boolean detalhada) { }

    /** guarda os dados da venda usados na resposta de detalhe. */
    public record VendaDetalheLinha(UUID id, LocalDate dataOcorrencia, LocalTime horaOcorrencia,
                                    String fusoOcorrencia, Instant registradaEm,
                                    BigDecimal totalRegistrado, String origem, String situacao,
                                    String formaPagamentoInformada, UUID encomendaOrigemId) { }

    /** guarda os dados salvos do item usados na resposta de detalhe. */
    public record ItemLinha(UUID id, UUID produtoId, String nome, String diferenciador,
                            BigDecimal quantidade, BigDecimal precoUnitario, BigDecimal custoUnitario) { }
}
