
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  SafeAreaView,
} from 'react-native';

import Header from '../../../components/Header';
import BottomNavigation from '../../../components/BottomNavigation';

const vendasMockadas = [
  {
    id: 1042,
    horario: '10:32',
    data: '26 out',
    responsavel: 'Rosa',
    valorTotal: 8.00,
    status: 'Recebimento pendente',
    produtos: [
      {
        id: 'prod-001',
        nome: 'Pastel de carne',
        quantidade: 1,
        preco: 8.00,
      },
    ],
  },
  {
    id: 1041,
    horario: '10:10',
    data: '26 out',
    responsavel: 'Rosa',
    valorTotal: 24.00,
    status: 'Recebido',
    produtos: [
      {
        id: 'prod-008',
        nome: 'Coxinha de frango',
        quantidade: 4,
        preco: 6.00,
      },
    ],
  },
  {
    id: 1040,
    horario: '09:48',
    data: '26 out',
    responsavel: 'Rosa',
    valorTotal: 50.00,
    status: 'Recebido',
    produtos: [
      {
        id: 'prod-004',
        nome: 'Água mineral',
        quantidade: 20,
        preco: 2.50,
      },
    ],
  },
  {
    id: 1039,
    horario: '09:15',
    data: '26 out',
    responsavel: 'Rosa',
    valorTotal: 16.50,
    status: 'Recebimento pendente',
    produtos: [
      {
        id: 'prod-003',
        nome: 'Brigadeiro',
        quantidade: 3,
        preco: 3.50,
      },
      {
        id: 'prod-010',
        nome: 'Beijinho',
        quantidade: 2,
        preco: 3.00,
      },
    ],
  },
];

export default function Historico({
  onVoltar,
  onAbrirFiltros,
  onSelecionarVenda,
  onNavegar,
}) {
  function formatarPreco(valor) {
    return valor.toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    });
  }

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <Header onVoltar={onVoltar} />

        <Text style={styles.titulo}>Suas vendas</Text>

        <Text style={styles.subtitulo}>
          Veja o que aconteceu, sem perder o contexto.
        </Text>

        <ScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.lista}
        >
          <TouchableOpacity
            style={styles.cardFiltros}
            onPress={onAbrirFiltros}
            activeOpacity={0.8}
          >
            <View>
              <Text style={styles.cardFiltrosTitulo}>
                Período ›
              </Text>

              <Text style={styles.cardFiltrosDescricao}>
                Hoje · Todos os registros
              </Text>
            </View>
          </TouchableOpacity>

          <View style={styles.listaVendas}>
            {vendasMockadas.map((venda) => (
              <TouchableOpacity
                key={venda.id}
                style={styles.cardVenda}
                activeOpacity={0.7}
                onPress={() => onSelecionarVenda(venda)}
              >
                <Text style={styles.numeroVenda}>
                  Venda #{venda.id} · {venda.horario} ›
                </Text>

                <View style={styles.linhaInferior}>
                  <Text style={styles.valorVenda}>
                    {formatarPreco(venda.valorTotal)}
                  </Text>

                  <Text style={styles.separador}>·</Text>

                  <Text
                    style={[
                      styles.statusVenda,
                      venda.status === 'Recebido'
                        ? styles.statusRecebido
                        : styles.statusPendente,
                    ]}
                  >
                    {venda.status}
                  </Text>
                </View>
              </TouchableOpacity>
            ))}
          </View>
        </ScrollView>
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
    backgroundColor: '#F5F3F6',
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
    paddingTop: 20,
    paddingBottom: 20,
  },
  cardFiltros: {
    backgroundColor: '#E5F0E5',
    padding: 15,
    borderRadius: 17,
    minHeight: 70,
    justifyContent: 'center',
  },
  cardFiltrosTitulo: {
    fontSize: 14,
    fontWeight: '600',
    color: '#402044',
  },
  cardFiltrosDescricao: {
    fontSize: 12,
    color: '#6B5D6D',
    marginTop: 6,
  },
  listaVendas: {
    marginTop: 10,
  },
  cardVenda: {
    paddingVertical: 19,
    borderBottomWidth: 1,
    borderBottomColor: '#E9E1EB',
  },
  numeroVenda: {
    fontSize: 13,
    fontWeight: '600',
    color: '#402044',
    marginBottom: 7,
  },
  linhaInferior: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
  },
  valorVenda: {
    fontSize: 12,
    color: '#57465A',
  },
  separador: {
    fontSize: 12,
    color: '#57465A',
  },
  statusVenda: {
    fontSize: 12,
  },
  statusRecebido: {
    color: '#28664B',
  },
  statusPendente: {
    color: '#80632B',
  },
});
