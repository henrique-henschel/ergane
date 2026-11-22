package br.app.ergane.api.identidade.service;

import br.app.ergane.api.compartilhado.exception.RegraNegocioException;
import br.app.ergane.api.identidade.dto.CadastroRequest;
import br.app.ergane.api.identidade.dto.CadastroResponse;
import br.app.ergane.api.identidade.dto.CadastroResponse.ContaResponse;
import br.app.ergane.api.identidade.model.Usuario;
import br.app.ergane.api.identidade.repository.UsuarioRepository;
import br.app.ergane.api.identidade.service.ContaService;
import br.app.ergane.api.identidade.service.ContaService.ContaCriada;
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
    private final ContaService contaService;
    private final PasswordEncoder passwordEncoder;

    public CadastroService(UsuarioRepository usuarioRepository, ContaService contaService,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.contaService = contaService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CadastroResponse cadastrar(@NotNull @Valid CadastroRequest request) {
        if (usuarioRepository.findByEmailNormalizado(request.email()).isPresent()) {
            throw new RegraNegocioException("Erro! Já existe um usuário com este email.");
        }

        String senhaHash = passwordEncoder.encode(request.senha());
        Usuario usuario = usuarioRepository.saveAndFlush(new Usuario(request.nome(), request.email(), senhaHash));

        ContaCriada conta = null;
        if (request.nomeConta() != null) {
            conta = contaService.criarComProprietario(usuario.getId(), request.nomeConta());
        }

        ContaResponse contaResponse = conta == null ? null
                : new ContaResponse(conta.contaId(), conta.acessoNegocioId(), conta.papel());
        return new CadastroResponse(usuario.getId(), contaResponse);
    }

}
