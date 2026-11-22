package br.app.ergane.api.identidade.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.NullSecurityContextRepository;

import java.util.Set;

@Configuration
@EnableWebSecurity
public class ConfiguracaoSeguranca {

    @Bean
    @Order(1)
    public SecurityFilterChain rotasPublicas(HttpSecurity http) throws Exception {
        Set<String> publicas = Set.of("/api/cadastros", "/api/sessoes", "/api/sessoes/renovacoes");
        http.securityMatcher(request -> request.getDispatcherType() == DispatcherType.ERROR
                        || ("POST".equals(request.getMethod()) && publicas.contains(request.getServletPath())))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        configurarStateless(http);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain rotasProtegidas(HttpSecurity http, DecodificadorJwt decoder,
                                               AutenticacaoJwtConverter converter,
                                               RespostaSeguranca resposta) throws Exception {
        configurarStateless(http);
        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(erros -> erros.authenticationEntryPoint(resposta).accessDeniedHandler(resposta))
                .oauth2ResourceServer(oauth -> oauth
                        .authenticationEntryPoint(resposta).accessDeniedHandler(resposta)
                        .jwt(jwt -> jwt.decoder(decoder).jwtAuthenticationConverter(converter)));
        return http.build();
    }

    private void configurarStateless(HttpSecurity http) throws Exception {
        http.sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityContext(contexto -> contexto.securityContextRepository(new NullSecurityContextRepository()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable);
    }
}
