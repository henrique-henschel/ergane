package br.app.ergane.api.vendas.consulta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "itens_venda")
public class ItemVenda {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "venda_id",
            nullable = false,
            updatable = false
    )
    private Venda venda;

    // Identificador do item vendável, sem dependência direta do catálogo.
    @Column(name = "item_vendavel_id")
    private UUID itemVendavelId;

    // Dados preservados conforme estavam no momento da venda.
    @Column(name = "descricao_snapshot", nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(name = "unidade_snapshot", nullable = false, length = 40)
    private String unidade;

    @Column(name = "quantidade", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "custo_unitario", precision = 15, scale = 2)
    private BigDecimal custoUnitario;

    protected ItemVenda() {
    }

    public UUID getId() {
        return id;
    }

    public Venda getVenda() {
        return venda;
    }

    public UUID getItemVendavelId() {
        return itemVendavelId;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getUnidade() {
        return unidade;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getCustoUnitario() {
        return custoUnitario;
    }
}
