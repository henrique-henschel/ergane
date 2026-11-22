package br.app.ergane.api.negocio.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "acessos_negocio")
public class AcessoNegocio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID usuarioId;

    @Column(nullable = false)
    private UUID negocioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PapelNegocio papel;

    @Column(nullable = false)
    private boolean ativo = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant atualizadoEm;

    protected AcessoNegocio() {

    }

    public AcessoNegocio(UUID usuarioId, UUID negocioId, PapelNegocio papel) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("O identificador do usuário é obrigatório.");
        }
        if (negocioId == null) {
            throw new IllegalArgumentException("O identificador do negócio é obrigatório.");
        }
        if (papel == null) {
            throw new IllegalArgumentException("O papel no negócio é obrigatório.");
        }
        this.usuarioId = usuarioId;
        this.negocioId = negocioId;
        this.papel = papel;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public UUID getNegocioId() {
        return negocioId;
    }

    public PapelNegocio getPapel() {
        return papel;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void revogar() {
        ativo = false;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

}
