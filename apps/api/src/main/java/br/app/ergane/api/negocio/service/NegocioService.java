package br.app.ergane.api.negocio.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.app.ergane.api.negocio.model.AcessoNegocio;
import br.app.ergane.api.negocio.model.Negocio;
import br.app.ergane.api.negocio.model.PapelNegocio;
import br.app.ergane.api.negocio.repository.AcessoNegocioRepository;
import br.app.ergane.api.negocio.repository.NegocioRepository;

@Service
public class NegocioService {

    private final NegocioRepository negocioRepository;
    private final AcessoNegocioRepository acessoNegocioRepository;

    public NegocioService(NegocioRepository negocioRepository, AcessoNegocioRepository acessoNegocioRepository) {
        this.negocioRepository = negocioRepository;
        this.acessoNegocioRepository = acessoNegocioRepository;
    }

    @Transactional
    public NegocioCriado criarComProprietario(UUID usuarioId, String nome) {
        Negocio negocio = negocioRepository.saveAndFlush(new Negocio(nome));

        //automaticamente cria o acesso do usuário que criou o negócio como proprietário
        AcessoNegocio acesso = acessoNegocioRepository.saveAndFlush(
                new AcessoNegocio(usuarioId, negocio.getId(), PapelNegocio.PROPRIETARIO));
        return new NegocioCriado(negocio.getId(), acesso.getId(), acesso.getPapel());
    }

    public record NegocioCriado(UUID negocioId, UUID acessoNegocioId, PapelNegocio papel) {

    }

}
