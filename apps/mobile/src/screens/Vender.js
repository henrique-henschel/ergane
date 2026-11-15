
import { useMemo, useState } from 'react';

import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  FlatList,
  SafeAreaView,
  StyleSheet,
} from 'react-native';

import Header from '../components/Header';
import BottomNavigation from '../components/BottomNavigation';

const categorias = ['Todos', 'Salgados', 'Bebidas', 'Doces'];

function formatarPreco(valor) {
  return `R$ ${valor.toFixed(2).replace('.', ',')}`;
}

export default function Vender({
  produtos,
  carrinho,
  onAdicionarProduto,
  onRemoverProduto,
  onAlterarQuantidade,
  onRevisarVenda,
  onNavegar,
}) {
  const [busca, setBusca] = useState('');
  const [categoriaAtiva, setCategoriaAtiva] = useState('Todos');

  // Filtra os produtos pelo nome e pela categoria selecionada.
  const produtosFiltrados = useMemo(() => {
    return produtos.filter((produto) => {
      if (produto.arquivado) {
        return false;
      }

      const correspondeBusca = produto.nome
        .toLowerCase()
        .includes(busca.toLowerCase().trim());

      let correspondeCategoria = true;

      if (categoriaAtiva === 'Salgados') {
        correspondeCategoria = produto.categoria === 'Salgado';
      }

      if (categoriaAtiva === 'Bebidas') {
        correspondeCategoria = produto.categoria === 'Suco';
      }

      if (categoriaAtiva === 'Doces') {
        correspondeCategoria = produto.categoria === 'Doce';
      }

      return correspondeBusca && correspondeCategoria;
    });
  }, [produtos, busca, categoriaAtiva]);

  // Retorna a quantidade de um produto no carrinho.
  function obterQuantidade(produtoId) {
    const item = carrinho.find(
      (item) => item.produtoId === produtoId
    );

    return item ? item.quantidade : 0;
  }

  // Calcula a quantidade total de unidades no carrinho.
  const quantidadeTotal = carrinho.reduce(
    (total, item) => total + item.quantidade,
    0
  );

  // Calcula o valor total da venda.
  const valorTotal = carrinho.reduce((total, item) => {
    const produto = produtos.find(
      (p) => p.id === item.produtoId
    );

    return total + (produto ? produto.preco * item.quantidade : 0);
  }, 0);

  // Aumenta a quantidade do produto.
  function aumentarQuantidade(produto) {
    // Não permite adicionar produtos controlados sem estoque.
    if (
      produto.controlaEstoque &&
      produto.quantidade !== null &&
      produto.quantidade <= 0
    ) {
      return;
    }

    onAdicionarProduto(produto);
  }

  // Diminui a quantidade sem permitir valores negativos.
  function diminuirQuantidade(produto) {
    const quantidadeAtual = obterQuantidade(produto.id);

    if (quantidadeAtual <= 0) {
      return;
    }

    if (quantidadeAtual === 1) {
      onRemoverProduto(produto);
      return;
    }

    onAlterarQuantidade(produto.id, quantidadeAtual - 1);
  }

  // Cabeçalho da lista: título, pesquisa e filtros.
  function renderizarCabecalho() {
    return (
      <View>
        <Header />

        <Text style={styles.titulo}>Vender</Text>

        <Text style={styles.subtitulo}>
          Selecione os produtos e suas quantidades.
        </Text>

        <TextInput
          style={styles.busca}
          placeholder="⌕  Buscar produto..."
          placeholderTextColor="#827787"
          value={busca}
          onChangeText={setBusca}
        />

        {/* Filtros de categoria */}
        <View style={styles.filtros}>
          {categorias.map((categoria) => (
            <TouchableOpacity
              key={categoria}
              onPress={() => setCategoriaAtiva(categoria)}
            >
              <Text
                style={[
                  styles.filtroTexto,
                  categoriaAtiva === categoria &&
                    styles.filtroTextoAtivo,
                ]}
              >
                {categoria}
              </Text>
            </TouchableOpacity>
          ))}
        </View>
      </View>
    );
  }

  // Renderiza cada produto da lista.
  function renderizarProduto({ item: produto, index }) {
    const quantidade = obterQuantidade(produto.id);

    const semEstoque =
      produto.controlaEstoque &&
      produto.quantidade !== null &&
      produto.quantidade <= 0;

    return (
      <View
        style={[
          styles.cardProduto,
          index % 2 === 0
            ? styles.cardAmarelo
            : styles.cardBranco,
          quantidade > 0 && styles.cardAdicionado,
          semEstoque && styles.cardIndisponivel,
        ]}
      >
        <Text style={styles.iconeProduto}>
          {produto.categoria === 'Salgado'
            ? '◈'
            : produto.categoria === 'Suco'
            ? '♧'
            : produto.categoria === 'Doce'
            ? '▤'
            : '＋'}
        </Text>

        <Text style={styles.nomeProduto}>
          {produto.nome}
        </Text>

        <Text style={styles.precoProduto}>
          {formatarPreco(produto.preco)}
        </Text>

        {/* Controle de quantidade */}
        <View style={styles.controleQuantidade}>
          <TouchableOpacity
            style={[
              styles.botaoMenos,
              quantidade === 0 && styles.botaoDesativado,
            ]}
            disabled={quantidade === 0}
            onPress={() => diminuirQuantidade(produto)}
          >
            <Text style={styles.textoBotaoQuantidade}>
              −
            </Text>
          </TouchableOpacity>

          <Text style={styles.numeroQuantidade}>
            {quantidade}
          </Text>

          <TouchableOpacity
            style={[
              styles.botaoMais,
              semEstoque && styles.botaoDesativado,
            ]}
            disabled={semEstoque}
            onPress={() => aumentarQuantidade(produto)}
          >
            <Text style={styles.textoBotaoQuantidade}>
              +
            </Text>
          </TouchableOpacity>
        </View>

        {semEstoque && (
          <Text style={styles.textoSemEstoque}>
            Sem estoque
          </Text>
        )}
      </View>
    );
  }

  // Mensagem exibida quando nenhum produto for encontrado.
  function renderizarListaVazia() {
    return (
      <View style={styles.mensagemVazia}>
        <Text style={styles.mensagemVaziaTexto}>
          Nenhum produto encontrado.
        </Text>
      </View>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <FlatList
        data={produtosFiltrados}
        keyExtractor={(item) => String(item.id)}
        renderItem={renderizarProduto}
        numColumns={2}
        columnWrapperStyle={styles.linhaProdutos}
        contentContainerStyle={styles.conteudo}
        ListHeaderComponent={renderizarCabecalho()}
        ListEmptyComponent={renderizarListaVazia}
        keyboardShouldPersistTaps="handled"
      />

      {/* Rodapé do carrinho */}
      <View style={styles.areaCarrinho}>
        <TouchableOpacity
          style={[
            styles.botaoRevisar,
            quantidadeTotal === 0 && styles.botaoDesativado,
          ]}
          disabled={quantidadeTotal === 0}
          onPress={onRevisarVenda}
        >
          <Text style={styles.textoBotaoRevisar}>
            {quantidadeTotal === 0
              ? 'Ver carrinho - vazio'
              : `Revisar · ${formatarPreco(valorTotal)}`}
          </Text>
        </TouchableOpacity>
      </View>

      <BottomNavigation
        telaAtiva="vender"
        onNavegar={onNavegar}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#E7F0E5',
  },

  conteudo: {
    paddingHorizontal: 20,
    paddingTop: 15,
    paddingBottom: 15,
  },

  titulo: {
    fontSize: 25,
    fontWeight: '600',
    color: '#402044',
  },

  subtitulo: {
    fontSize: 13,
    color: '#57465A',
    marginTop: 5,
    marginBottom: 15,
  },

  busca: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    height: 48,
    paddingHorizontal: 15,
    fontSize: 14,
    marginBottom: 18,
    color: '#402044',
  },

  filtros: {
    flexDirection: 'row',
    gap: 15,
    marginBottom: 18,
    paddingHorizontal: 5,
  },

  filtroTexto: {
    fontSize: 12,
    color: '#063D2E',
  },

  filtroTextoAtivo: {
    fontWeight: '700',
    textDecorationLine: 'underline',
  },

  linhaProdutos: {
    justifyContent: 'space-between',
  },

  cardProduto: {
    width: '48%',
    minHeight: 155,
    borderRadius: 16,
    padding: 12,
    marginBottom: 10,
    justifyContent: 'space-between',
  },

  cardAmarelo: {
    backgroundColor: '#F8E9CC',
  },

  cardBranco: {
    backgroundColor: '#FFFFFF',
  },

  cardAdicionado: {
    borderWidth: 1.5,
    borderColor: '#063D2E',
  },

  cardIndisponivel: {
    opacity: 0.5,
  },

  iconeProduto: {
    fontSize: 27,
    color: '#063D2E',
    textAlign: 'center',
    marginBottom: 8,
  },

  nomeProduto: {
    fontSize: 13,
    fontWeight: '600',
    color: '#402044',
  },

  precoProduto: {
    fontSize: 12,
    color: '#063D2E',
    marginTop: 5,
  },

  controleQuantidade: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: 12,
  },

  botaoMenos: {
    width: 32,
    height: 32,
    borderRadius: 9,
    backgroundColor: '#D94B4B',
    alignItems: 'center',
    justifyContent: 'center',
  },

  botaoMais: {
    width: 32,
    height: 32,
    borderRadius: 9,
    backgroundColor: '#249653',
    alignItems: 'center',
    justifyContent: 'center',
  },

  botaoDesativado: {
    opacity: 0.35,
  },

  textoBotaoQuantidade: {
    fontSize: 21,
    fontWeight: '600',
    color: '#FFFFFF',
  },

  numeroQuantidade: {
    fontSize: 16,
    fontWeight: '600',
    color: '#402044',
  },

  textoSemEstoque: {
    fontSize: 10,
    color: '#A13C3C',
    textAlign: 'center',
    marginTop: 5,
  },

  mensagemVazia: {
    backgroundColor: '#FFFFFF',
    borderRadius: 15,
    padding: 20,
    alignItems: 'center',
  },

  mensagemVaziaTexto: {
    color: '#57465A',
    fontSize: 13,
  },

  areaCarrinho: {
    paddingHorizontal: 20,
    paddingVertical: 10,
    backgroundColor: '#E7F0E5',
  },

  botaoRevisar: {
    backgroundColor: '#063D2E',
    borderRadius: 13,
    paddingVertical: 13,
    alignItems: 'center',
  },

  textoBotaoRevisar: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '600',
  },
});
