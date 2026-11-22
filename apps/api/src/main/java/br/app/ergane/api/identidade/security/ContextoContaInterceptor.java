package br.app.ergane.api.identidade.security;

import br.app.ergane.api.compartilhado.exception.ContextoContaNaoSelecionadaException;
import br.app.ergane.api.compartilhado.security.ContextoConta;
import br.app.ergane.api.compartilhado.security.UsuarioAutenticado;
import br.app.ergane.api.identidade.exception.FalhaAutenticacaoException;
import br.app.ergane.api.identidade.service.ContaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ContextoContaInterceptor implements HandlerInterceptor {

    private final ContaService contaService;
    private final ContextoConta contextoConta;

    public ContextoContaInterceptor(ContaService contaService, ContextoConta contextoConta) {
        this.contaService = contaService;
        this.contextoConta = contextoConta;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new FalhaAutenticacaoException();
        }
        if (usuario.contaAtivaId() == null) {
            throw new ContextoContaNaoSelecionadaException();
        }
        contaService.validarAcesso(usuario.usuarioId(), usuario.contaAtivaId());
        contextoConta.definirContaAutorizada(usuario.usuarioId(), usuario.contaAtivaId());
        return true;
    }
}
