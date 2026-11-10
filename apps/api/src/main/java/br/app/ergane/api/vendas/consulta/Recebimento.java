package br.app.ergane.api.vendas.consulta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recebimentos")
public class Recebimento {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "venda_id",
            nullable = false,
            updatable = false
    )
    private Venda venda;

    @Column(name = "forma", nullable = false, columnDefinition = "text")
    private String forma;

    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "recebido_em", nullable = false)
    private Instant recebidoEm;

    protected Recebimento() {
    }

    public UUID getId() {
        return id;
    }

    public Venda getVenda() {
        return venda;
    }

    public String getForma() {
        return forma;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public Instant getRecebidoEm() {
        return recebidoEm;
    }
}
