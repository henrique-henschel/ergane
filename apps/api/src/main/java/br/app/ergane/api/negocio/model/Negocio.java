package br.app.ergane.api.negocio.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "negocios")
public class Negocio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    protected Negocio() {

    }

    public Negocio(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do negócio é obrigatório.");
        }
        if (nome.codePointCount(0, nome.length()) > 100) {
            throw new IllegalArgumentException("O nome do negócio deve ter no máximo 100 caracteres.");
        }
        this.nome = nome;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

}
