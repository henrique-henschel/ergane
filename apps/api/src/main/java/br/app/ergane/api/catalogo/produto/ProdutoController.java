package br.app.ergane.api.catalogo.produto;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public List<ProdutoResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(defaultValue = "false") boolean semCategoria,
            @RequestParam(defaultValue = "false") boolean incluirArquivados) {

        return produtoService.listar(nome, categoriaId, semCategoria, incluirArquivados).stream()
                .map(ProdutoResponse::de)
                .toList();
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscar(@PathVariable UUID id) {
        return ProdutoResponse.de(produtoService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request) {
        Produto produto = produtoService.criar(request);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(produto.getId())
                .toUri();
        return ResponseEntity.created(local).body(ProdutoResponse.de(produto));
    }

    @PutMapping("/{id}")
    public ProdutoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody ProdutoRequest request) {
        return ProdutoResponse.de(produtoService.atualizar(id, request));
    }

    // Arquivar nao eh DELETE: o produto continua existindo para o historico
    @PostMapping("/{id}/arquivar")
    public ProdutoResponse arquivar(@PathVariable UUID id) {
        return ProdutoResponse.de(produtoService.arquivar(id));
    }

}
