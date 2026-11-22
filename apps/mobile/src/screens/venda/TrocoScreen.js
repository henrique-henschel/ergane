import { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import {
  useFonts,
  PlusJakartaSans_400Regular,
  PlusJakartaSans_500Medium,
  PlusJakartaSans_600SemiBold,
  PlusJakartaSans_700Bold,
} from '@expo-google-fonts/plus-jakarta-sans';

import Header from '../../components/Header';

function formatarPreco(valor) {
  const num = Number(valor) || 0;
  return `R$ ${num.toFixed(2).replace('.', ',')}`;
}

export default function TrocoScreen({
  valorDevido = 8.0,
  valorEntregueInicial = 10.0,
  onVoltar,
  onUsarRecebimento,
  onNotificationsPress,
  onIAPress,
}) {
  const [fontesCarregadas] = useFonts({
    PlusJakartaSans_400Regular,
    PlusJakartaSans_500Medium,
    PlusJakartaSans_600SemiBold,
    PlusJakartaSans_700Bold,
  });

  const [valorEntregue, setValorEntregue] = useState(
    typeof valorEntregueInicial === 'number'
      ? valorEntregueInicial.toFixed(2).replace('.', ',')
      : String(valorEntregueInicial || '10,00')
  );

  function formatarEntradaDinheiro(valor) {
    const numeros = valor.replace(/\D/g, '');

    if (!numeros) {
      return '';
    }

    const centavos = Number(numeros);
    return (centavos / 100).toFixed(2).replace('.', ',');
  }

  const valorDevidoNum = Number(valorDevido) || 0;
  const valorEntregueNum = valorEntregue
    ? Number(valorEntregue.replace(',', '.'))
    : 0;

  const troco = Math.max(valorEntregueNum - valorDevidoNum, 0);
  const falta = Math.max(valorDevidoNum - valorEntregueNum, 0);
  const isInsuficiente = valorEntregueNum > 0 && valorEntregueNum < valorDevidoNum;
  const isExato = valorEntregueNum === valorDevidoNum;

  function handleConfirmar() {
    if (onUsarRecebimento) {
      onUsarRecebimento({
        valorRecebido: valorDevidoNum,
        valorEntregue: valorEntregueNum,
        troco: isInsuficiente ? 0 : troco,
      });
    }
  }

  const fontRegular = fontesCarregadas ? 'PlusJakartaSans_400Regular' : undefined;
  const fontMedium = fontesCarregadas ? 'PlusJakartaSans_500Medium' : undefined;
  const fontSemiBold = fontesCarregadas ? 'PlusJakartaSans_600SemiBold' : undefined;
  const fontBold = fontesCarregadas ? 'PlusJakartaSans_700Bold' : undefined;

  return (
    <SafeAreaView style={styles.container}>
      <KeyboardAvoidingView
        style={styles.keyboardContainer}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        <ScrollView
          contentContainerStyle={styles.conteudo}
          showsVerticalScrollIndicator={false}
          keyboardShouldPersistTaps="handled"
        >
          {/* Cabeçalho padrão Ergane */}
          <Header
            onVoltar={onVoltar}
            modoVoltar={true}
            usarChevronVoltar={true}
            onNotificationsPress={onNotificationsPress}
            onIAPress={onIAPress}
          />

          {/* Título e Subtítulo */}
          <Text
            style={[
              styles.titulo,
              fontSemiBold ? { fontFamily: fontSemiBold } : null,
            ]}
          >
            Troco sem complicar
          </Text>
          <Text
            style={[
              styles.subtitulo,
              fontRegular ? { fontFamily: fontRegular } : null,
            ]}
          >
            Ajuda para conferir o dinheiro entregue.
          </Text>

          {/* Card 1: Valor entregue pelo cliente */}
          <View style={styles.cardEntregue}>
            <Text
              style={[
                styles.rotuloCardEntregue,
                fontRegular ? { fontFamily: fontRegular } : null,
              ]}
            >
              Valor entregue pelo cliente
            </Text>
            <View style={styles.inputWrapper}>
              <TextInput
                style={[
                  styles.inputEntregue,
                  fontSemiBold ? { fontFamily: fontSemiBold } : null,
                ]}
                value={`R$ ${valorEntregue || '0,00'}`}
                onChangeText={(texto) => {
                  setValorEntregue(formatarEntradaDinheiro(texto));
                }}
                keyboardType="numeric"
                selectTextOnFocus
              />
            </View>
          </View>

          {/* Card 2: Troco a devolver */}
          <View style={styles.cardTroco}>
            <Text
              style={[
                styles.rotuloCardTroco,
                fontSemiBold ? { fontFamily: fontSemiBold } : null,
              ]}
            >
              Troco a devolver
            </Text>
            <Text
              style={[
                styles.valorCardTroco,
                fontRegular ? { fontFamily: fontRegular } : null,
              ]}
            >
              {isInsuficiente
                ? `Falta ${formatarPreco(falta)}`
                : isExato
                ? 'R$ 0,00'
                : formatarPreco(troco)}
            </Text>
          </View>

          {/* Card 3: Valor recebido na venda */}
          <View style={styles.cardRecebido}>
            <Text
              style={[
                styles.rotuloCardRecebido,
                fontSemiBold ? { fontFamily: fontSemiBold } : null,
              ]}
            >
              Valor recebido na venda
            </Text>
            <Text
              style={[
                styles.descricaoCardRecebido,
                fontRegular ? { fontFamily: fontRegular } : null,
              ]}
            >
              {formatarPreco(valorDevidoNum)}. O troco não aumenta o recebimento.
            </Text>
          </View>
        </ScrollView>

        {/* Botão de ação fixado na base */}
        <View style={styles.areaBotao}>
          <TouchableOpacity
            style={styles.botaoConfirmar}
            onPress={handleConfirmar}
            activeOpacity={0.85}
          >
            <Text
              style={[
                styles.textoBotaoConfirmar,
                fontSemiBold ? { fontFamily: fontSemiBold } : null,
              ]}
            >
              Usar recebimento de {formatarPreco(valorDevidoNum)}
            </Text>
          </TouchableOpacity>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F3F6',
  },
  keyboardContainer: {
    flex: 1,
  },
  conteudo: {
    paddingHorizontal: 20,
    paddingTop: 15,
    paddingBottom: 20,
  },
  titulo: {
    fontSize: 26,
    fontWeight: '600',
    color: '#402044',
    marginTop: 6,
  },
  subtitulo: {
    fontSize: 13,
    color: '#827787',
    marginTop: 5,
    marginBottom: 20,
  },
  cardEntregue: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    paddingHorizontal: 16,
    paddingVertical: 14,
    marginBottom: 12,
  },
  rotuloCardEntregue: {
    fontSize: 12,
    color: '#827787',
    marginBottom: 5,
  },
  inputWrapper: {
    justifyContent: 'center',
  },
  inputEntregue: {
    fontSize: 15,
    fontWeight: '600',
    color: '#402044',
    paddingVertical: 0,
  },
  cardTroco: {
    backgroundColor: '#EFE3F4',
    borderRadius: 16,
    paddingHorizontal: 16,
    paddingVertical: 14,
    marginBottom: 12,
  },
  rotuloCardTroco: {
    fontSize: 13,
    fontWeight: '600',
    color: '#402044',
    marginBottom: 5,
  },
  valorCardTroco: {
    fontSize: 12,
    color: '#827787',
  },
  cardRecebido: {
    backgroundColor: '#E4EFE7',
    borderRadius: 16,
    paddingHorizontal: 16,
    paddingVertical: 14,
    marginBottom: 12,
  },
  rotuloCardRecebido: {
    fontSize: 13,
    fontWeight: '600',
    color: '#063D2E',
    marginBottom: 5,
  },
  descricaoCardRecebido: {
    fontSize: 12,
    color: '#557264',
  },
  areaBotao: {
    paddingHorizontal: 20,
    paddingVertical: 16,
    backgroundColor: '#F5F3F6',
  },
  botaoConfirmar: {
    backgroundColor: '#063D2E',
    borderRadius: 14,
    paddingVertical: 15,
    alignItems: 'center',
    justifyContent: 'center',
  },
  textoBotaoConfirmar: {
    fontSize: 13,
    fontWeight: '600',
    color: '#FFFFFF',
  },
});
