
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  SafeAreaView,
} from 'react-native';

import Header from '../components/Header';

export default function DetalhesScreen({ produto, onVoltar }) {
  function formatarValor(valor) {
    if (valor === null || valor === undefined || valor === '') {
      return 'Não informado';
    }

    return `R$ ${Number(valor).toFixed(2).replace('.', ',')}`;
  }

  function formatarCampo(valor) {
    if (valor === null || valor === undefined || valor === '') {
      return 'Não informado';
    }

    return String(valor);
  }

  function obterDisponibilidade() {
    if (!produto.controlaEstoque || produto.quantidade === null) {
      return 'Saldo desconhecido';
    }

    if (produto.quantidade === 0) {
      return 'Sem unidades disponíveis';
    }

    return `${produto.quantidade} unidades disponíveis`;
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.conteudo}>
        <Header
            onVoltar={onVoltar}
            modoVoltar={true}
        />

        <Text style={styles.titulo}>Detalhes do produto</Text>

        <Text style={styles.subtitulo}>
          Confira todas as informações cadastradas.
        </Text>

        <View style={styles.identificacao}>
          <Text style={styles.nomeProduto}>
            {produto.nome}
          </Text>

          <Text style={styles.idProduto}>
            ID: {produto.id}
          </Text>
        </View>

        <View style={styles.campo}>
          <Text style={styles.rotulo}>Categoria</Text>
          <Text style={styles.valor}>
            {formatarCampo(produto.categoria)}
          </Text>
        </View>

        <View style={styles.campo}>
          <Text style={styles.rotulo}>Preço de venda</Text>
          <Text style={styles.valor}>
            {formatarValor(produto.preco)}
          </Text>
        </View>

        <View style={styles.campo}>
          <Text style={styles.rotulo}>Custo por unidade</Text>
          <Text style={styles.valor}>
            {formatarValor(produto.custo)}
          </Text>
        </View>

        <View style={styles.campo}>
          <Text style={styles.rotulo}>Disponibilidade</Text>
          <Text style={styles.valor}>
            {obterDisponibilidade()}
          </Text>
        </View>

        <View style={styles.campo}>
          <Text style={styles.rotulo}>Controle de estoque</Text>
          <Text style={styles.valor}>
            {produto.controlaEstoque
              ? 'Acompanhado'
              : 'Não acompanhado'}
          </Text>
        </View>

        <View style={styles.campo}>
          <Text style={styles.rotulo}>Validade</Text>
          <Text style={styles.valor}>
            {formatarCampo(produto.validade)}
          </Text>
        </View>

        <View style={styles.campo}>
          <Text style={styles.rotulo}>Descrição</Text>
          <Text style={styles.valor}>
            {formatarCampo(produto.descricao)}
          </Text>
        </View>

        <View style={styles.campo}>
          <Text style={styles.rotulo}>Status</Text>
          <Text style={styles.valor}>
            {produto.arquivado ? 'Arquivado' : 'Ativo'}
          </Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F4F2F5',
  },
  conteudo: {
    paddingHorizontal: 20,
    paddingTop: 15,
    paddingBottom: 30,
  },
  titulo: {
    fontSize: 25,
    fontWeight: '600',
    color: '#402044',
  },
  subtitulo: {
    fontSize: 13,
    color: '#746A77',
    marginTop: 5,
    marginBottom: 18,
  },
  identificacao: {
    backgroundColor: '#E5F0E5',
    padding: 16,
    borderRadius: 15,
    marginBottom: 12,
  },
  nomeProduto: {
    color: '#402044',
    fontSize: 18,
    fontWeight: '600',
  },
  idProduto: {
    color: '#746A77',
    fontSize: 11,
    marginTop: 6,
  },
  campo: {
    backgroundColor: '#FFFFFF',
    borderRadius: 15,
    padding: 15,
    marginBottom: 9,
    minHeight: 65,
    justifyContent: 'center',
  },
  rotulo: {
    color: '#746A77',
    fontSize: 12,
    marginBottom: 6,
  },
  valor: {
    color: '#402044',
    fontSize: 14,
  },
});
