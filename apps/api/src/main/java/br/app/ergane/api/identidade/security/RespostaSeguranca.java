package br.app.ergane.api.identidade.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class RespostaSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RespostaSeguranca(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        if (indisponivel(ex)) {
            escrever(response, HttpStatus.SERVICE_UNAVAILABLE, "Erro! A autenticação está temporariamente indisponível.");
        } else {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            escrever(response, HttpStatus.UNAUTHORIZED, "Erro! Credencial de acesso inválida ou ausente.");
        }
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        escrever(response, HttpStatus.FORBIDDEN, "Erro! Acesso não permitido.");
    }

    private boolean indisponivel(Throwable erro) {
        for (Throwable atual = erro; atual != null; atual = atual.getCause()) {
            if (atual instanceof AuthenticationServiceException) {
                return true;
            }
        }
        return false;
    }

    private void escrever(HttpServletResponse response, HttpStatus status, String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        objectMapper.writeValue(response.getOutputStream(), ProblemDetail.forStatusAndDetail(status, mensagem));
    }
}
