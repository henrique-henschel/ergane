package br.app.ergane.api.catalogo.produto;

import br.app.ergane.api.catalogo.categoria.CategoriaService;
import br.app.ergane.api.compartilhado.ContextoNegocio;
import br.app.ergane.api.compartilhado.RecursoNaoEncontradoException;
import br.app.ergane.api.compartilhado.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaService categoriaService;
    private final ContextoNegocio contextoNegocio;

    public ProdutoService(ProdutoRepository produtoRepository, CategoriaService categoriaService, ContextoNegocio contextoNegocio) {
        this.produtoRepository = produtoRepository;
        this.categoriaService = categoriaService;
        this.contextoNegocio = contextoNegocio;
    }

    @Transactional(readOnly = true)
    public List<Produto> listar(String nome, UUID categoriaId, boolean semCategoria, boolean incluirArquivados) {
        String nomeLimpo = limpar(nome);
        String padraoNome = nomeLimpo == null ? null : "%" + nomeLimpo.toLowerCase() + "%";
        UUID filtroCategoria = semCategoria ? null : categoriaId; // "sem categoria" prevalece

        return produtoRepository.buscarComFiltros(contextoNegocio.negocioId(), padraoNome, filtroCategoria, semCategoria, incluirArquivados);
    }

    @Transactional(readOnly = true)
    public Produto buscar(UUID id) {
        return produtoRepository.findByNegocioIdAndId(contextoNegocio.negocioId(), id).orElseThrow(() -> new RecursoNaoEncontradoException("Erro! Produto não encontrado."));
    }

    @Transactional
    public Produto criar(ProdutoRequest dados) {
        String nome = dados.nome().trim();
        String diferenciador = limpar(dados.diferenciador());

        validarCategoria(dados.categoriaId());
        validarNomeUnico(nome, diferenciador, null);

        Produto produto = new Produto(contextoNegocio.negocioId(), nome, diferenciador,
                limpar(dados.descricao()), dados.preco(), dados.custo(),
                dados.validade(), dados.categoriaId());

        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizar(UUID id, ProdutoRequest dados) {
        Produto produto = buscar(id);

        if (produto.isArquivado()) {
            throw new RegraNegocioException("Erro! Produto arquivado não pode ser editado.");
        }

        String nome = dados.nome().trim();
        String diferenciador = limpar(dados.diferenciador());

        validarCategoria(dados.categoriaId());
        validarNomeUnico(nome, diferenciador, id);

        produto.atualizar(nome, diferenciador, limpar(dados.descricao()),
                dados.preco(), dados.custo(), dados.validade(), dados.categoriaId());

        return produto; // dirty checking faz o UPDATE
    }

    @Transactional
    public Produto arquivar(UUID id) {
        Produto produto = buscar(id);
        produto.arquivar();
        return produto;
    }

    private void validarCategoria(UUID categoriaId) {
        if (categoriaId != null && !categoriaService.existe(categoriaId)) {
            throw new RegraNegocioException("Erro! A categoria informada não existe.");
        }
    }

    private void validarNomeUnico(String nome, String diferenciador, UUID ignorarId) {
        String chave = diferenciador == null ? "" : diferenciador;

        if (produtoRepository.existeAtivoComMesmoNome(contextoNegocio.negocioId(), nome, chave, ignorarId)) {
            throw new RegraNegocioException(diferenciador == null
                    ? "Erro! Já existe um produto com este nome. Informe um diferenciador (ex.: tamanho, sabor)."
                    : "Erro! Já existe um produto com este nome e diferenciador.");
        }
    }

    private static String limpar(String texto) {
        if (texto == null) return null;

        String limpo = texto.trim();

        return limpo.isEmpty() ? null : limpo;
    }

}
