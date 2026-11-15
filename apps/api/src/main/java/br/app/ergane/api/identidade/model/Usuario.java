package br.app.ergane.api.identidade.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "text")
    private String nome;

    @Column(nullable = false, columnDefinition = "text")
    private String email;

    @JsonIgnore
    @Column(nullable = false, columnDefinition = "text")
    private String senhaHash;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = true)
    private Instant emailVerificadoEm;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant atualizadoEm;

    protected Usuario() {

    }

    public Usuario(String nome, String email, String senhaHash) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do usuário é obrigatório.");
        }
        String emailNormalizado = email == null ? null : email.trim();
        if (emailNormalizado == null || emailNormalizado.isBlank()) {
            throw new IllegalArgumentException("O email do usuário é obrigatório.");
        }
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new IllegalArgumentException("O hash da senha é obrigatório.");
        }
        this.nome = nome;
        this.email = emailNormalizado;
        this.senhaHash = senhaHash;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    @JsonIgnore
    public String getSenhaHash() {
        return senhaHash;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Instant getEmailVerificadoEm() {
        return emailVerificadoEm;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

}
