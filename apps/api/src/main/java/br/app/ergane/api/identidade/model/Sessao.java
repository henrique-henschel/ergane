package br.app.ergane.api.identidade.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sessoes", uniqueConstraints = {
        @UniqueConstraint(name = "uq_sessoes_token_acesso_id", columnNames = "token_acesso_id"),
        @UniqueConstraint(name = "uq_sessoes_refresh_token_hash", columnNames = "refresh_token_hash")
})
public class Sessao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID usuarioId;

    @Column(name = "conta_ativa_id", nullable = true)
    private UUID contaAtivaId;

    @Column(name = "token_acesso_id", nullable = false)
    private UUID tokenAcessoId;

    @JsonIgnore
    @Column(name = "refresh_token_hash", nullable = false, length = 64)
    private String refreshTokenHash;

    @Column(nullable = false)
    private Instant criadoEm;

    @Column(nullable = false)
    private Instant expiraEm;

    @Column(nullable = false)
    private Instant limiteAbsolutoEm;

    @Column(nullable = true)
    private Instant revogadaEm;

    protected Sessao() {

    }

    public Sessao(UUID usuarioId, UUID tokenAcessoId, String refreshTokenHash,
                  Instant criadoEm, Instant expiraEm, Instant limiteAbsolutoEm) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("O identificador do usuário é obrigatório.");
        }
        if (tokenAcessoId == null) {
            throw new IllegalArgumentException("O identificador do token de acesso é obrigatório.");
        }
        if (refreshTokenHash == null || !refreshTokenHash.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("O hash do refresh token deve conter 64 caracteres hexadecimais minúsculos.");
        }
        if (criadoEm == null || expiraEm == null || limiteAbsolutoEm == null) {
            throw new IllegalArgumentException("As datas da sessão são obrigatórias.");
        }
        if (!criadoEm.isBefore(expiraEm) || expiraEm.isAfter(limiteAbsolutoEm)) {
            throw new IllegalArgumentException("A expiração deve ser posterior à criação e respeitar o limite absoluto.");
        }
        this.usuarioId = usuarioId;
        this.tokenAcessoId = tokenAcessoId;
        this.refreshTokenHash = refreshTokenHash;
        this.criadoEm = criadoEm;
        this.expiraEm = expiraEm;
        this.limiteAbsolutoEm = limiteAbsolutoEm;
    }

    public UUID getId() {
        return id;
    }

    public boolean estaValida(Instant agora) {
        return revogadaEm == null && agora.isBefore(expiraEm) && agora.isBefore(limiteAbsolutoEm);
    }

    public void renovar(UUID novoTokenAcessoId, String novoRefreshTokenHash, Instant agora, Instant novaExpiracao) {
        if (!estaValida(agora)) {
            throw new IllegalStateException("A sessão não pode ser renovada.");
        }
        if (novoTokenAcessoId == null || novoRefreshTokenHash == null
                || !novoRefreshTokenHash.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("Os novos identificadores da sessão são inválidos.");
        }
        if (novaExpiracao == null || !agora.isBefore(novaExpiracao)
                || novaExpiracao.isAfter(limiteAbsolutoEm)) {
            throw new IllegalArgumentException("A nova expiração deve respeitar o limite absoluto.");
        }
        tokenAcessoId = novoTokenAcessoId;
        refreshTokenHash = novoRefreshTokenHash;
        expiraEm = novaExpiracao;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public UUID getContaAtivaId() {
        return contaAtivaId;
    }

    public void selecionarConta(UUID contaId) {
        if (contaId == null) {
            throw new IllegalArgumentException("O identificador do negócio é obrigatório.");
        }
        contaAtivaId = contaId;
    }

    public UUID getTokenAcessoId() {
        return tokenAcessoId;
    }

    @JsonIgnore
    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }

    public Instant getLimiteAbsolutoEm() {
        return limiteAbsolutoEm;
    }

    public Instant getRevogadaEm() {
        return revogadaEm;
    }

    public void revogar(Instant agora) {
        if (agora == null) {
            throw new IllegalArgumentException("A data de revogação é obrigatória.");
        }
        if (revogadaEm == null) {
            revogadaEm = agora.isBefore(criadoEm) ? criadoEm : agora;
        }
    }

}
