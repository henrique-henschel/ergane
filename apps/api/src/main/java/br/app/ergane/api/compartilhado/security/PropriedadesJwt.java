package br.app.ergane.api.compartilhado.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "ergane.security.jwt")
public record PropriedadesJwt(String issuer, String audience, Resource privateKey, Resource publicKey) {
    @Override
    public String toString() {
        return "PropriedadesJwt[configuração protegida]";
    }
}
