import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  SafeAreaView,
} from 'react-native';

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
  const {
    periodo,
    fusoHorario,
    totalVendasDetalhadas,
    dadosSuficientes,
    motivoInsuficiencia,
    combinacoes = [],
  } = analise;

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

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.conteudo}>
        <Header
          onVoltar={onVoltar}
          modoVoltar={true}
          onIAPress={onConversarComIA}
        />

        <Text style={styles.titulo}>Boas combinações</Text>

        <Text style={styles.subtitulo}>
          Sugestões para você avaliar.
        </Text>

        <Text style={styles.contextoAnalise}>
          Período: {periodo} · Fuso: {fusoHorario} · Base:{' '}
          {totalVendasDetalhadas} vendas detalhadas
        </Text>

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
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F4EDF8',
  },
  conteudo: {
    paddingHorizontal: 20,
    paddingTop: 15,
    paddingBottom: 24,
  },
  titulo: {
    fontSize: 32,
    lineHeight: 40,
    fontWeight: '600',
    color: '#341539',
  },
  subtitulo: {
    fontSize: 16,
    fontWeight: '400',
    color: '#6C5E74',
    marginTop: 8,
  },
  contextoAnalise: {
    fontSize: 12,
    fontWeight: '400',
    color: '#6C5E74',
    marginTop: 12,
    marginBottom: 16,
  },
  lista: {
    gap: 12,
    marginBottom: 16,
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
    fontSize: 16,
    fontWeight: '600',
    color: '#341539',
  },
  cardDescricao: {
    fontSize: 14,
    fontWeight: '400',
    color: '#6C5E74',
  },
  cardInformativo: {
    width: '100%',
    backgroundColor: '#F6E7C8',
    borderRadius: 20,
    paddingVertical: 16,
    paddingHorizontal: 20,
    gap: 8,
  },
  cardInformativoTitulo: {
    fontSize: 16,
    fontWeight: '600',
    color: '#341539',
  },
  cardInformativoDescricao: {
    fontSize: 14,
    fontWeight: '400',
    color: '#6C5E74',
  },
  rodape: {
    paddingHorizontal: 20,
    paddingTop: 8,
    paddingBottom: 20,
    backgroundColor: '#F4EDF8',
  },
  botaoIA: {
    width: '100%',
    height: 56,
    borderRadius: 20,
    backgroundColor: '#053225',
    alignItems: 'center',
    justifyContent: 'center',
  },
  botaoIATexto: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
  },
});

