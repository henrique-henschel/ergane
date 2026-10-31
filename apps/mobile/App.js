
import { useState } from 'react';
import { StatusBar } from 'expo-status-bar';

import GestaoScreen from './screens/GestaoScreen';
import CatalogoScreen from './screens/CatalogoScreen';
import DetalhesScreen from './screens/DetalhesScreen';

const produtosIniciais = [
  {
    id: 'prod-001',
    nome: 'Pastel de Carne',
    categoria: 'Salgado',
    preco: 8.00,
    quantidade: 12,
    controlaEstoque: true,
    arquivado: false,
    custo: 3.00,
    validade: null,
    descricao: null,
  },
  {
    id: 'prod-002',
    nome: 'Suco de Laranja',
    categoria: 'Suco',
    preco: 7.00,
    quantidade: null,
    controlaEstoque: false,
    arquivado: false,
    custo: null,
    validade: null,
    descricao: null,
  },
  {
    id: 'prod-003',
    nome: 'Brigadeiro',
    categoria: 'Doce',
    preco: 3.50,
    quantidade: 0,
    controlaEstoque: true,
    arquivado: false,
    custo: null,
    validade: null,
    descricao: null,
  },
  {
    id: 'prod-004',
    nome: 'Água Mineral',
    categoria: null,
    preco: 2.50,
    quantidade: 20,
    controlaEstoque: true,
    arquivado: false,
    custo: 1.00,
    validade: null,
    descricao: 'Garrafa de água mineral',
  },

];

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

  function abrirCatalogo() {
    setTelaAtual('catalogo');
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

  return (
    <>
      <StatusBar style="dark" />

      {telaAtual === 'gestao' && (
        <GestaoScreen
          onAbrirCatalogo={abrirCatalogo}
          onVoltarInicio={voltarInicio}
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
      
    </>
  );
}
