# Regras de Negócio

## 1. Autenticação de Usuários

### RN01 - Validar CNPJ

- **Descrição:** O sistema deve verificar e validar o CNPJ do usuário para garantir que o usuário é uma pessoa verificada e não um vendedor ambulante.
- **Origem:**
- **Dados Necessários:** VendedorCNPJ
- **Validação:** Caso o usuário não tenha um CNPJ válido, o aplicativo não deixará nenhuma tela acessível, mantendo-o na tela de autenticação, é mostrado um alerta "CNPJ e/ou SENHA errados".

### RN02 - Validar Senha

- **Descrição:** O sistema deve verificar e validar a senha do usuário para garantir que ninguém tenha adicionado um CNPJ alheio e tentado invadir a conta
- **Origem:**
- **Dados Necessários:** VendedorSenha
- **Validação:** Caso o usuário tenha adicionado a senha errada, o aplicativo não deixará nenhuma tela acessível, mantendo-o na tela de autenticação, é mostrado um alerta "CNPJ e/ou SENHA errados".

### IDEIA PARA VALIDAR CNPJ

#### 1. Cruzamento de Dados (Receita Federal)

- **Como funciona:** O usuário digita o CNPJ e o seu CPF.
- **O que o app faz:** Consome uma API (como Serasa, BigDataCorp ou InfoSimples) para buscar o Quadro de Sócios e Administradores (QSA) da empresa na Receita Federal.
- **A validação:** O sistema só avança se o CPF digitado constar como sócio ou representante legal daquele CNPJ.

#### 2. Prova de Vida e Validação de Identidade (Biometria)

- **Como funciona:** O aplicativo solicita uma foto do documento de identidade (RG ou CNH) do vendedor e uma selfie.
- **O que o app faz:** Envia as imagens para um motor de biometria facial (como Unico Check ou Facetec).
- **A validação:** O sistema faz o Liveness Check (garante que é uma pessoa viva, não uma foto da foto) e valida se o rosto da selfie é o mesmo do documento.

#### 3. Validação do Estabelecimento Físico

- **Como funciona:** O vendedor precisa provar que a loja física existe e que ele está nela.
- **O que o app faz:** Solicita o envio de uma foto da fachada da loja com o vendedor nela, ou um comprovante de endereço comercial (água, luz, internet) recente em nome da empresa.
- **A validação alternativa:** Exige a geolocalização do aparelho no momento do cadastro para bater com o endereço oficial do CNPJ.

#### 4. Validação Bancária (Split de Pagamento)

- **Como funciona:** O vendedor precisa cadastrar uma conta bancária para receber as vendas do app.
- **O que o app faz:** Exige que a conta bancária seja obrigatoriamente uma Conta PJ atrelada àquele CNPJ (ou conta PF do sócio majoritário).
- **A validação:** Gateways de pagamento (como Pagar.me, Asaas ou Zoop) barram automaticamente se os dados bancários não baterem com o documento cadastrado.

## 2. Estoque

### RN03 - Campos Obrigatórios

- **Descrição:** O sistema deve garantir que quando for cadastrado um item, todos os seus campos devem estar preenchidos.
- **Origem:**
- **Dados Necessários:** VerificaçãoCampos
- **Validação:** Caso algum dos campos não tenha nada escrito, o sistema apresenta um alerta "Campo obrigatório" e ressalta em vermelho o campo não preenchido, impedindo do item ser adicionado.

### RN04 - Tipo de Produto Obrigatório

- **Descrição:** O sistema deve garantir que quando um item for ser adicionado, ele precisa ser submetido em um tipo de produto.
- **Origem:**
- **Dados Necessários:** TipoItem = NotNull
- **Validação:** Quando um item for adicionado ao estoque, o usuário deve especificar o tipo de produto que o mesmo é, garantindo a ordem quando filtros forem feitos.

### RN05 - Diferenciador de Produtos

- **Descrição:** O sistema deve permitir itens serem adicionados com ao menos uma diferença, seja nome, tamanho, tipo, entre outros.
- **Origem:**
- **Dados Necessários:** AdicionarItem
- **Validação:** Quando um item for adicionado ao estoque, o sistema deve verificar se existem produtos iguais já cadastrados, caso todos os atributos forem iguais, um alerta é enviado "Já existe produto idêntico no estoque" (Quando um item já estiver no estoque, eu pensei em somente procurá-lo e clicar em um botão de "+" no próprio produto para adicionar mais unidades).

### RN06 - Filtro de Procura

- **Descrição:** O usuário pode filtrar o estoque para achar um item desejado.
- **Origem:**
- **Dados Necessários:** ProcurarItem
- **Validação:** Quando o usuário entrar no estoque, deve existir algumas barras de filtro como nome, tamanho, tipo, entre outros, permitindo o usuário a procurar o item que desejar.

### RN07 - Exclusão de Itens

- **Descrição:** O sistema deve garantir que quando um item for selecionado para ser excluído, o mesmo deve ter sua quantidade igual a "0" no estoque.
- **Origem:**
- **Dados Necessários:** ExcluirItem
- **Validação:** Caso o item que o usuário queira excluir ainda exista no estoque, (minimo de 1 quantidade restante) o sistema emite um alerta "Para excluir um item, não pode existir unidades dele no estoque".

## 3. Vendas

### RN08 - Obrigatoriedade de Venda

- **Descrição:** O sistema deve garantir que quando um item for selecionado para ser vendido, o mesmo necessita conter ao menos uma unidade no estoque.
- **Origem:**
- **Dados Necessários:** ItemQuantidade >= 1
- **Validação:** Caso o item que o usuário queira vender não existir no estoque, o sistema emitirá um alerta "O produto não existe no seu estoque" e não prosseguirá com a venda.

### RN09 - Pagamento

- **Descrição:** Quando uma venda estiver no processo de pagamento, o sistema permitirá o usuário a escolher o tipo de pagamento.
- **Origem:**
- **Dados Necessários:** APIpagamento
- **Validação:** Quando o usuário escolher os itens a serem vendidos e clicar no botão "Vender", o sistema apresentará as formas de pagamento para o mesmo escolher, como Pix, Crédito e Débito.

### RN10 - Conversão de Dinheiro

- **Descrição:** Durante a escolha do tipo de pagamento, o usuário também têm a possibilidade de escolher a moeda da compra.
- **Origem:**
- **Dados Necessários:** APItaxaCambio
- **Validação:** Quando o usuário escolher os itens a serem vendidos e clicar no botão "Vender", o sistema apresentará as moedas disponíveis para efetuar a compra, com suas taxas de câmbio e spread aplicáveis.
