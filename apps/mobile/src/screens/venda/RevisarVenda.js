
import { useState } from 'react';

import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  SafeAreaView,
  StyleSheet,
  TextInput,
} from 'react-native';

import Header from '../../components/Header';

const formasPagamento = ['PIX', 'Crédito', 'Débito'];

function formatarPreco(valor) {
  return `R$ ${valor.toFixed(2).replace('.', ',')}`;
}

export default function RevisarVenda({
  produtos,
  carrinho,
  onVoltar,
  onConfirmarVenda,
  onAbrirTroco,
}) {
  const [formaPagamento, setFormaPagamento] = useState('');
  const [valorRecebido, setValorRecebido] = useState('');

  const dinheiroAtivo = formaPagamento === 'Dinheiro';

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

  // Calcula a quantidade total de unidades.
  const quantidadeTotal = itensVenda.reduce(
    (total, item) => total + item.quantidade,
    0
  );

  // Converte o valor digitado para número.
  const valorNumerico = Number(valorRecebido.replace(',', '.')) || 0;

  // Calcula o troco, sem permitir valores negativos.
  const valorTroco = Math.max(
    valorNumerico - valorTotal,
    0
  );

  // Ativa ou desativa o recebimento em dinheiro.
  function alternarDinheiro() {
    if (dinheiroAtivo) {
      setFormaPagamento('');
      setValorRecebido('');
    } else {
      setFormaPagamento('Dinheiro');
    }
  }

  function formatarEntradaDinheiro(valor) {
    const numeros = valor.replace(/\D/g, '');

    if (!numeros) {
      return '';
    }

    const centavos = Number(numeros);

    return (centavos / 100).toFixed(2).replace('.', ',');
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        contentContainerStyle={styles.conteudo}
        showsVerticalScrollIndicator={false}
      >
        <Header onVoltar={onVoltar} modoVoltar={true} />

        <Text style={styles.titulo}>Revisar venda</Text>

        <Text style={styles.subtitulo}>
          Confira os {quantidadeTotal} itens antes de registrar.
        </Text>

        {/* Produtos da venda */}
        <View style={styles.listaProdutos}>
          {itensVenda.map((item) => (
            <View key={item.id} style={styles.cardProduto}>
              <Text style={styles.nomeProduto}>
                {item.nome}
              </Text>

              <Text style={styles.precoProduto}>
                {item.quantidade} x {formatarPreco(item.preco)}
              </Text>
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
            {quantidadeTotal} unidades na venda
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
                  onPress={() => {
                    setFormaPagamento(forma);
                    setValorRecebido('');
                  }}
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

          {/* Separação visual do dinheiro */}
          <View style={styles.separadorDinheiro} />

          {/* Recebimento em dinheiro */}
          <TouchableOpacity
            style={[
              styles.opcaoDinheiro,
              dinheiroAtivo && styles.opcaoDinheiroAtiva,
            ]}
            onPress={alternarDinheiro}
          >
            <Text
              style={[
                styles.textoDinheiro,
                dinheiroAtivo && styles.textoDinheiroAtivo,
              ]}
            >
              Dinheiro
            </Text>
          </TouchableOpacity>

          {/* Valor recebido */}
          <Text style={styles.rotuloCampo}>
            Recebido
          </Text>

          <TextInput
            style={[
              styles.campoRecebido,
              dinheiroAtivo && styles.campoRecebidoAtivo,
            ]}
            value={dinheiroAtivo ? `R$ ${valorRecebido || '0,00'}` : ''}
            onChangeText={(texto) => {
              setValorRecebido(formatarEntradaDinheiro(texto));
            }}
            placeholder="R$ 0,00"
            placeholderTextColor="#A99EAC"
            keyboardType="numeric"
            editable={dinheiroAtivo}
          />

          {/* Troco */}
          <View style={styles.cabecalhoCampoTroco}>
            <Text style={styles.rotuloCampoTroco}>
              Troco
            </Text>
            {dinheiroAtivo && onAbrirTroco && (
              <TouchableOpacity onPress={() => onAbrirTroco({ valorTotal })}>
                <Text style={styles.linkTroco}>Troco sem complicar ›</Text>
              </TouchableOpacity>
            )}
          </View>

          <TouchableOpacity
            style={styles.campoTroco}
            onPress={() => {
              if (dinheiroAtivo && onAbrirTroco) {
                onAbrirTroco({ valorTotal });
              }
            }}
            activeOpacity={dinheiroAtivo && onAbrirTroco ? 0.7 : 1}
          >
            <Text style={styles.valorTroco}>
              {formatarPreco(dinheiroAtivo ? valorTroco : 0)}
            </Text>
          </TouchableOpacity>
        </View>
      </ScrollView>

      {/* Botão de confirmação, sem funcionalidade nesta etapa */}
      <View style={styles.areaBotao}>
        <TouchableOpacity
          style={styles.botaoConfirmar}
          onPress={() => onConfirmarVenda({ valorTotal })}
        >
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
  separadorDinheiro: {
    height: 1,
    backgroundColor: '#D8C9DC',
    marginTop: 16,
    marginBottom: 2,
  },
  // Botão Dinheiro
  opcaoDinheiro: {
    alignSelf: 'flex-start',
    paddingVertical: 11,
    paddingHorizontal: 22,
    borderRadius: 10,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: 'transparent',
    marginTop: 14,
  },
  opcaoDinheiroAtiva: {
    backgroundColor: '#063D2E',
    borderColor: '#063D2E',
  },
  textoDinheiro: {
    fontSize: 12,
    fontWeight: '600',
    color: '#402044',
  },
  textoDinheiroAtivo: {
    color: '#FFFFFF',
  },

  // Campo Recebido
  rotuloCampo: {
    fontSize: 12,
    fontWeight: '600',
    color: '#402044',
    marginTop: 14,
    marginBottom: 7,
  },
  campoRecebido: {
    backgroundColor: '#E5DCE8',
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 11,
    fontSize: 14,
    color: '#402044',
    borderWidth: 1,
    borderColor: 'transparent',
  },
  campoRecebidoAtivo: {
    backgroundColor: '#FFFFFF',
    borderColor: '#063D2E',
  },

  // Campo Troco
  cabecalhoCampoTroco: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 14,
    marginBottom: 7,
  },
  rotuloCampoTroco: {
    fontSize: 12,
    fontWeight: '600',
    color: '#402044',
  },
  linkTroco: {
    fontSize: 12,
    fontWeight: '600',
    color: '#063D2E',
  },
  campoTroco: {
    backgroundColor: '#E5DCE8',
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 11,
  },
  valorTroco: {
    fontSize: 14,
    fontWeight: '600',
    color: '#063D2E',
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
