
import { useMemo, useState } from 'react';

import {
  View,
  Text,
  TextInput,
  StyleSheet,
  TouchableOpacity,
  FlatList,
  SafeAreaView,
} from 'react-native';

import Header from '../../../components/Header';
import BottomNavigation from '../../../components/BottomNavigation';

export default function CatalogoScreen({
  produtos,
  filtros,
  onAtualizarFiltros,
  onSelecionarProduto,
  onVoltar,
  onNavegar,
}) {
  // Controla qual menu de filtro está aberto.
  const [filtroAberto, setFiltroAberto] = useState('');

  // Filtra os produtos conforme os critérios selecionados.
  const produtosFiltrados = useMemo(() => {
    return produtos.filter((produto) => {
      // Produtos arquivados não aparecem no catálogo.
      if (produto.arquivado) {
        return false;
      }

      const nomeCorresponde = produto.nome
        .toLowerCase()
        .includes(filtros.nome.toLowerCase().trim());

      const categoriaCorresponde =
        filtros.categoria === 'Todas' ||
        (filtros.categoria === 'Sem categoria' &&
          !produto.categoria) ||
        produto.categoria === filtros.categoria;

      let disponibilidadeCorresponde = true;

      if (filtros.disponibilidade === 'Disponível') {
        disponibilidadeCorresponde =
          produto.controlaEstoque &&
          produto.quantidade > 0;
      }

      if (filtros.disponibilidade === 'Sem estoque') {
        disponibilidadeCorresponde =
          produto.controlaEstoque &&
          produto.quantidade === 0;
      }

      if (filtros.disponibilidade === 'Desconhecida') {
        disponibilidadeCorresponde =
          !produto.controlaEstoque ||
          produto.quantidade === null;
      }

      return (
        nomeCorresponde &&
        categoriaCorresponde &&
        disponibilidadeCorresponde
      );
    });
  }, [produtos, filtros]);

  // Atualiza um filtro.
  function alterarFiltro(campo, valor) {
    onAtualizarFiltros({
      [campo]: valor,
    });
  }

  // Limpa todos os filtros.
  function limparFiltros() {
    onAtualizarFiltros({
      nome: '',
      categoria: 'Todas',
      disponibilidade: 'Todas',
    });

    setFiltroAberto('');
  }

  // Informa a disponibilidade de cada produto.
  function obterDisponibilidade(produto) {
    if (
      !produto.controlaEstoque ||
      produto.quantidade === null
    ) {
      return 'Estoque não informado';
    }

    if (produto.quantidade === 0) {
      return 'Sem estoque';
    }

    return `${produto.quantidade} disponíveis`;
  }

  // Renderiza cada produto da lista.
  function renderizarProduto({ item: produto }) {
    return (
      <TouchableOpacity
        style={styles.card}
        onPress={() => onSelecionarProduto(produto)}
      >
        <View style={styles.cardLinha}>
          <Text style={styles.nomeProduto}>
            {produto.nome} ›
          </Text>

          <Text style={styles.preco}>
            R$ {produto.preco.toFixed(2).replace('.', ',')}
          </Text>
        </View>

        <Text style={styles.informacao}>
          {produto.categoria || 'Sem categoria'}
          {' · '}
          {obterDisponibilidade(produto)}
        </Text>
      </TouchableOpacity>
    );
  }

  // Exibe uma mensagem quando nenhum produto é encontrado.
  function renderizarListaVazia() {
    return (
      <View style={styles.vazio}>
        <Text style={styles.vazioTitulo}>
          Nenhum produto encontrado
        </Text>

        <Text style={styles.vazioTexto}>
          Tente alterar os termos da busca ou os filtros.
        </Text>

        <TouchableOpacity
          style={styles.botaoLimpar}
          onPress={limparFiltros}
        >
          <Text style={styles.botaoLimparTexto}>
            Limpar filtros
          </Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <SafeAreaView style={styles.container}>

      {/* Lista de produtos e conteúdo superior/inferior */}
      <FlatList
        data={produtosFiltrados}
        keyExtractor={(item) => String(item.id)}
        renderItem={renderizarProduto}
        ListHeaderComponent={
          <>
            {/* Header */}
            <Header onVoltar={onVoltar} />

            {/* Título e subtítulo */}
            <Text style={styles.titulo}>
              Seu catálogo
            </Text>

            <Text style={styles.subtitulo}>
              Produtos organizados, venda mais leve.
            </Text>

            {/* Barra de pesquisa */}
            <TextInput
              style={styles.busca}
              placeholder="⌕  Buscar produto..."
              placeholderTextColor="#827787"
              value={filtros.nome}
              onChangeText={(valor) =>
                alterarFiltro('nome', valor)
              }
            />

            {/* Seletores de filtros */}
            <View style={styles.linhaFiltros}>

              {/* Filtro de categoria */}
              <View style={styles.filtroContainer}>
                <TouchableOpacity
                  style={styles.botaoFiltro}
                  onPress={() =>
                    setFiltroAberto(
                      filtroAberto === 'categoria'
                        ? ''
                        : 'categoria'
                    )
                  }
                >
                  <View style={styles.conteudoFiltro}>
                    <Text style={styles.rotuloFiltro}>
                      Categoria
                    </Text>

                    <Text
                      style={styles.valorFiltro}
                      numberOfLines={1}
                    >
                      {filtros.categoria}
                    </Text>
                  </View>

                  <Text style={styles.setaFiltro}>
                    {filtroAberto === 'categoria' ? '⌃' : '⌄'}
                  </Text>
                </TouchableOpacity>

                {filtroAberto === 'categoria' && (
                  <View style={styles.listaOpcoesFiltro}>
                    {[
                      'Todas',
                      'Salgado',
                      'Suco',
                      'Doce',
                      'Sem categoria',
                    ].map((categoria) => (
                      <TouchableOpacity
                        key={categoria}
                        style={styles.itemFiltro}
                        onPress={() => {
                          alterarFiltro('categoria', categoria);
                          setFiltroAberto('');
                        }}
                      >
                        <Text
                          style={[
                            styles.textoItemFiltro,
                            filtros.categoria === categoria &&
                              styles.textoItemFiltroAtivo,
                          ]}
                        >
                          {categoria}
                        </Text>
                      </TouchableOpacity>
                    ))}
                  </View>
                )}
              </View>

              {/* Filtro de disponibilidade */}
              <View style={styles.filtroContainer}>
                <TouchableOpacity
                  style={styles.botaoFiltro}
                  onPress={() =>
                    setFiltroAberto(
                      filtroAberto === 'disponibilidade'
                        ? ''
                        : 'disponibilidade'
                    )
                  }
                >
                  <View style={styles.conteudoFiltro}>
                    <Text style={styles.rotuloFiltro}>
                      Disponibilidade
                    </Text>

                    <Text
                      style={styles.valorFiltro}
                      numberOfLines={1}
                    >
                      {filtros.disponibilidade}
                    </Text>
                  </View>

                  <Text style={styles.setaFiltro}>
                    {filtroAberto === 'disponibilidade' ? '⌃' : '⌄'}
                  </Text>
                </TouchableOpacity>

                {filtroAberto === 'disponibilidade' && (
                  <View style={styles.listaOpcoesFiltro}>
                    {[
                      'Todas',
                      'Disponível',
                      'Sem estoque',
                      'Desconhecida',
                    ].map((disponibilidade) => (
                      <TouchableOpacity
                        key={disponibilidade}
                        style={styles.itemFiltro}
                        onPress={() => {
                          alterarFiltro(
                            'disponibilidade',
                            disponibilidade
                          );
                          setFiltroAberto('');
                        }}
                      >
                        <Text
                          style={[
                            styles.textoItemFiltro,
                            filtros.disponibilidade === disponibilidade &&
                              styles.textoItemFiltroAtivo,
                          ]}
                        >
                          {disponibilidade}
                        </Text>
                      </TouchableOpacity>
                    ))}
                  </View>
                )}
              </View>
            </View>

            {/* Quantidade de resultados */}
            <Text style={styles.resultados}>
              {produtosFiltrados.length} produto(s) encontrado(s)
            </Text>
          </>
        }
        ListEmptyComponent={renderizarListaVazia}
        ListFooterComponent={
          /* Card de categorias depois dos produtos */
          <TouchableOpacity
            style={styles.cardCategorias}
            activeOpacity={0.9}
          >
            <View>
              <Text style={styles.tituloCategorias}>
                Categorias ›
              </Text>

              <Text style={styles.descricaoCategorias}>
                Salgados, bebidas e doces
              </Text>
            </View>
          </TouchableOpacity>
        }
        contentContainerStyle={styles.conteudo}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      />

      {/* Botão de adicionar produto */}
      <View style={styles.areaAdicionar}>
        <TouchableOpacity
          style={styles.botaoAdicionar}
          activeOpacity={0.9}
        >
          <Text style={styles.textoBotaoAdicionar}>
            Adicionar produto
          </Text>
        </TouchableOpacity>
      </View>

      {/* Navegação inferior */}
      <BottomNavigation
        telaAtiva="gestao"
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
    marginBottom: 15,
    color: '#402044',
  },

  // Linha dos dois filtros.
  linhaFiltros: {
    flexDirection: 'row',
    gap: 10,
    marginBottom: 18,
    alignItems: 'flex-start',
  },

  filtroContainer: {
    flex: 1,
    minWidth: 0,
  },

  conteudoFiltro: {
    flex: 1,
    minWidth: 0,
  },

  botaoFiltro: {
    backgroundColor: '#FFFFFF',
    borderRadius: 13,
    paddingHorizontal: 12,
    paddingVertical: 11,
    minHeight: 62,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 5,
  },

  rotuloFiltro: {
    fontSize: 12,
    fontWeight: '600',
    color: '#402044',
  },

  valorFiltro: {
    fontSize: 11,
    color: '#827787',
    marginTop: 4,
  },

  setaFiltro: {
    fontSize: 19,
    color: '#402044',
  },

  // Opções dos filtros.
  listaOpcoesFiltro: {
    backgroundColor: '#FFFFFF',
    borderRadius: 13,
    marginTop: 5,
    paddingVertical: 5,
  },

  itemFiltro: {
    paddingHorizontal: 10,
    paddingVertical: 11,
  },

  textoItemFiltro: {
    fontSize: 11,
    color: '#402044',
  },

  textoItemFiltroAtivo: {
    color: '#063D2E',
    fontWeight: '700',
  },

  resultados: {
    fontSize: 12,
    color: '#57465A',
    marginBottom: 10,
  },

  // Cards dos produtos.
  card: {
    backgroundColor: '#F8E9CC',
    borderRadius: 15,
    padding: 13,
    marginBottom: 9,
  },

  cardLinha: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    flexWrap: 'wrap',
    gap: 5,
  },

  nomeProduto: {
    color: '#402044',
    fontSize: 14,
    fontWeight: '600',
  },

  preco: {
    color: '#402044',
    fontSize: 13,
    fontWeight: '600',
  },

  informacao: {
    color: '#6B5D6D',
    fontSize: 12,
    marginTop: 6,
  },

  // Estado vazio.
  vazio: {
    backgroundColor: '#FFFFFF',
    padding: 20,
    borderRadius: 16,
    alignItems: 'center',
    marginTop: 5,
  },

  vazioTitulo: {
    color: '#402044',
    fontSize: 15,
    fontWeight: '600',
  },

  vazioTexto: {
    color: '#6B5D6D',
    fontSize: 12,
    textAlign: 'center',
    marginTop: 7,
  },

  botaoLimpar: {
    backgroundColor: '#063D2E',
    borderRadius: 12,
    padding: 12,
    marginTop: 15,
    width: '100%',
    alignItems: 'center',
  },

  botaoLimparTexto: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '600',
  },

  // Card de categorias.
  cardCategorias: {
    backgroundColor: '#FFFFFF',
    borderRadius: 15,
    padding: 14,
    marginTop: 2,
    marginBottom: 10,
  },

  tituloCategorias: {
    color: '#402044',
    fontSize: 14,
    fontWeight: '600',
  },

  descricaoCategorias: {
    color: '#6B5D6D',
    fontSize: 12,
    marginTop: 6,
  },

  // Área fixa do botão adicionar.
  areaAdicionar: {
    paddingHorizontal: 20,
    paddingTop: 8,
    paddingBottom: 12,
    backgroundColor: '#E7F0E5',
  },

  botaoAdicionar: {
    backgroundColor: '#063D2E',
    borderRadius: 13,
    paddingVertical: 14,
    alignItems: 'center',
  },

  textoBotaoAdicionar: {
    fontSize: 13,
    fontWeight: '600',
    color: '#FFFFFF',
  },
});
