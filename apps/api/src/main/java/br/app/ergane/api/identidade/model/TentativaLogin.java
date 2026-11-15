package br.app.ergane.api.identidade.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.Duration;
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

    public boolean estaBloqueada(Instant agora) {
        return bloqueadoAte != null && agora.isBefore(bloqueadoAte);
    }

    public void registrarFalha(Instant agora) {
        if (estaBloqueada(agora)) {
            return;
        }
        if (falhas == 0 || !agora.isBefore(janelaInicioEm.plus(Duration.ofMinutes(15)))) {
            reiniciar(agora);
        }
        falhas = Math.min(falhas, Integer.MAX_VALUE - 1) + 1;
        if (falhas >= 5) {
            long espera = Math.min(900L, 30L << Math.min(falhas - 5, 5));
            bloqueadoAte = agora.plusSeconds(espera);
        }
    }

    public void reiniciar(Instant agora) {
        falhas = 0;
        janelaInicioEm = agora;
        bloqueadoAte = null;
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
