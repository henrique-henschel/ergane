package br.app.ergane.api.vendas.consulta;

import br.app.ergane.api.compartilhado.RecursoNaoEncontradoException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ConsultaVendasService {

    // variável temporária até a UC01 fornecer o negócio autorizado a partir da sessão.
    private static final UUID NEGOCIO_FIXO =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final VendaConsultaRepository vendaRepository;
    private final ItemVendaConsultaRepository itemRepository;
    private final RecebimentoConsultaRepository recebimentoRepository;

    public ConsultaVendasService(
            VendaConsultaRepository vendaRepository,
            ItemVendaConsultaRepository itemRepository,
            RecebimentoConsultaRepository recebimentoRepository
    ) {
        this.vendaRepository = vendaRepository;
        this.itemRepository = itemRepository;
        this.recebimentoRepository = recebimentoRepository;
    }

    @Transactional(readOnly = true)
    public ConsultaVendasResponse listar(ConsultaVendasFiltro filtro) {

        PageRequest paginacao = PageRequest.of(
                filtro.paginaEfetiva(),
                filtro.tamanhoEfetivo()
        );

        Page<Venda> pagina = vendaRepository.consultar(
                NEGOCIO_FIXO,
                filtro.de() == null ? null : filtro.de().toInstant(),
                filtro.de() == null ? null : filtro.de().toLocalDate(),
                filtro.ate() == null ? null : filtro.ate().toInstant(),
                filtro.ate() == null ? null : filtro.ate().toLocalDate(),
                filtro.origem(),
                filtro.situacao(),
                filtro.produtoId(),
                filtro.formaRecebimento(),
                paginacao
        );

        return new ConsultaVendasResponse(
                pagina.getContent()
                        .stream()
                        .map(VendaResumoResponse::de)
                        .toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                CoberturaHistoricoResponse.desconhecida()
        );
    }

    @Transactional(readOnly = true)
    public VendaDetalheResponse detalhar(UUID id) {

        Venda venda = vendaRepository
                .findByNegocioIdAndId(NEGOCIO_FIXO, id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Erro! Venda não encontrada."
                        )
                );

        var itens = itemRepository
                .findAllByVenda_IdOrderByIdAsc(venda.getId())
                .stream()
                .map(ItemVendaResponse::de)
                .toList();

        var recebimentos = recebimentoRepository
                .findAllByVenda_IdOrderByIdAsc(venda.getId())
                .stream()
                .map(RecebimentoResponse::de)
                .toList();

        return new VendaDetalheResponse(
                venda.getId(),
                venda.getRegistradaEm(),
                venda.getDataOcorrencia(),
                venda.getTotal(),
                venda.getOrigem(),
                venda.getSituacao(),
                !itens.isEmpty(),
                itens,
                recebimentos,
                CoberturaHistoricoResponse.desconhecida()
        );
    }
}
