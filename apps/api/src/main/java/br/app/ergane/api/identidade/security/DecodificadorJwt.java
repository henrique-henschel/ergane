package br.app.ergane.api.identidade.security;

import br.app.ergane.api.compartilhado.security.PropriedadesJwt;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class DecodificadorJwt implements JwtDecoder {

    private final PropriedadesJwt propriedades;
    private volatile NimbusJwtDecoder decoder;

    public DecodificadorJwt(PropriedadesJwt propriedades) {
        this.propriedades = propriedades;
    }

    @Override
    public Jwt decode(String token) {
        NimbusJwtDecoder atual = obterDecoder();
        try {
            return atual.decode(token);
        } catch (JwtException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new BadJwtException("Credencial inválida.");
        }
    }

    private NimbusJwtDecoder obterDecoder() {
        NimbusJwtDecoder atual = decoder;
        if (atual != null) {
            return atual;
        }
        synchronized (this) {
            if (decoder != null) {
                return decoder;
            }
            if (propriedades.issuer() == null || propriedades.issuer().isBlank()
                    || propriedades.audience() == null || propriedades.audience().isBlank()
                    || propriedades.publicKey() == null) {
                throw indisponivel();
            }
            try (InputStream publica = propriedades.publicKey().getInputStream()) {
                RSAPublicKey chave = RsaKeyConverters.x509().convert(publica);
                if (chave == null || chave.getModulus().bitLength() < 2048) {
                    throw indisponivel();
                }
                NimbusJwtDecoder novo = NimbusJwtDecoder.withPublicKey(chave)
                        .signatureAlgorithm(SignatureAlgorithm.RS256).build();
                novo.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                        new JwtTimestampValidator(Duration.ZERO),
                        new JwtIssuerValidator(propriedades.issuer()), this::validarClaims));
                decoder = novo;
                return novo;
            } catch (Exception ex) {
                throw indisponivel();
            }
        }
    }

    private OAuth2TokenValidatorResult validarClaims(Jwt jwt) {
        try {
            UUID.fromString(jwt.getSubject());
            UUID.fromString(jwt.getId());
            UUID.fromString(jwt.getClaimAsString("sid"));
            Instant agora = Instant.now();
            Instant criado = jwt.getIssuedAt();
            Instant expira = jwt.getExpiresAt();
            if (!"RS256".equals(jwt.getHeaders().get("alg"))
                    || !jwt.getAudience().contains(propriedades.audience())
                    || criado == null || expira == null || criado.isAfter(agora)
                    || !expira.isAfter(agora) || !expira.isAfter(criado)
                    || expira.isAfter(criado.plus(Duration.ofMinutes(15)))) {
                return invalido();
            }
            return OAuth2TokenValidatorResult.success();
        } catch (RuntimeException ex) {
            return invalido();
        }
    }

    private OAuth2TokenValidatorResult invalido() {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Credencial inválida.", null));
    }

    private AuthenticationServiceException indisponivel() {
        return new AuthenticationServiceException("A autenticação está temporariamente indisponível.");
    }
}
