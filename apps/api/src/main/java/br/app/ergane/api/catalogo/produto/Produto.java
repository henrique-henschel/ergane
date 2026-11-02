package br.app.ergane.api.catalogo.produto;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID negocioId;

    private UUID categoriaId; // nulo → sem categoria

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 50)
    private String diferenciador;

    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(precision = 12, scale = 2)
    private BigDecimal custo; // nulo = nao informado (diferente de zero)

    private LocalDate validade;

    private Instant arquivadoEm; // nulo → ativo

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant atualizadoEm;

    protected Produto() {
    }

    public Produto(UUID negocioId, String nome, String diferenciador, String descricao,
                   BigDecimal preco, BigDecimal custo, LocalDate validade, UUID categoriaId) {
        this.negocioId = negocioId;
        this.nome = nome;
        this.diferenciador = diferenciador;
        this.descricao = descricao;
        this.preco = preco.setScale(2, RoundingMode.UNNECESSARY);
        this.custo = custo == null ? null : custo.setScale(2, RoundingMode.UNNECESSARY);
        this.validade = validade;
        this.categoriaId = categoriaId;
    }

    public void atualizar(String nome, String diferenciador, String descricao,
                          BigDecimal preco, BigDecimal custo, LocalDate validade, UUID categoriaId) {
        this.nome = nome;
        this.diferenciador = diferenciador;
        this.descricao = descricao;
        this.preco = preco.setScale(2, RoundingMode.UNNECESSARY); // sempre 2 casas: 4.5 vira 4.50
        this.custo = custo == null ? null : custo.setScale(2, RoundingMode.UNNECESSARY);
        this.validade = validade;
        this.categoriaId = categoriaId;
    }

    public void arquivar() {
        if (arquivadoEm == null) {
            arquivadoEm = Instant.now();
        }
    }

    public boolean isArquivado() {
        return arquivadoEm != null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getNegocioId() {
        return negocioId;
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

    public BigDecimal getPreco() {
        return preco;
    }

    public BigDecimal getCusto() {
        return custo;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public Instant getArquivadoEm() {
        return arquivadoEm;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

}
