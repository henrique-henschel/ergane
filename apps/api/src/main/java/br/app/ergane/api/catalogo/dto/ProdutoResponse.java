package br.app.ergane.api.catalogo.dto;

import br.app.ergane.api.catalogo.model.Produto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProdutoResponse(UUID id, String nome, String diferenciador, String descricao,
                              BigDecimal precoVenda, BigDecimal custoUnitario, LocalDate validade,
                              UUID categoriaId, boolean arquivado, Instant arquivadoEm,
                              Long versao, UUID criadoPorId, Instant criadoEm, Instant atualizadoEm) {

    public static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDiferenciador(),
                produto.getDescricao(),
                produto.getPrecoVenda(),
                produto.getCustoUnitario(),
                produto.getValidade(),
                produto.getCategoriaId(),
                produto.isArquivado(),
                produto.getArquivadoEm(),
                produto.getVersao(),
                produto.getCriadoPorId(),
                produto.getCriadoEm(),
                produto.getAtualizadoEm()
        );
    }

}
