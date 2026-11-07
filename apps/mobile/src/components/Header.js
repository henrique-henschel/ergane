import { View, Text, TouchableOpacity, Image, StyleSheet } from 'react-native';

export default function Header({
  onVoltar,
  onNotificationsPress,
  onIAPress,
  modoVoltar = false,
  usarChevronVoltar = false,
}) {
  return (
    <View style={styles.container}>

      {/* Casa ou seta de voltar */}
      <TouchableOpacity
        style={styles.iconeCircular}
        onPress={onVoltar}
      >
        {modoVoltar ? (
          usarChevronVoltar ? (
            <View style={styles.chevronVoltar} />
          ) : (
            <Image
              source={require('../../assets/retornar.png')}
              style={styles.iconeCasa}
              resizeMode="contain"
            />
          )
        ) : (
          <Image
            source={require('../../assets/lar.png')}
            style={styles.iconeCasa}
            resizeMode="contain"
          />
        )}
      </TouchableOpacity>

      {/* Notificações e IA */}
      <View style={styles.acoesDireita}>

        <TouchableOpacity
          style={styles.iconeCircular}
          onPress={onNotificationsPress}
        >
          <Image
            source={require('../../assets/notificacao.png')}
            style={styles.iconeNotificacao}
            resizeMode="contain"
          />
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.botaoIA}
          onPress={onIAPress}
        >
          <Image
            source={require('../../assets/brilhar.png')}
            style={styles.iconeIA}
            resizeMode="contain"
          />

          <Text style={styles.textoIA}>IA</Text>
        </TouchableOpacity>

      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: '100%',
    height: 66,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 20,
  },
  iconeCircular: {
    width: 38,
    height: 38,
    borderRadius: 20,
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
  },
  setaVoltar: {
    fontSize: 32,
    color: '#063D2E',
    lineHeight: 36,
    marginTop: -3,
  },
  chevronVoltar: {
    width: 10,
    height: 10,
    borderLeftWidth: 2,
    borderBottomWidth: 2,
    borderColor: '#053225',
    transform: [{ rotate: '45deg' }],
    marginLeft: 3,
  },
  acoesDireita: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 14,
  },
  iconeCasa: {
    width: 22,
    height: 22,
  },
  iconeNotificacao: {
    width: 22,
    height: 22,
  },
  botaoIA: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  iconeIA: {
    width: 20,
    height: 20,
  },
  textoIA: {
    fontSize: 12,
    color: '#402044',
  },
});
