package br.app.ergane.api.vendas.consulta.service;

import br.app.ergane.api.compartilhado.RecursoNaoEncontradoException;
import br.app.ergane.api.compartilhado.security.ContextoNegocio;
import br.app.ergane.api.vendas.consulta.dto.CoberturaHistoricoResponse;
import br.app.ergane.api.vendas.consulta.dto.ConsultaVendasFiltro;
import br.app.ergane.api.vendas.consulta.dto.ConsultaVendasResponse;
import br.app.ergane.api.vendas.consulta.dto.ItemVendaResponse;
import br.app.ergane.api.vendas.consulta.dto.VendaDetalheResponse;
import br.app.ergane.api.vendas.consulta.dto.VendaResumoResponse;
import br.app.ergane.api.vendas.consulta.repository.ConsultaVendasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * cuida das consultas de venda, usa o negocio do usuario e prepara os dados
 * para enviar pela api.
 */
@Service
public class ConsultaVendasService {

    private final ConsultaVendasRepository consultaVendasRepository;
    private final ContextoNegocio contextoNegocio;

    public ConsultaVendasService(ConsultaVendasRepository consultaVendasRepository,
                                 ContextoNegocio contextoNegocio) {
        this.consultaVendasRepository = consultaVendasRepository;
        this.contextoNegocio = contextoNegocio;
    }

    @Transactional(readOnly = true)
    public ConsultaVendasResponse listar(ConsultaVendasFiltro filtro) {
        var pagina = consultaVendasRepository.consultar(contextoNegocio.getNegocioId(), filtro);
        List<VendaResumoResponse> registros = pagina.registros().stream()
                .map(linha -> new VendaResumoResponse(linha.id(), linha.dataOcorrencia(),
                        linha.horaOcorrencia(), linha.fusoOcorrencia(), linha.valor(),
                        linha.origem(), linha.situacao(), linha.detalhada()))
                .toList();
        return new ConsultaVendasResponse(registros, pagina.pagina(), pagina.tamanho(),
                pagina.totalRegistros(), pagina.totalPaginas(), CoberturaHistoricoResponse.desconhecida());
    }

    @Transactional(readOnly = true)
    public VendaDetalheResponse detalhar(UUID vendaId) {
        UUID negocioId = contextoNegocio.getNegocioId();
        var linha = consultaVendasRepository.buscarDetalhe(negocioId, vendaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Erro! Venda não encontrada."));
        List<ItemVendaResponse> itens = consultaVendasRepository.listarItens(negocioId, vendaId).stream()
                .map(item -> new ItemVendaResponse(item.id(), item.produtoId(), item.nome(),
                        item.diferenciador(), item.quantidade(), item.precoUnitario(), item.custoUnitario()))
                .toList();
        return new VendaDetalheResponse(linha.id(), linha.dataOcorrencia(), linha.horaOcorrencia(),
                linha.fusoOcorrencia(), linha.registradaEm(), linha.totalRegistrado(),
                linha.origem(), linha.situacao(), linha.formaPagamentoInformada(),
                linha.encomendaOrigemId(), !itens.isEmpty(), itens,
                CoberturaHistoricoResponse.desconhecida());
    }
}
