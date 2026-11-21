package br.app.ergane.api.identidade.service;

import br.app.ergane.api.compartilhado.security.PropriedadesJwt;
import br.app.ergane.api.identidade.dto.SessaoResponse;
import br.app.ergane.api.identidade.exception.AutenticacaoIndisponivelException;
import br.app.ergane.api.identidade.exception.FalhaAutenticacaoException;
import br.app.ergane.api.identidade.model.Sessao;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
public class TokenSessaoService {

    private final PropriedadesJwt propriedades;
    private final SecureRandom random = new SecureRandom();
    private volatile JwtEncoder encoder;

    public TokenSessaoService(PropriedadesJwt propriedades) {
        this.propriedades = propriedades;
    }

    public String gerarRefreshToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String calcularHash(String refreshToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(refreshToken.getBytes(StandardCharsets.US_ASCII));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponível.");
        }
    }

    public SessaoResponse emitir(Sessao sessao, String refreshToken, Instant agora) {
        Instant expiraJwt = agora.plus(Duration.ofMinutes(15));
        if (expiraJwt.isAfter(sessao.getExpiraEm())) {
            expiraJwt = sessao.getExpiraEm();
        }
        expiraJwt = expiraJwt.truncatedTo(ChronoUnit.SECONDS);
        if (!expiraJwt.isAfter(agora)) {
            throw new FalhaAutenticacaoException();
        }
        JwtEncoder encoderAtual = obterEncoder();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(propriedades.issuer())
                .audience(List.of(propriedades.audience()))
                .subject(sessao.getUsuarioId().toString())
                .id(sessao.getTokenAcessoId().toString())
                .claim("sid", sessao.getId().toString())
                .issuedAt(agora.truncatedTo(ChronoUnit.SECONDS))
                .expiresAt(expiraJwt)
                .build();
        try {
            Jwt jwt = encoderAtual.encode(JwtEncoderParameters.from(
                    JwsHeader.with(SignatureAlgorithm.RS256).type("JWT").build(), claims));
            return new SessaoResponse(sessao.getUsuarioId(), sessao.getId(), jwt.getTokenValue(), "Bearer",
                    refreshToken, expiraJwt, sessao.getExpiraEm(), sessao.getLimiteAbsolutoEm());
        } catch (JwtException ex) {
            throw new AutenticacaoIndisponivelException();
        }
    }

    private JwtEncoder obterEncoder() {
        JwtEncoder atual = encoder;
        if (atual != null) {
            return atual;
        }
        synchronized (this) {
            if (encoder != null) {
                return encoder;
            }
            if (propriedades.issuer() == null || propriedades.issuer().isBlank()
                    || propriedades.audience() == null || propriedades.audience().isBlank()
                    || propriedades.privateKey() == null || propriedades.publicKey() == null) {
                throw new AutenticacaoIndisponivelException();
            }
            try (InputStream privada = propriedades.privateKey().getInputStream();
                 InputStream publica = propriedades.publicKey().getInputStream()) {
                RSAPrivateKey chavePrivada = RsaKeyConverters.pkcs8().convert(privada);
                RSAPublicKey chavePublica = RsaKeyConverters.x509().convert(publica);
                if (chavePrivada == null || chavePublica == null
                        || chavePublica.getModulus().bitLength() < 2048
                        || !chavePrivada.getModulus().equals(chavePublica.getModulus())) {
                    throw new AutenticacaoIndisponivelException();
                }
                Signature assinatura = Signature.getInstance("SHA256withRSA");
                byte[] verificacao = "ergane-jwt-key-pair".getBytes(StandardCharsets.US_ASCII);
                assinatura.initSign(chavePrivada);
                assinatura.update(verificacao);
                byte[] assinaturaTeste = assinatura.sign();
                assinatura.initVerify(chavePublica);
                assinatura.update(verificacao);
                if (!assinatura.verify(assinaturaTeste)) {
                    throw new AutenticacaoIndisponivelException();
                }
                RSAKey chave = new RSAKey.Builder(chavePublica).privateKey(chavePrivada).build();
                encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(chave)));
                return encoder;
            } catch (Exception ex) {
                throw new AutenticacaoIndisponivelException();
            }
        }
    }
}
