package br.app.ergane.api.compartilhado.documentacao;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Set;

/**
 * Documentação OpenAPI (Swagger UI em /swagger-ui.html, especificação em /v3/api-docs).
 * Só existe quando springdoc.api-docs.enabled=true (perfil local).
 * Não altera as cadeias de segurança de ConfiguracaoSeguranca: apenas acrescenta uma
 * cadeia restrita às rotas da documentação.
 */
@Configuration
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true")
public class ConfiguracaoDocumentacaoApi {

    private static final String ESQUEMA_BEARER = "bearer";

    private static final String[] ROTAS_DOCUMENTACAO = {
            "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"
    };

    // Rotas que não exigem token (mesmas liberadas em ConfiguracaoSeguranca.rotasPublicas)
    private static final Set<String> ROTAS_PUBLICAS = Set.of("/api/cadastros", "/api/sessoes", "/api/sessoes/renovacoes");

    @Bean
    public OpenAPI openApiErgane() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ergane API")
                        .version("v1")
                        .description("API REST do Ergane. Para chamar rotas protegidas: cadastre-se (POST /api/cadastros), "
                                + "entre (POST /api/sessoes), selecione a conta (PUT /api/sessoes/atual/conta) "
                                + "e informe o accessToken em Authorize."))
                .components(new Components().addSecuritySchemes(ESQUEMA_BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_BEARER));
    }

    // Tira o cadeado das rotas públicas (cadastro, login e renovação)
    @Bean
    public OpenApiCustomizer rotasPublicasSemToken() {
        return openApi -> openApi.getPaths().forEach((caminho, item) -> {
            if (ROTAS_PUBLICAS.contains(caminho) && item.getPost() != null) {
                item.getPost().setSecurity(List.of());
            }
        });
    }

    @Bean
    @Order(0)
    public SecurityFilterChain rotasDocumentacao(HttpSecurity http) throws Exception {
        http.securityMatcher(ROTAS_DOCUMENTACAO)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable);
        return http.build();
    }

}
