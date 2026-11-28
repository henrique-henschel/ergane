package br.app.ergane.api.operacao.service;

import br.app.ergane.api.compartilhado.exception.RecursoNaoEncontradoException;
import br.app.ergane.api.compartilhado.security.ContextoConta;
import br.app.ergane.api.operacao.dto.CoberturaHistoricoResponse;
import br.app.ergane.api.operacao.dto.ConsultaVendasFiltro;
import br.app.ergane.api.operacao.dto.ConsultaVendasResponse;
import br.app.ergane.api.operacao.dto.ItemVendaResponse;
import br.app.ergane.api.operacao.dto.VendaDetalheResponse;
import br.app.ergane.api.operacao.dto.VendaResumoResponse;
import br.app.ergane.api.operacao.repository.ConsultaVendasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * cuida das consultas de venda, usa a conta do usuario e prepara os dados
 * para enviar pela api.
 */
@Service
public class ConsultaVendasService {

    private final ConsultaVendasRepository consultaVendasRepository;
    private final ContextoConta contextoConta;

    public ConsultaVendasService(ConsultaVendasRepository consultaVendasRepository,
                                 ContextoConta contextoConta) {
        this.consultaVendasRepository = consultaVendasRepository;
        this.contextoConta = contextoConta;
    }

    @Transactional(readOnly = true)
    public ConsultaVendasResponse listar(ConsultaVendasFiltro filtro) {
        var pagina = consultaVendasRepository.consultar(contextoConta.getContaId(), filtro);
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
        UUID contaId = contextoConta.getContaId();
        var linha = consultaVendasRepository.buscarDetalhe(contaId, vendaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Erro! Venda não encontrada."));
        List<ItemVendaResponse> itens = consultaVendasRepository.listarItens(contaId, vendaId).stream()
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
