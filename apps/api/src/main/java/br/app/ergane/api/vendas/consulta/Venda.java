package br.app.ergane.api.vendas.consulta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vendas")
public class Venda {

    @Id
    private UUID id;

    @Column(name = "negocio_id", nullable = false, updatable = false)
    private UUID negocioId;

    @Column(name = "registrada_em")
    private Instant registradaEm;

    @Column(name = "data_ocorrencia")
    private LocalDate dataOcorrencia;

    @Column(name = "total", nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "origem", columnDefinition = "text")
    private String origem;

    @Column(name = "situacao", nullable = false, columnDefinition = "text")
    private String situacao;

    protected Venda() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getNegocioId() {
        return negocioId;
    }

    public Instant getRegistradaEm() {
        return registradaEm;
    }

    public LocalDate getDataOcorrencia() {
        return dataOcorrencia;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getOrigem() {
        return origem;
    }

    public String getSituacao() {
        return situacao;
    }
}