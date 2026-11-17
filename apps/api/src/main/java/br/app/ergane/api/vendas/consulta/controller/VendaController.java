package br.app.ergane.api.vendas.consulta.controller;

import br.app.ergane.api.vendas.consulta.dto.ConsultaVendasFiltro;
import br.app.ergane.api.vendas.consulta.dto.ConsultaVendasResponse;
import br.app.ergane.api.vendas.consulta.dto.VendaDetalheResponse;
import br.app.ergane.api.vendas.consulta.service.ConsultaVendasService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * mostra os enderecos para listar vendas com filtros e ver uma venda pelo id.
 */
@RestController
@RequestMapping("/api/vendas")
public class VendaController {

    private final ConsultaVendasService consultaVendasService;

    public VendaController(ConsultaVendasService consultaVendasService) {
        this.consultaVendasService = consultaVendasService;
    }

    @GetMapping
    public ConsultaVendasResponse listar(@Valid @ModelAttribute ConsultaVendasFiltro filtro) {
        return consultaVendasService.listar(filtro);
    }

    @GetMapping("/{id}")
    public VendaDetalheResponse detalhar(@PathVariable UUID id) {
        return consultaVendasService.detalhar(id);
    }
}
