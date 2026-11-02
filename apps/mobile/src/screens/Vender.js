
import { useMemo, useState } from 'react';

import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
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
  onRevisarVenda,
  onNavegar,
}) {
  const [busca, setBusca] = useState('');
  const [categoriaAtiva, setCategoriaAtiva] = useState('Todos');
  const [produtosAdicionados, setProdutosAdicionados] = useState([]);

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

  // Verifica se o produto já está no carrinho.
  function obterItemCarrinho(produtoId) {
    return carrinho.find((item) => item.produtoId === produtoId);
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

  function adicionarProduto(produto) {
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

  // Alterna entre adicionar e desmarcar o produto.
  function alternarAdicao(produto) {
    const jaAdicionado = produtosAdicionados.includes(produto.id);

    if (jaAdicionado) {
        onRemoverProduto(produto);

        setProdutosAdicionados((atuais) =>
        atuais.filter((id) => id !== produto.id)
        );

        return;
    }

    adicionarProduto(produto);

    setProdutosAdicionados((atuais) => [
        ...atuais,
        produto.id,
    ]);
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        contentContainerStyle={styles.conteudo}
        keyboardShouldPersistTaps="handled"
      >
        <Header />

        <Text style={styles.titulo}>Vender</Text>

        <Text style={styles.subtitulo}>
          Toque nos produtos para montar a venda.
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

        {/* Produtos */}
        <View style={styles.gradeProdutos}>
          {produtosFiltrados.map((produto, index) => {
            const itemCarrinho = obterItemCarrinho(produto.id);

            const semEstoque =
              produto.controlaEstoque &&
              produto.quantidade !== null &&
              produto.quantidade <= 0;

            return (
              <TouchableOpacity
                key={produto.id}
                style={[
                  styles.cardProduto,
                  index % 2 === 0
                    ? styles.cardAmarelo
                    : styles.cardBranco,
                  produtosAdicionados.includes(produto.id) && styles.cardAdicionado,
                  semEstoque && styles.cardIndisponivel,
                ]}
                disabled={semEstoque}
                onPress={() => alternarAdicao(produto)}
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

                <Text style={styles.acaoProduto}>
                  {semEstoque
                    ? 'Sem estoque'
                    : produtosAdicionados.includes(produto.id)
                    ? '✓ Adicionado'
                    : '+ Adicionar'}
                </Text>
              </TouchableOpacity>
            );
          })}
        </View>

        {produtosFiltrados.length === 0 && (
          <View style={styles.mensagemVazia}>
            <Text style={styles.mensagemVaziaTexto}>
              Nenhum produto encontrado.
            </Text>
          </View>
        )}
      </ScrollView>

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
  gradeProdutos: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    gap: 10,
  },
  cardProduto: {
    width: '48%',
    minHeight: 125,
    borderRadius: 16,
    padding: 12,
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
  acaoProduto: {
    fontSize: 11,
    color: '#063D2E',
    marginTop: 7,
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
  botaoDesativado: {
    backgroundColor: '#FFFFFF',
  },
  textoBotaoRevisar: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '600',
  },
});
