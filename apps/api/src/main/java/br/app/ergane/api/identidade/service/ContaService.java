package br.app.ergane.api.identidade.service;

import java.time.ZoneId;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.app.ergane.api.identidade.model.AcessoNegocio;
import br.app.ergane.api.identidade.model.Conta;
import br.app.ergane.api.identidade.model.PapelNegocio;
import br.app.ergane.api.identidade.repository.AcessoNegocioRepository;
import br.app.ergane.api.identidade.repository.ContaRepository;
import br.app.ergane.api.compartilhado.exception.RecursoNaoEncontradoException;

@Service
public class ContaService {

    // Provisório: fuso padrão até a D02 definir como o fuso da conta é informado
    private static final ZoneId FUSO_PADRAO = ZoneId.of("America/Sao_Paulo");

    private final ContaRepository contaRepository;
    private final AcessoNegocioRepository acessoNegocioRepository;

    public ContaService(ContaRepository contaRepository, AcessoNegocioRepository acessoNegocioRepository) {
        this.contaRepository = contaRepository;
        this.acessoNegocioRepository = acessoNegocioRepository;
    }

    @Transactional
    public ContaCriada criarComProprietario(UUID usuarioId, String nome) {
        Conta conta = contaRepository.saveAndFlush(new Conta(nome, FUSO_PADRAO));

        //automaticamente cria o acesso do usuário que criou o negócio como proprietário
        AcessoNegocio acesso = acessoNegocioRepository.saveAndFlush(
                new AcessoNegocio(usuarioId, conta.getId(), PapelNegocio.PROPRIETARIO));
        return new ContaCriada(conta.getId(), acesso.getId(), acesso.getPapel());
    }

    public record ContaCriada(UUID contaId, UUID acessoNegocioId, PapelNegocio papel) {

    }

    @Transactional(readOnly = true)
    public void validarAcesso(UUID usuarioId, UUID contaId) {
        acessoNegocioRepository.findByUsuarioIdAndContaIdAndAtivoTrue(usuarioId, contaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Erro! Negócio não encontrado."));
    }

}
