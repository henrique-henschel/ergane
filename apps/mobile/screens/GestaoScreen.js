
import { View, Text, StyleSheet, TouchableOpacity, Image, SafeAreaView } from 'react-native';

import Header from '../components/Header';

import BottomNavigation from '../components/BottomNavigation';

export default function GestaoScreen({ onAbrirCatalogo, onVoltarInicio }) {
  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <Header onVoltar={onVoltarInicio} />

        <Text style={styles.titulo}>Tudo organizado</Text>

        <Text style={styles.subtitulo}>
          O que você precisa, em um só lugar.
        </Text>

        <View style={styles.lista}>
          <TouchableOpacity style={styles.card}>
            <Text style={styles.cardTitulo}>Vendas ›</Text>
            <Text style={styles.cardDescricao}>
              Histórico, correções e conferência
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.card, styles.cardDestaque]}
            onPress={onAbrirCatalogo}
          >
            <Text style={styles.cardTitulo}>
              Produtos e estoque ›
            </Text>
            <Text style={styles.cardDescricao}>
              Seu catálogo e disponibilidade
            </Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.card}>
            <Text style={styles.cardTitulo}>Financeiro ›</Text>
            <Text style={styles.cardDescricao}>
              Recebimentos e valores pendentes
            </Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.card}>
            <Text style={styles.cardTitulo}>Resultados ›</Text>
            <Text style={styles.cardDescricao}>
              Entenda os números do negócio
            </Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.card}>
            <Text style={styles.cardTitulo}>
              Metas e planejamento ›
            </Text>
            <Text style={styles.cardDescricao}>
              Um passo de cada vez
            </Text>
          </TouchableOpacity>
        </View>
      </View>

      <BottomNavigation telaAtiva="gestao" />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#EFE2F5',
  },
  content: {
    flex: 1,
    paddingHorizontal: 20,
    paddingTop: 15,
  },
  titulo: {
    fontSize: 27,
    fontWeight: '600',
    color: '#402044',
  },
  subtitulo: {
    fontSize: 14,
    color: '#57465A',
    marginTop: 5,
  },
  lista: {
    marginTop: 20,
    gap: 10,
  },
  card: {
    backgroundColor: '#E5F0E5',
    padding: 15,
    borderRadius: 17,
    minHeight: 70,
    justifyContent: 'center',
  },
  cardDestaque: {
    backgroundColor: '#F8E9CC',
  },
  cardTitulo: {
    fontSize: 14,
    fontWeight: '600',
    color: '#402044',
  },
  cardDescricao: {
    fontSize: 12,
    color: '#6B5D6D',
    marginTop: 6,
  },
  
});
