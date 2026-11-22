package br.app.ergane.api.identidade.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "contas")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 64)
    private ZoneId fusoHorario;

    @Column(nullable = false)
    private boolean ativa;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    protected Conta() {

    }

    public Conta(String nome, ZoneId fusoHorario) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do negócio é obrigatório.");
        }
        if (nome.codePointCount(0, nome.length()) > 100) {
            throw new IllegalArgumentException("O nome do negócio deve ter no máximo 100 caracteres.");
        }
        if (fusoHorario == null) {
            throw new IllegalArgumentException("O fuso horário do negócio é obrigatório.");
        }
        this.nome = nome;
        this.fusoHorario = fusoHorario;
        this.ativa = true;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public ZoneId getFusoHorario() {
        return fusoHorario;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

}
