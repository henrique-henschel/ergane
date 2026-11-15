package br.app.ergane.api.compartilhado.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PropriedadesJwt.class)
public class ConfiguracaoJwt {
}
