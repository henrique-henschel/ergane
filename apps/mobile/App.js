import { useState } from 'react';
import { StatusBar } from 'expo-status-bar';

import GestaoScreen from './src/screens/GestaoScreen';
import CatalogoScreen from './src/screens/CatalogoScreen';
import DetalhesScreen from './src/screens/DetalhesScreen';
import Vender from './src/screens/Vender';
import RevisarVenda from './src/screens/RevisarVenda';
import EntradaScreen from './src/screens/EntradaScreen';
import CombinacoesScreen from './src/screens/CombinacoesScreen';

import produtosIniciais from './src/mocks/produtos';


export default function App() {
  const [telaAtual, setTelaAtual] = useState('gestao');
  const [produtos] = useState(produtosIniciais);
  const [produtoSelecionado, setProdutoSelecionado] = useState(null);
  
  const [filtros, setFiltros] = useState({
    nome: '',
    categoria: 'Todas',
    disponibilidade: 'Todas',
    pagina: 1,
  });

  const [carrinho, setCarrinho] = useState([]);

  function abrirCatalogo() {
    setTelaAtual('catalogo');
  }

  function abrirMetas() {
    setTelaAtual('entrada');
  }

  function abrirCombinacoes() {
    setTelaAtual('combinacoes');
  }

  function atualizarFiltros(novosFiltros) {
    setFiltros((atuais) => ({
      ...atuais,
      ...novosFiltros,
    }));
  }

  function consultarProduto(produto) {
    setProdutoSelecionado(produto);
    setTelaAtual('detalhes');
  }

  function voltarInicio() {
    setTelaAtual('gestao');
  }

  function adicionarProduto(produto) {
    setCarrinho((atual) => {
      const itemExistente = atual.find(
        (item) => item.produtoId === produto.id
      );

      if (itemExistente) {
        return atual.map((item) =>
          item.produtoId === produto.id
            ? {
                ...item,
                quantidade: item.quantidade + 1,
              }
            : item
        );
      }

      return [
        ...atual,
        {
          produtoId: produto.id,
          quantidade: 1,
        },
      ];
    });
  }

  function removerProduto(produto) {
    setCarrinho((atual) =>
      atual.filter((item) => item.produtoId !== produto.id)
    );
  }

  function alterarQuantidade(produtoId, novaQuantidade) {
    setCarrinho((atual) =>
      atual.map((item) =>
        item.produtoId === produtoId
          ? { ...item, quantidade: novaQuantidade }
          : item
      )
    );
  }

  function removerItemCarrinho(produtoId) {
    setCarrinho((atual) =>
      atual.filter((item) => item.produtoId !== produtoId)
    );
  }

  function navegarPara(tela) {
    if (tela === 'gestao') {
      setTelaAtual('gestao');
    }

    if (tela === 'vender') {
      setTelaAtual('vender');
    }
  }

  function revisarVenda() {
    setTelaAtual('revisarVenda');
  }

  return (
    <>
      <StatusBar style="dark" />

      {telaAtual === 'entrada' && (
        <EntradaScreen
          onAbrirCombinacoes={abrirCombinacoes}
          onVoltarInicio={voltarInicio}
          onNavegar={navegarPara}
        />
      )}

      {telaAtual === 'gestao' && (
        <GestaoScreen
          onAbrirCatalogo={abrirCatalogo}
          onAbrirMetas={abrirMetas}
          onVoltarInicio={voltarInicio}
          onNavegar={navegarPara}
        />
      )}

      {telaAtual === 'catalogo' && (
        <CatalogoScreen
          produtos={produtos}
          filtros={filtros}
          onAtualizarFiltros={atualizarFiltros}
          onSelecionarProduto={consultarProduto}
          onVoltar={() => setTelaAtual('gestao')}
        />
      )}

      {telaAtual === 'detalhes' && produtoSelecionado && (
        <DetalhesScreen
          produto={produtoSelecionado}
          onVoltar={() => setTelaAtual('catalogo')}
        />
      )}

      {telaAtual === 'vender' && (
        <Vender
          produtos={produtos}
          carrinho={carrinho}
          onAdicionarProduto={adicionarProduto}
          onRemoverProduto={removerProduto}
          onRevisarVenda={revisarVenda}
          onNavegar={navegarPara}
        />
      )}

      {telaAtual === 'revisarVenda' && (
        <RevisarVenda
          produtos={produtos}
          carrinho={carrinho}
          onVoltar={() => setTelaAtual('vender')}
          onAlterarQuantidade={alterarQuantidade}
          onRemoverProduto={removerItemCarrinho}
        />
      )}

      {telaAtual === 'combinacoes' && (
        <CombinacoesScreen
          onVoltar={() => setTelaAtual('entrada')}
        />
      )}
    </>
  );
}
