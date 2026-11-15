package br.app.ergane.api.identidade.service;

import br.app.ergane.api.compartilhado.exception.RegraNegocioException;
import br.app.ergane.api.identidade.dto.CadastroRequest;
import br.app.ergane.api.identidade.dto.CadastroResponse;
import br.app.ergane.api.identidade.dto.CadastroResponse.NegocioResponse;
import br.app.ergane.api.identidade.model.Usuario;
import br.app.ergane.api.identidade.repository.UsuarioRepository;
import br.app.ergane.api.negocio.service.NegocioService;
import br.app.ergane.api.negocio.service.NegocioService.NegocioCriado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class CadastroService {

    private final UsuarioRepository usuarioRepository;
    private final NegocioService negocioService;
    private final PasswordEncoder passwordEncoder;

    public CadastroService(UsuarioRepository usuarioRepository, NegocioService negocioService,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.negocioService = negocioService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CadastroResponse cadastrar(@NotNull @Valid CadastroRequest request) {
        if (usuarioRepository.findByEmailNormalizado(request.email()).isPresent()) {
            throw new RegraNegocioException("Erro! Já existe um usuário com este email.");
        }

        String senhaHash = passwordEncoder.encode(request.senha());
        Usuario usuario = usuarioRepository.saveAndFlush(new Usuario(request.nome(), request.email(), senhaHash));

        NegocioCriado negocio = null;
        if (request.nomeNegocio() != null) {
            negocio = negocioService.criarComProprietario(usuario.getId(), request.nomeNegocio());
        }

        NegocioResponse negocioResponse = negocio == null ? null
                : new NegocioResponse(negocio.negocioId(), negocio.acessoNegocioId(), negocio.papel());
        return new CadastroResponse(usuario.getId(), negocioResponse);
    }

}
