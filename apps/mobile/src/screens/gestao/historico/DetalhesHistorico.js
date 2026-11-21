
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  SafeAreaView,
  Alert,
} from 'react-native';

import Header from '../../../components/Header';

export default function DetalhesVenda({
  venda,
  onVoltar,
}) {
  function formatarPreco(valor) {
    return valor.toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    });
  }

  const pendente = venda.status === 'Recebimento pendente';

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <Header onVoltar={onVoltar} />

        <ScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.lista}
        >
          <Text style={styles.titulo}>
            Venda #{venda.id}
          </Text>

          <Text style={styles.subtitulo}>
            {venda.data}, {venda.horario} • {venda.responsavel}
          </Text>

          <View style={styles.listaProdutos}>
            {venda.produtos.map((produto) => (
              <View key={produto.id} style={styles.cardProduto}>
                <Text style={styles.nomeProduto}>
                  {produto.nome}
                </Text>

                <Text style={styles.descricaoProduto}>
                  {produto.quantidade} x {formatarPreco(produto.preco)}
                </Text>
              </View>
            ))}
          </View>

          <View style={styles.cardTotal}>
            <Text style={styles.rotuloTotal}>
              Total registrado
            </Text>

            <Text style={styles.valorTotal}>
              {formatarPreco(venda.valorTotal)}
            </Text>

            <Text style={styles.valorReceber}>
              {pendente
                ? `A receber: ${formatarPreco(venda.valorTotal)}`
                : `Recebido: ${formatarPreco(venda.valorTotal)}`}
            </Text>
          </View>

          <TouchableOpacity
            style={styles.cardAlteracoes}
            activeOpacity={0.8}
            onPress={() =>
              Alert.alert(
                'Histórico de alterações',
                'Ainda não existem alterações registradas.'
              )
            }
          >
            <Text style={styles.tituloAlteracoes}>
              Histórico de alterações ›
            </Text>

            <Text style={styles.descricaoAlteracoes}>
              Confira autor, data e versões.
            </Text>
          </TouchableOpacity>
        </ScrollView>

        <View style={styles.acoes}>
          <TouchableOpacity
            style={styles.botaoRecebimento}
            onPress={() =>
              Alert.alert(
                'Registrar recebimento',
                'Essa funcionalidade será implementada posteriormente.'
              )
            }
          >
            <Text style={styles.textoBotaoPrincipal}>
              Registrar recebimento
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.botaoCorrigir}
            onPress={() =>
              Alert.alert(
                'Corrigir venda',
                'A correção de vendas será implementada posteriormente.'
              )
            }
          >
            <Text style={styles.textoBotaoSecundario}>
              Corrigir venda
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.botaoCancelar}
            onPress={() =>
              Alert.alert(
                'Cancelar venda',
                'O cancelamento de vendas será implementado posteriormente.'
              )
            }
          >
            <Text style={styles.textoBotaoCancelar}>
              Cancelar venda
            </Text>
          </TouchableOpacity>
        </View>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F3F6',
  },
  content: {
    flex: 1,
    paddingHorizontal: 20,
    paddingTop: 15,
  },
  lista: {
    paddingBottom: 20,
  },
  titulo: {
    fontSize: 27,
    fontWeight: '600',
    color: '#402044',
  },
  subtitulo: {
    fontSize: 12,
    color: '#6B5D6D',
    marginTop: 5,
  },
  listaProdutos: {
    marginTop: 18,
    gap: 10,
  },
  cardProduto: {
    backgroundColor: '#F8E9CC',
    padding: 15,
    borderRadius: 17,
    minHeight: 70,
    justifyContent: 'center',
  },
  nomeProduto: {
    fontSize: 13,
    fontWeight: '600',
    color: '#402044',
  },
  descricaoProduto: {
    fontSize: 11,
    color: '#6B5D6D',
    marginTop: 6,
  },
  cardTotal: {
    backgroundColor: '#003D2E',
    padding: 17,
    borderRadius: 17,
    marginTop: 10,
    minHeight: 100,
    justifyContent: 'center',
  },
  rotuloTotal: {
    fontSize: 11,
    color: '#FFFFFF',
  },
  valorTotal: {
    fontSize: 27,
    fontWeight: '600',
    color: '#FFFFFF',
    marginTop: 5,
  },
  valorReceber: {
    fontSize: 11,
    color: '#FFFFFF',
    marginTop: 5,
  },
  cardAlteracoes: {
    backgroundColor: '#EFE2F5',
    padding: 15,
    borderRadius: 17,
    marginTop: 10,
    minHeight: 70,
    justifyContent: 'center',
  },
  tituloAlteracoes: {
    fontSize: 13,
    fontWeight: '600',
    color: '#402044',
  },
  descricaoAlteracoes: {
    fontSize: 11,
    color: '#6B5D6D',
    marginTop: 6,
  },
  acoes: {
    gap: 8,
    paddingTop: 10,
    paddingBottom: 12,
  },
  botaoRecebimento: {
    backgroundColor: '#003D2E',
    minHeight: 43,
    borderRadius: 14,
    justifyContent: 'center',
    alignItems: 'center',
  },
  textoBotaoPrincipal: {
    fontSize: 12,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  botaoCorrigir: {
    backgroundColor: '#FFFFFF',
    minHeight: 43,
    borderRadius: 14,
    justifyContent: 'center',
    alignItems: 'center',
  },
  textoBotaoSecundario: {
    fontSize: 12,
    fontWeight: '600',
    color: '#003D2E',
  },
  botaoCancelar: {
    backgroundColor: '#FCEDEA',
    minHeight: 43,
    borderRadius: 14,
    justifyContent: 'center',
    alignItems: 'center',
  },
  textoBotaoCancelar: {
    fontSize: 12,
    fontWeight: '600',
    color: '#C62828',
  },
});
