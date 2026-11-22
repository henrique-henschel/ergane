package br.app.ergane.api.catalogo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID contaId;

    private UUID categoriaId; // nulo → sem categoria

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 50)
    private String diferenciador;

    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precoVenda;

    @Column(precision = 12, scale = 2)
    private BigDecimal custoUnitario; // nulo = nao informado (diferente de zero)

    private LocalDate validade;

    private Instant arquivadoEm; // nulo → ativo

    @Version
    @Column(nullable = false)
    private Long versao;

    @Column(nullable = false, updatable = false)
    private UUID criadoPorId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant atualizadoEm;

    protected Produto() {
    }

    public Produto(UUID contaId, UUID criadoPorId, String nome, String diferenciador, String descricao,
                   BigDecimal precoVenda, BigDecimal custoUnitario, LocalDate validade, UUID categoriaId) {
        this.contaId = contaId;
        this.criadoPorId = criadoPorId;
        this.nome = nome;
        this.diferenciador = diferenciador;
        this.descricao = descricao;
        this.precoVenda = precoVenda.setScale(2, RoundingMode.UNNECESSARY);
        this.custoUnitario = custoUnitario == null ? null : custoUnitario.setScale(2, RoundingMode.UNNECESSARY);
        this.validade = validade;
        this.categoriaId = categoriaId;
    }

    public void atualizar(String nome, String diferenciador, String descricao,
                          BigDecimal precoVenda, BigDecimal custoUnitario, LocalDate validade, UUID categoriaId) {
        this.nome = nome;
        this.diferenciador = diferenciador;
        this.descricao = descricao;
        this.precoVenda = precoVenda.setScale(2, RoundingMode.UNNECESSARY); // sempre 2 casas: 4.5 vira 4.50
        this.custoUnitario = custoUnitario == null ? null : custoUnitario.setScale(2, RoundingMode.UNNECESSARY);
        this.validade = validade;
        this.categoriaId = categoriaId;
    }

    public void arquivar() {
        if (arquivadoEm == null) {
            arquivadoEm = Instant.now().truncatedTo(ChronoUnit.MICROS);
        }
    }

    public boolean isArquivado() {
        return arquivadoEm != null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getContaId() {
        return contaId;
    }

    public UUID getCategoriaId() {
        return categoriaId;
    }

    public String getNome() {
        return nome;
    }

    public String getDiferenciador() {
        return diferenciador;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public BigDecimal getCustoUnitario() {
        return custoUnitario;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public Instant getArquivadoEm() {
        return arquivadoEm;
    }

    public Long getVersao() {
        return versao;
    }

    public UUID getCriadoPorId() {
        return criadoPorId;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

}
