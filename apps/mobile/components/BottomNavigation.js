
import {
  View,
  Text,
  TouchableOpacity,
  Image,
  StyleSheet,
} from 'react-native';

const itens = [
  {
    nome: 'Início',
    icone: require('../assets/lar.png'),
    tela: 'inicio',
  },
  {
    nome: 'Pedidos',
    icone: require('../assets/arquivo.png'),
    tela: 'pedidos',
  },
  {
    nome: 'Vender',
    icone: require('../assets/saco-de-papel.png'),
    tela: 'vender',
  },
  {
    nome: 'Gestão',
    icone: require('../assets/caixa.png'),
    tela: 'gestao',
  },
  {
    nome: 'Mais',
    icone: require('../assets/pontos.png'),
    tela: 'mais',
  },
];

export default function BottomNavigation({
  telaAtiva,
  onNavegar,
}) {
  return (
    <View style={styles.container}>
      {itens.map((item) => {
        const ativo = telaAtiva === item.tela;

        return (
          <TouchableOpacity
            key={item.tela}
            style={[
              styles.item,
              ativo && styles.itemAtivo,
            ]}
            onPress={() => onNavegar(item.tela)}
          >
            <Image
              source={item.icone}
              style={styles.icone}
              resizeMode="contain"
            />

            <Text
              style={[
                styles.texto,
                ativo && styles.textoAtivo,
              ]}
            >
              {item.nome}
            </Text>
          </TouchableOpacity>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    height: 72,
    backgroundColor: '#FFFFFF',
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
    paddingHorizontal: 5,
  },

  item: {
    flex: 1,
    height: 58,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 3,
  },

  itemAtivo: {
    backgroundColor: '#E5F0E5',
    borderRadius: 18,
  },

  icone: {
    width: 23,
    height: 23,
  },

  texto: {
    fontSize: 10,
    color: '#063D2E',
  },

  textoAtivo: {
    fontWeight: '600',
  },
});
