
import { useState } from 'react';

import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  SafeAreaView,
  StyleSheet,
} from 'react-native';

import Header from '../components/Header';

const formasPagamento = ['PIX', 'Crédito', 'Débito'];

function formatarPreco(valor) {
  return `R$ ${valor.toFixed(2).replace('.', ',')}`;
}

export default function RevisarVenda({
  produtos,
  carrinho,
  onVoltar,
  onAlterarQuantidade,
  onRemoverProduto,
}) {
  const [formaPagamento, setFormaPagamento] = useState('');

  // Relaciona os produtos do catálogo aos itens do carrinho.
  const itensVenda = carrinho
    .map((item) => {
      const produto = produtos.find(
        (p) => p.id === item.produtoId
      );

      if (!produto) {
        return null;
      }

      return {
        ...produto,
        quantidade: item.quantidade,
      };
    })
    .filter((item) => item !== null);

  // Calcula o valor total da venda.
  const valorTotal = itensVenda.reduce(
    (total, item) => total + item.preco * item.quantidade,
    0
  );

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        contentContainerStyle={styles.conteudo}
        showsVerticalScrollIndicator={false}
      >
        <Header onVoltar={onVoltar} modoVoltar={true}/>

        <Text style={styles.titulo}>Revisar venda</Text>

        <Text style={styles.subtitulo}>
          Confira os {itensVenda.length} itens antes de registrar.
        </Text>

        {/* Produtos da venda */}
        <View style={styles.listaProdutos}>
          {itensVenda.map((item) => (
            <View key={item.id} style={styles.cardProduto}>
              <View style={styles.informacoesProduto}>
                <Text style={styles.nomeProduto}>
                  {item.nome}
                </Text>

                <Text style={styles.precoProduto}>
                  {item.quantidade} x {formatarPreco(item.preco)}
                </Text>
              </View>

              <View style={styles.controlesQuantidade}>
                <TouchableOpacity
                  style={styles.botaoQuantidade}
                  onPress={() => {
                    if (item.quantidade > 1) {
                      onAlterarQuantidade(
                        item.id,
                        item.quantidade - 1
                      );
                    } else {
                      onRemoverProduto(item.id);
                    }
                  }}
                >
                  <Text style={styles.textoQuantidade}>−</Text>
                </TouchableOpacity>

                <Text style={styles.quantidade}>
                  {item.quantidade}
                </Text>

                <TouchableOpacity
                  style={styles.botaoQuantidade}
                  onPress={() =>
                    onAlterarQuantidade(
                      item.id,
                      item.quantidade + 1
                    )
                  }
                >
                  <Text style={styles.textoQuantidade}>+</Text>
                </TouchableOpacity>

                <TouchableOpacity
                  onPress={() => onRemoverProduto(item.id)}
                >
                  <Text style={styles.remover}>Remover</Text>
                </TouchableOpacity>
              </View>
            </View>
          ))}
        </View>

        {/* Total da venda */}
        <View style={styles.cardTotal}>
          <Text style={styles.rotuloTotal}>
            Total da venda
          </Text>

          <Text style={styles.valorTotal}>
            {formatarPreco(valorTotal)}
          </Text>

          <Text style={styles.descricaoTotal}>
            {itensVenda.length} produtos na venda
          </Text>
        </View>

        {/* Recebimento */}
        <View style={styles.cardRecebimento}>
          <Text style={styles.tituloRecebimento}>
            Recebimento
          </Text>

          <Text style={styles.subtituloRecebimento}>
            Selecione a forma de pagamento.
          </Text>

          <View style={styles.opcoesPagamento}>
            {formasPagamento.map((forma) => {
              const selecionada = formaPagamento === forma;

              return (
                <TouchableOpacity
                  key={forma}
                  style={[
                    styles.opcaoPagamento,
                    selecionada && styles.opcaoSelecionada,
                  ]}
                  onPress={() => setFormaPagamento(forma)}
                >
                  <Text
                    style={[
                      styles.textoOpcao,
                      selecionada && styles.textoOpcaoSelecionada,
                    ]}
                  >
                    {forma}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>
        </View>
      </ScrollView>

      {/* Botão de confirmação, sem funcionalidade nesta etapa */}
      <View style={styles.areaBotao}>
        <TouchableOpacity style={styles.botaoConfirmar}>
          <Text style={styles.textoBotaoConfirmar}>
            Confirmar venda
          </Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F3F6',
  },
  conteudo: {
    paddingHorizontal: 20,
    paddingTop: 15,
    paddingBottom: 20,
  },
  titulo: {
    fontSize: 25,
    fontWeight: '600',
    color: '#402044',
    marginTop: 10,
  },
  subtitulo: {
    fontSize: 13,
    color: '#827787',
    marginTop: 5,
    marginBottom: 18,
  },
  listaProdutos: {
    gap: 10,
  },
  cardProduto: {
    backgroundColor: '#F8E9CC',
    borderRadius: 16,
    padding: 14,
    gap: 12,
  },
  informacoesProduto: {
    gap: 6,
  },
  nomeProduto: {
    fontSize: 14,
    fontWeight: '600',
    color: '#402044',
  },
  precoProduto: {
    fontSize: 12,
    color: '#827787',
  },
  controlesQuantidade: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  botaoQuantidade: {
    width: 28,
    height: 28,
    borderRadius: 8,
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
  },
  textoQuantidade: {
    fontSize: 18,
    color: '#063D2E',
    fontWeight: '600',
  },
  quantidade: {
    fontSize: 13,
    fontWeight: '600',
    color: '#402044',
  },
  remover: {
    fontSize: 12,
    color: '#A13C3C',
    marginLeft: 'auto',
  },
  cardTotal: {
    backgroundColor: '#063D2E',
    borderRadius: 17,
    padding: 17,
    marginTop: 18,
  },
  rotuloTotal: {
    fontSize: 12,
    color: '#FFFFFF',
  },
  valorTotal: {
    fontSize: 28,
    fontWeight: '600',
    color: '#FFFFFF',
    marginTop: 8,
  },
  descricaoTotal: {
    fontSize: 11,
    color: '#FFFFFF',
    marginTop: 8,
  },
  cardRecebimento: {
    backgroundColor: '#EFE3F4',
    borderRadius: 16,
    padding: 15,
    marginTop: 12,
  },
  tituloRecebimento: {
    fontSize: 14,
    fontWeight: '600',
    color: '#402044',
  },
  subtituloRecebimento: {
    fontSize: 12,
    color: '#827787',
    marginTop: 5,
    marginBottom: 14,
  },
  opcoesPagamento: {
    flexDirection: 'row',
    gap: 8,
  },
  opcaoPagamento: {
    flex: 1,
    paddingVertical: 11,
    borderRadius: 10,
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'transparent',
  },
  opcaoSelecionada: {
    backgroundColor: '#063D2E',
    borderColor: '#063D2E',
  },
  textoOpcao: {
    fontSize: 12,
    fontWeight: '600',
    color: '#402044',
  },
  textoOpcaoSelecionada: {
    color: '#FFFFFF',
  },
  areaBotao: {
    paddingHorizontal: 20,
    paddingVertical: 12,
    backgroundColor: '#F5F3F6',
  },
  botaoConfirmar: {
    backgroundColor: '#063D2E',
    borderRadius: 13,
    paddingVertical: 14,
    alignItems: 'center',
  },
  textoBotaoConfirmar: {
    fontSize: 13,
    fontWeight: '600',
    color: '#FFFFFF',
  },
});
