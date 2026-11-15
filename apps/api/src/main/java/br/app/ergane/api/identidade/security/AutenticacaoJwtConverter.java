package br.app.ergane.api.identidade.security;

import br.app.ergane.api.compartilhado.security.UsuarioAutenticado;
import br.app.ergane.api.identidade.exception.FalhaAutenticacaoException;
import br.app.ergane.api.identidade.service.ValidacaoSessaoService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;

import java.util.List;
import java.util.UUID;

@Component
public class AutenticacaoJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final ValidacaoSessaoService validacaoSessaoService;

    public AutenticacaoJwtConverter(ValidacaoSessaoService validacaoSessaoService) {
        this.validacaoSessaoService = validacaoSessaoService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        try {
            UsuarioAutenticado usuario = validacaoSessaoService.validar(UUID.fromString(jwt.getSubject()),
                    UUID.fromString(jwt.getClaimAsString("sid")), UUID.fromString(jwt.getId()));
            return UsernamePasswordAuthenticationToken.authenticated(usuario, null, List.of());
        } catch (FalhaAutenticacaoException | IllegalArgumentException | NullPointerException ex) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token"), "Credencial inválida.");
        } catch (DataAccessException | TransactionException ex) {
            throw new AuthenticationServiceException("A autenticação está temporariamente indisponível.");
        }
    }
}
