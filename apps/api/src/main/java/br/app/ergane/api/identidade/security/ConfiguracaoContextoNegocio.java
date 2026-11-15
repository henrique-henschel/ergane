package br.app.ergane.api.identidade.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConfiguracaoContextoNegocio implements WebMvcConfigurer {

    private final ContextoNegocioInterceptor interceptor;

    public ConfiguracaoContextoNegocio(ContextoNegocioInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/api/categorias", "/api/categorias/**");
    }
}
