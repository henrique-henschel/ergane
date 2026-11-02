package br.app.ergane.api.catalogo.produto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProdutoResponse(UUID id, String nome, String diferenciador, String descricao,
                              BigDecimal preco, BigDecimal custo, LocalDate validade,
                              UUID categoriaId, boolean arquivado, Instant arquivadoEm,
                              Instant criadoEm, Instant atualizadoEm) {

    public static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDiferenciador(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCusto(),
                produto.getValidade(),
                produto.getCategoriaId(),
                produto.isArquivado(),
                produto.getArquivadoEm(),
                produto.getCriadoEm(),
                produto.getAtualizadoEm()
        );
    }

}
