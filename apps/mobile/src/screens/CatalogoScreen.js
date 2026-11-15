
import { useMemo } from 'react';

import {View, Text, TextInput, StyleSheet, TouchableOpacity, ScrollView, Image, SafeAreaView } from 'react-native';

import Header from '../components/Header';

import BottomNavigation from '../components/BottomNavigation';

const TAMANHO_PAGINA = 5;

export default function CatalogoScreen({
  produtos,
  filtros,
  onAtualizarFiltros,
  onSelecionarProduto,
  onVoltar,
  onNavegar,
}) {
  const produtosFiltrados = useMemo(() => {
    return produtos.filter((produto) => {
      // Produtos arquivados não aparecem no catálogo selecionável.
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

  const totalPaginas = Math.max(
    1,
    Math.ceil(produtosFiltrados.length / TAMANHO_PAGINA)
  );

  const paginaAtual = Math.min(filtros.pagina, totalPaginas);

  const inicio = (paginaAtual - 1) * TAMANHO_PAGINA;

  const produtosPagina = produtosFiltrados.slice(
    inicio,
    inicio + TAMANHO_PAGINA
  );

  function alterarFiltro(campo, valor) {
    onAtualizarFiltros({
      [campo]: valor,
      pagina: 1,
    });
  }

  function limparFiltros() {
    onAtualizarFiltros({
      nome: '',
      categoria: 'Todas',
      disponibilidade: 'Todas',
      pagina: 1,
    });
  }

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

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        contentContainerStyle={styles.conteudo}
        keyboardShouldPersistTaps="handled"
      >
        <Header onVoltar={onVoltar} />

        <Text style={styles.titulo}>Seu catálogo</Text>

        <Text style={styles.subtitulo}>
          Produtos organizados, venda mais leve.
        </Text>

        <TextInput
          style={styles.busca}
          placeholder="⌕  Buscar produto..."
          placeholderTextColor="#827787"
          value={filtros.nome}
          onChangeText={(valor) => alterarFiltro('nome', valor)}
        />

        <Text style={styles.rotulo}>Categoria</Text>

        <View style={styles.opcoes}>
          {[
            'Todas',
            'Salgado',
            'Suco',
            'Doce',
            'Sem categoria',
          ].map((categoria) => (
            <TouchableOpacity
              key={categoria}
              style={[
                styles.opcao,
                filtros.categoria === categoria && styles.opcaoAtiva,
              ]}
              onPress={() => alterarFiltro('categoria', categoria)}
            >
              <Text
                style={[
                  styles.textoOpcao,
                  filtros.categoria === categoria &&
                    styles.textoOpcaoAtiva,
                ]}
              >
                {categoria}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        <Text style={styles.rotulo}>Disponibilidade</Text>

        <View style={styles.opcoes}>
          {[
            'Todas',
            'Disponível',
            'Sem estoque',
            'Desconhecida',
          ].map((disponibilidade) => (
            <TouchableOpacity
              key={disponibilidade}
              style={[
                styles.opcao,
                filtros.disponibilidade === disponibilidade &&
                  styles.opcaoAtiva,
              ]}
              onPress={() =>
                alterarFiltro('disponibilidade', disponibilidade)
              }
            >
              <Text
                style={[
                  styles.textoOpcao,
                  filtros.disponibilidade === disponibilidade &&
                    styles.textoOpcaoAtiva,
                ]}
              >
                {disponibilidade}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        <Text style={styles.resultados}>
          {produtosFiltrados.length} produto(s) encontrado(s)
        </Text>

        {produtosPagina.length > 0 ? (
          produtosPagina.map((produto) => (
            <TouchableOpacity
              key={produto.id}
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
          ))
        ) : (
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
        )}

        {produtosFiltrados.length > 0 && (
          <View style={styles.paginacao}>
            <TouchableOpacity
              disabled={paginaAtual === 1}
              onPress={() =>
                onAtualizarFiltros({ pagina: paginaAtual - 1 })
              }
            >
              <Text
                style={[
                  styles.paginaBotao,
                  paginaAtual === 1 && styles.paginaDesativada,
                ]}
              >
                Anterior
              </Text>
            </TouchableOpacity>

            <Text style={styles.paginaTexto}>
              Página {paginaAtual} de {totalPaginas}
            </Text>

            <TouchableOpacity
              disabled={paginaAtual === totalPaginas}
              onPress={() =>
                onAtualizarFiltros({ pagina: paginaAtual + 1 })
              }
            >
              <Text
                style={[
                  styles.paginaBotao,
                  paginaAtual === totalPaginas &&
                    styles.paginaDesativada,
                ]}
              >
                Próxima
              </Text>
            </TouchableOpacity>
          </View>
        )}
      </ScrollView>

      <BottomNavigation telaAtiva="gestao" onNavegar={onNavegar}/>
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
    paddingBottom: 25,
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
  rotulo: {
    fontSize: 13,
    fontWeight: '600',
    color: '#402044',
    marginBottom: 8,
  },
  opcoes: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 7,
    marginBottom: 15,
  },
  opcao: {
    backgroundColor: '#FFFFFF',
    borderRadius: 15,
    paddingHorizontal: 12,
    paddingVertical: 9,
  },
  opcaoAtiva: {
    backgroundColor: '#063D2E',
  },
  textoOpcao: {
    fontSize: 12,
    color: '#402044',
  },
  textoOpcaoAtiva: {
    color: '#FFFFFF',
  },
  resultados: {
    fontSize: 12,
    color: '#57465A',
    marginBottom: 10,
  },
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
  paginacao: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 15,
    marginBottom: 10,
  },
  paginaBotao: {
    color: '#063D2E',
    fontSize: 12,
    fontWeight: '600',
  },
  paginaDesativada: {
    color: '#A9A1AA',
  },
  paginaTexto: {
    color: '#57465A',
    fontSize: 12,
  },
});
