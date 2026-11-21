import { useState } from 'react';
import { StatusBar } from 'expo-status-bar';

import GestaoScreen from './src/screens/gestao/GestaoScreen';

import CatalogoScreen from './src/screens/gestao/estoque/CatalogoScreen';
import DetalhesScreen from './src/screens/gestao/estoque/DetalhesScreen';

import Historico from './src/screens/gestao/historico/Historico';
import DetalhesHistorico from './src/screens/gestao/historico/DetalhesHistorico';


import Vender from './src/screens/venda/Vender';

import RevisarVenda from './src/screens/venda/RevisarVenda';
import VendaRegistradaScreen from './src/screens/venda/VendaRegistrada';


import EntradaScreen from './src/screens/EntradaScreen';
import CombinacoesScreen from './src/screens/CombinacoesScreen';


import produtosIniciais from './src/mocks/produtos';


export default function App() {
  const [telaAtual, setTelaAtual] = useState('gestao');
  const [produtos] = useState(produtosIniciais);
  const [produtoSelecionado, setProdutoSelecionado] = useState(null);
  const [proximoRegistro, setProximoRegistro] = useState(1);
  const [vendaRegistrada, setVendaRegistrada] = useState(null);
  const [vendaSelecionada, setVendaSelecionada] = useState(null);
  
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

  function abrirHistorico() {
    setTelaAtual('historico');
  }

  function abrirDetalhesHistorico(venda) {
    setVendaSelecionada(venda);
    setTelaAtual('detalhesHistorico');
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

  function confirmarVenda(dadosVenda) {
    setVendaRegistrada({
      ...dadosVenda,
      registro: proximoRegistro,
    });

    setProximoRegistro((atual) => atual + 1);

    setCarrinho([]);
    setTelaAtual('vendaRegistrada');
  }

  function iniciarNovaVenda() {
    setCarrinho([]);
    setTelaAtual('vender');
  }

  return (
    <>
      <StatusBar
        style={telaAtual === 'vendaRegistrada' ? 'light' : 'dark'}
      />

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
          onAbrirHistorico={abrirHistorico}
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
          onNavegar={navegarPara}
        />
      )}

      {telaAtual === 'detalhes' && produtoSelecionado && (
        <DetalhesScreen
          produto={produtoSelecionado}
          onVoltar={() => setTelaAtual('catalogo')}
        />
      )}

      {telaAtual === 'historico' && (
        <Historico
          onVoltar={() => setTelaAtual('gestao')}
          onAbrirFiltros={() => {
            alert('A tela de filtros será implementada na próxima etapa.');
          }}
          onSelecionarVenda={abrirDetalhesHistorico}
          onNavegar={navegarPara}
        />
      )}

      {telaAtual === 'detalhesHistorico' && vendaSelecionada && (
        <DetalhesHistorico
          venda={vendaSelecionada}
          onVoltar={() => setTelaAtual('historico')}
        />
      )}

      {telaAtual === 'vender' && (
        <Vender
          produtos={produtos}
          carrinho={carrinho}
          onAdicionarProduto={adicionarProduto}
          onRemoverProduto={removerProduto}
          onAlterarQuantidade={alterarQuantidade}
          onRevisarVenda={revisarVenda}
          onNavegar={navegarPara}
        />
      )}

      {telaAtual === 'revisarVenda' && (
        <RevisarVenda
          produtos={produtos}
          carrinho={carrinho}
          onVoltar={() => setTelaAtual('vender')}
          onConfirmarVenda={confirmarVenda}
        />
      )}

      {telaAtual === 'vendaRegistrada' && vendaRegistrada && (
        <VendaRegistradaScreen
          valorTotal={vendaRegistrada.valorTotal}
          registro={vendaRegistrada.registro}
          onNovaVenda={iniciarNovaVenda}
          onAbrirHistorico={abrirHistorico}
          onNavegar={navegarPara}
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
