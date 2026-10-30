package br.app.ergane.api.catalogo.categoria;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaResponse> listar() {
        return categoriaService.listar().stream()
                .map(CategoriaResponse::de)
                .toList();
    }

    @GetMapping("/{id}")
    public CategoriaResponse buscar(@PathVariable UUID id) {
        return CategoriaResponse.de(categoriaService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest request) {
        Categoria categoria = categoriaService.criar(request.nome());
        URI local = URI.create("/api/categorias/" + categoria.getId());
        return ResponseEntity.created(local).body(CategoriaResponse.de(categoria));
    }

    @PutMapping("/{id}")
    public CategoriaResponse renomear(@PathVariable UUID id, @Valid @RequestBody CategoriaRequest request) {
        return CategoriaResponse.de(categoriaService.renomear(id, request.nome()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        categoriaService.excluir(id);
    }

}
