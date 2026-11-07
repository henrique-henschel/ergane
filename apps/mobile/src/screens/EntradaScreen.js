import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  SafeAreaView,
} from 'react-native';
import {
  useFonts,
  PlusJakartaSans_400Regular,
  PlusJakartaSans_500Medium,
  PlusJakartaSans_600SemiBold,
} from '@expo-google-fonts/plus-jakarta-sans';

import Header from '../components/Header';
import BottomNavigation from '../components/BottomNavigation';

// Tela temporaria apenas para testar o acesso a UC30 no Expo enquanto a V3/065 nao existe.
export default function EntradaScreen({
  onAbrirCombinacoes,
  onVoltarInicio,
  onNavegar,
}) {
  const [fontesCarregadas] = useFonts({
    PlusJakartaSans_400Regular,
    PlusJakartaSans_500Medium,
    PlusJakartaSans_600SemiBold,
  });

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <Header onVoltar={onVoltarInicio} />

        <Text style={styles.titulo}>
          Acesso de desenvolvimento
        </Text>

        {fontesCarregadas && (
          <View style={styles.areaCard}>
            <TouchableOpacity
              style={styles.card}
              onPress={onAbrirCombinacoes}
              activeOpacity={0.85}
            >
              <Text style={styles.cardTitulo}>
                {'Produtos que\ncombinam ›'}
              </Text>

              <Text style={styles.cardDescricao}>
                Descubra combinações com dados suficientes.
              </Text>
            </TouchableOpacity>
          </View>
        )}
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
  areaCard: {
    marginTop: 24,
    alignItems: 'flex-start',
  },
  card: {
    width: 176,
    maxWidth: '100%',
    minHeight: 148,
    flexDirection: 'column',
    padding: 16,
    gap: 8,
    borderRadius: 36,
    backgroundColor: '#EEE3F5',
    opacity: 1,
    justifyContent: 'flex-start',
  },
  cardTitulo: {
    width: 144,
    maxWidth: '100%',
    minHeight: 48,
    fontFamily: 'PlusJakartaSans_600SemiBold',
    fontWeight: '600',
    fontSize: 16,
    lineHeight: 24,
    color: '#341539',
  },
  cardDescricao: {
    width: 144,
    maxWidth: '100%',
    minHeight: 60,
    fontFamily: 'PlusJakartaSans_500Medium',
    fontWeight: '500',
    fontSize: 14,
    lineHeight: 20,
    color: '#655C69',
  },
});
