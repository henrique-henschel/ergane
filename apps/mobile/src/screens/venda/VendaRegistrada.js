
import {
  View,
  Text,
  TouchableOpacity,
  SafeAreaView,
  StyleSheet,
} from 'react-native';

import Header from '../../components/Header';
import BottomNavigation from '../../components/BottomNavigation';

function formatarPreco(valor) {
  return `R$ ${valor.toFixed(2).replace('.', ',')}`;
}

export default function VendaRegistrada({
  valorTotal,
  registro,
  onNovaVenda,
  onAbrirHistorico,
  onNavegar,
}) {
  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.conteudo}>
        <Header />

        <Text style={styles.titulo}>
          Venda registrada!
        </Text>

        <Text style={styles.subtitulo}>
          Mais um passo no seu dia.
        </Text>

        {/* Ícone de sucesso */}
        <View style={styles.areaSucesso}>
          <View style={styles.iconeSucesso}>
            <Text style={styles.check}>
              ✓
            </Text>
          </View>

          <Text style={styles.textoSucesso}>
            ✓ Tudo certo
          </Text>
        </View>

        {/* Valor da venda */}
        <Text style={styles.valorTotal}>
          {formatarPreco(valorTotal)}
        </Text>

        <Text style={styles.descricaoPagamento}>
          Recebimento ainda não informado.
        </Text>

        {/* Número do registro */}
        <View style={styles.areaRegistro}>
          <Text style={styles.registro}>
            Registro #{registro} ›
          </Text>

          <Text style={styles.descricaoRegistro}>
            Você pode consultar ou corrigir no histórico.
          </Text>
        </View>

        <View style={styles.botoes}>
          <TouchableOpacity
            style={styles.botaoNovaVenda}
            onPress={onNovaVenda}
          >
            <Text style={styles.textoBotaoNovaVenda}>
              Nova venda
            </Text>
          </TouchableOpacity>

          <TouchableOpacity 
            style={styles.botaoHistorico}
            onPress={onAbrirHistorico}
          >
            <Text style={styles.textoBotaoHistorico}>
              Ver histórico
            </Text>
          </TouchableOpacity>
        </View>
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
    backgroundColor: '#063D2E',
  },

  conteudo: {
    flex: 1,
    paddingHorizontal: 20,
    paddingTop: 15,
  },

  titulo: {
    fontSize: 25,
    fontWeight: '600',
    color: '#FFFFFF',
    marginTop: 15,
  },

  subtitulo: {
    fontSize: 13,
    color: '#FFFFFF',
    marginTop: 5,
  },

  areaSucesso: {
    alignItems: 'center',
    marginTop: 28,
  },

  iconeSucesso: {
    width: 112,
    height: 112,
    borderRadius: 25,
    backgroundColor: '#39C900',
    alignItems: 'center',
    justifyContent: 'center',
  },

  check: {
    fontSize: 76,
    fontWeight: '500',
    color: '#063D2E',
    lineHeight: 85,
  },

  textoSucesso: {
    fontSize: 12,
    color: '#FFFFFF',
    marginTop: 28,
  },

  valorTotal: {
    fontSize: 32,
    fontWeight: '600',
    color: '#FFFFFF',
    textAlign: 'center',
    marginTop: 8,
  },

  descricaoPagamento: {
    fontSize: 11,
    color: '#FFFFFF',
    textAlign: 'center',
    marginTop: 8,
  },

  areaRegistro: {
    marginTop: 44,
  },

  registro: {
    fontSize: 13,
    fontWeight: '600',
    color: '#FFFFFF',
  },

  descricaoRegistro: {
    fontSize: 12,
    color: '#FFFFFF',
    marginTop: 8,
  },

  botoes: {
    marginTop: 'auto',
    marginBottom: 15,
    gap: 10,
  },

  botaoNovaVenda: {
    backgroundColor: '#39C900',
    borderRadius: 30,
    paddingVertical: 14,
    alignItems: 'center',
  },

  textoBotaoNovaVenda: {
    fontSize: 13,
    fontWeight: '600',
    color: '#063D2E',
  },

  botaoHistorico: {
    backgroundColor: '#FFFFFF',
    borderRadius: 30,
    paddingVertical: 14,
    alignItems: 'center',
  },

  textoBotaoHistorico: {
    fontSize: 13,
    fontWeight: '600',
    color: '#063D2E',
  },
});
