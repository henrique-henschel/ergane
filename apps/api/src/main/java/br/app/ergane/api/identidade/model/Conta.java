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
