import { useEffect, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  SafeAreaView,
  Animated,
  Easing,
} from 'react-native';
import {
  useFonts,
  PlusJakartaSans_400Regular,
  PlusJakartaSans_500Medium,
  PlusJakartaSans_600SemiBold,
} from '@expo-google-fonts/plus-jakarta-sans';

import Header from '../components/Header';

// Dados ficticios de demonstracao para visualizacao da interface (UC30).
// Nao representam resultados reais nem decisao definitiva de D02/D05/D08/D09.
const ANALISE_DEMONSTRACAO = {
  periodo: 'Últimos 30 dias (demonstração)',
  fusoHorario: 'America/Sao_Paulo (demonstração)',
  totalVendasDetalhadas: 40,
  dadosSuficientes: true,
  motivoInsuficiencia:
    'Ainda não há vendas detalhadas suficientes no período para sugerir combinações com segurança.',
  combinacoes: [
    {
      id: 'comb-demo-001',
      titulo: 'Pastel + suco',
      ocorrencias: 12,
      base: 40,
    },
  ],
};

export default function CombinacoesScreen({
  analise = ANALISE_DEMONSTRACAO,
  onVoltar,
  onConversarComIA,
}) {
  const [fontesCarregadas] = useFonts({
    PlusJakartaSans_400Regular,
    PlusJakartaSans_500Medium,
    PlusJakartaSans_600SemiBold,
  });

  const opacidadeEntrada = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    if (fontesCarregadas) {
      Animated.timing(opacidadeEntrada, {
        toValue: 1,
        duration: 200,
        easing: Easing.out(Easing.ease),
        useNativeDriver: true,
      }).start();
    }
  }, [fontesCarregadas, opacidadeEntrada]);

  const {
    periodo,
    fusoHorario,
    totalVendasDetalhadas,
    dadosSuficientes,
    motivoInsuficiencia,
    combinacoes = [],
  } = analise;

  function formatarContextoConsulta(periodoValor, fusoValor) {
    const periodoLimpo = (periodoValor || '')
      .replace(/\s*\(demonstração\)/gi, '')
      .trim();

    const fusoLimpo = (fusoValor || '')
      .replace(/\s*\(demonstração\)/gi, '')
      .replace('America/Sao_Paulo', 'Horário de Brasília')
      .trim();

    return [periodoLimpo, fusoLimpo].filter(Boolean).join(' · ');
  }

  function formatarDescricaoCombinacao(combinacao) {
    const denominador = combinacao.base ?? totalVendasDetalhadas;

    if (denominador > 0) {
      const percentual = Math.round(
        (combinacao.ocorrencias / denominador) * 100
      );

      return `Aparecem juntos em ${combinacao.ocorrencias} das ${denominador} vendas detalhadas do período (${percentual}%).`;
    }

    if (combinacao.descricao) {
      return combinacao.descricao;
    }

    return `Aparecem juntos em ${combinacao.ocorrencias} vendas detalhadas do período.`;
  }

  if (!fontesCarregadas) {
    return <SafeAreaView style={styles.container} />;
  }

  return (
    <SafeAreaView style={styles.container}>
      <Animated.View
        style={[
          styles.conteudoAnimado,
          { opacity: opacidadeEntrada },
        ]}
      >
        <ScrollView
          style={styles.areaRolagem}
          contentContainerStyle={styles.conteudo}
          showsVerticalScrollIndicator={false}
        >
          <View style={styles.cabecalhoContainer}>
            <Header
              onVoltar={onVoltar}
              modoVoltar={true}
              usarChevronVoltar={true}
              onIAPress={onConversarComIA}
            />
          </View>

          <View style={styles.blocoTitulo}>
            <Text style={styles.titulo}>Boas combinações</Text>

            <Text style={styles.subtitulo}>
              Sugestões para você avaliar.
            </Text>

            <Text style={styles.contextoAnalise}>
              {formatarContextoConsulta(periodo, fusoHorario)}
            </Text>
          </View>

          <View style={styles.containerCards}>
            <View style={styles.lista}>
              {!dadosSuficientes ? (
                <View style={styles.cardEstado}>
                  <Text style={styles.cardTitulo}>
                    Dados insuficientes no período
                  </Text>

                  <Text style={styles.cardDescricao}>
                    {motivoInsuficiencia ||
                      'Ainda não há vendas detalhadas suficientes no período para identificar combinações.'}
                  </Text>
                </View>
              ) : combinacoes.length > 0 ? (
                combinacoes.map((combinacao) => (
                  <View
                    key={combinacao.id}
                    style={styles.cardCombinacao}
                  >
                    <Text style={styles.cardTitulo}>
                      {combinacao.titulo}
                    </Text>

                    <Text style={styles.cardDescricao}>
                      {formatarDescricaoCombinacao(combinacao)}
                    </Text>
                  </View>
                ))
              ) : (
                <View style={styles.cardEstado}>
                  <Text style={styles.cardTitulo}>
                    Nenhuma combinação observada
                  </Text>

                  <Text style={styles.cardDescricao}>
                    Não foram encontrados produtos vendidos juntos nas{' '}
                    {totalVendasDetalhadas} vendas detalhadas analisadas.
                  </Text>
                </View>
              )}
            </View>

            <View style={styles.cardInformativo}>
              <Text style={styles.cardInformativoTitulo}>
                Teste no seu ritmo
              </Text>

              <Text style={styles.cardInformativoDescricao}>
                A associação não garante demanda nem define desconto.
              </Text>
            </View>
          </View>
        </ScrollView>

        <View style={styles.rodape}>
          <TouchableOpacity
            style={styles.botaoIA}
            onPress={onConversarComIA}
            disabled={!onConversarComIA}
            activeOpacity={0.85}
          >
            <Text style={styles.botaoIATexto}>
              Conversar com a IA
            </Text>
          </TouchableOpacity>
        </View>
      </Animated.View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F4EDF8',
  },
  conteudoAnimado: {
    flex: 1,
  },
  areaRolagem: {
    flex: 1,
  },
  conteudo: {
    flexGrow: 1,
  },
  cabecalhoContainer: {
    paddingHorizontal: 24,
    paddingTop: 15,
  },
  blocoTitulo: {
    paddingTop: 12,
    paddingHorizontal: 24,
    paddingBottom: 24,
    gap: 8,
  },
  titulo: {
    fontFamily: 'PlusJakartaSans_600SemiBold',
    fontSize: 32,
    lineHeight: 40,
    fontWeight: '600',
    color: '#341539',
  },
  subtitulo: {
    fontFamily: 'PlusJakartaSans_400Regular',
    fontSize: 16,
    lineHeight: 24,
    fontWeight: '400',
    color: '#341539',
  },
  contextoAnalise: {
    fontFamily: 'PlusJakartaSans_400Regular',
    fontSize: 12,
    lineHeight: 16,
    fontWeight: '400',
    color: '#655C69',
    marginTop: 2,
  },
  containerCards: {
    width: '100%',
    paddingHorizontal: 24,
    paddingBottom: 24,
    gap: 16,
  },
  lista: {
    width: '100%',
    gap: 16,
  },
  cardCombinacao: {
    width: '100%',
    minHeight: 104,
    borderRadius: 20,
    paddingVertical: 16,
    paddingHorizontal: 20,
    backgroundColor: '#EEE3F5',
    justifyContent: 'center',
    gap: 8,
  },
  cardEstado: {
    width: '100%',
    minHeight: 104,
    borderRadius: 20,
    paddingVertical: 16,
    paddingHorizontal: 20,
    backgroundColor: '#EEE3F5',
    justifyContent: 'center',
    gap: 8,
  },
  cardTitulo: {
    fontFamily: 'PlusJakartaSans_600SemiBold',
    fontSize: 16,
    lineHeight: 24,
    fontWeight: '600',
    color: '#341539',
  },
  cardDescricao: {
    fontFamily: 'PlusJakartaSans_500Medium',
    fontSize: 14,
    lineHeight: 20,
    fontWeight: '500',
    color: '#655C69',
  },
  cardInformativo: {
    width: '100%',
    minHeight: 104,
    backgroundColor: '#F8E8CE',
    borderRadius: 20,
    paddingVertical: 16,
    paddingHorizontal: 20,
    justifyContent: 'center',
    gap: 8,
  },
  cardInformativoTitulo: {
    fontFamily: 'PlusJakartaSans_600SemiBold',
    fontSize: 16,
    lineHeight: 24,
    fontWeight: '600',
    color: '#341539',
  },
  cardInformativoDescricao: {
    fontFamily: 'PlusJakartaSans_500Medium',
    fontSize: 14,
    lineHeight: 20,
    fontWeight: '500',
    color: '#655C69',
  },
  rodape: {
    paddingVertical: 16,
    paddingHorizontal: 24,
    minHeight: 88,
    justifyContent: 'center',
    backgroundColor: '#F4EDF8',
  },
  botaoIA: {
    width: '100%',
    height: 56,
    borderRadius: 20,
    backgroundColor: '#053225',
    paddingHorizontal: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  botaoIATexto: {
    fontFamily: 'PlusJakartaSans_600SemiBold',
    color: '#FFFFFF',
    fontSize: 16,
    lineHeight: 24,
    fontWeight: '600',
    textAlign: 'center',
  },
});
