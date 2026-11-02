import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  SafeAreaView,
} from 'react-native';

import Header from '../components/Header';

import BottomNavigation from '../components/BottomNavigation';

// Tela temporaria apenas para testar o acesso a UC30 no Expo enquanto a V3/065 nao existe.
export default function EntradaScreen({
  onAbrirCombinacoes,
  onVoltarInicio,
  onNavegar,
}) {
  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <Header onVoltar={onVoltarInicio} />

        <Text style={styles.titulo}>
          Acesso de desenvolvimento
        </Text>

        <View style={styles.grid}>
          <TouchableOpacity
            style={styles.card}
            onPress={onAbrirCombinacoes}
          >
            <Text style={styles.cardTitulo}>
              Produtos que combinam ›
            </Text>

            <Text style={styles.cardDescricao}>
              Descubra combinações com dados suficientes.
            </Text>
          </TouchableOpacity>

          <View style={styles.colunaReserva} />
        </View>
      </View>

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
    backgroundColor: '#F4EDF8',
  },
  content: {
    flex: 1,
    paddingHorizontal: 20,
    paddingTop: 15,
  },
  titulo: {
    fontSize: 32,
    lineHeight: 40,
    fontWeight: '600',
    color: '#341539',
  },
  grid: {
    flexDirection: 'row',
    gap: 12,
    marginTop: 24,
  },
  card: {
    flex: 1,
    backgroundColor: '#EEE3F5',
    borderRadius: 20,
    paddingVertical: 16,
    paddingHorizontal: 20,
    minHeight: 120,
    justifyContent: 'space-between',
    gap: 8,
  },
  colunaReserva: {
    flex: 1,
  },
  cardTitulo: {
    fontSize: 16,
    fontWeight: '600',
    color: '#341539',
  },
  cardDescricao: {
    fontSize: 14,
    fontWeight: '400',
    color: '#6C5E74',
  },
});

