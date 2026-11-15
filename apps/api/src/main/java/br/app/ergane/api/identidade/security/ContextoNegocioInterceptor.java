package br.app.ergane.api.identidade.security;

import br.app.ergane.api.compartilhado.exception.ContextoNegocioNaoSelecionadoException;
import br.app.ergane.api.compartilhado.security.ContextoNegocio;
import br.app.ergane.api.compartilhado.security.UsuarioAutenticado;
import br.app.ergane.api.identidade.exception.FalhaAutenticacaoException;
import br.app.ergane.api.negocio.service.NegocioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ContextoNegocioInterceptor implements HandlerInterceptor {

    private final NegocioService negocioService;
    private final ContextoNegocio contextoNegocio;

    public ContextoNegocioInterceptor(NegocioService negocioService, ContextoNegocio contextoNegocio) {
        this.negocioService = negocioService;
        this.contextoNegocio = contextoNegocio;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new FalhaAutenticacaoException();
        }
        if (usuario.negocioAtivoId() == null) {
            throw new ContextoNegocioNaoSelecionadoException();
        }
        negocioService.validarAcesso(usuario.usuarioId(), usuario.negocioAtivoId());
        contextoNegocio.definirNegocioAutorizado(usuario.negocioAtivoId());
        return true;
    }
}
