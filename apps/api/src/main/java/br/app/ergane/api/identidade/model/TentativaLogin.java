package br.app.ergane.api.identidade.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tentativas_login", uniqueConstraints = {
        @UniqueConstraint(name = "uq_tentativas_login_email", columnNames = "email_normalizado")
})
public class TentativaLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email_normalizado", nullable = false, columnDefinition = "text")
    private String emailNormalizado;

    @Column(nullable = false)
    private int falhas;

    @Column(nullable = false)
    private Instant janelaInicioEm;

    @Column(nullable = true)
    private Instant bloqueadoAte;

    protected TentativaLogin() {

    }

    public TentativaLogin(String emailNormalizado, Instant janelaInicioEm) {
        if (emailNormalizado == null || emailNormalizado.isBlank()) {
            throw new IllegalArgumentException("O email normalizado é obrigatório.");
        }
        if (janelaInicioEm == null) {
            throw new IllegalArgumentException("O início da janela de tentativas é obrigatório.");
        }
        this.emailNormalizado = emailNormalizado;
        this.janelaInicioEm = janelaInicioEm;
    }

    public UUID getId() {
        return id;
    }

    public String getEmailNormalizado() {
        return emailNormalizado;
    }

    public int getFalhas() {
        return falhas;
    }

    public Instant getJanelaInicioEm() {
        return janelaInicioEm;
    }

    public Instant getBloqueadoAte() {
        return bloqueadoAte;
    }

}
