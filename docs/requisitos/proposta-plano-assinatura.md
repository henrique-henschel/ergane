# Proposta resumida de assinatura — Ergane

**Status:** proposta para validação; ainda não aprovada nem implementada.

## Planos sugeridos

| Recurso | Ergane Dia a Dia — gratuito | Ergane Avança — R$ 28,90/mês ou R$ 289/ano |
|---|---|---|
| Produtos e categorias | Sim | Incluído |
| Vendas, encomendas e recebimentos informados | Sim | Incluído |
| Histórico e resumo básico do negócio (UC25) | Sim | Incluído |
| Registro de vendas offline e histórico local, conforme cobertura definida | Sim | Incluído |
| Análises de produtos e horários (UC26) | — | Sim, após aprovação de D05/D08 |
| Exportações PDF/CSV (UC27) | — | Sim |
| Sugestões, metas e alertas (UC28–UC32) | — | Após aprovação das regras pendentes |
| Limites de produtos, vendas ou histórico | Nenhum proposto | Nenhum proposto |

O valor anual mantém aproximadamente 16,7% de desconto sobre doze mensalidades. Recursos do Avança que dependem das decisões D03, D05, D08, D09 e D10 só devem ser oferecidos após a definição dessas regras.

## Justificativa de implementação

- **Manter os fluxos essenciais no Dia a Dia:** HU00–HU03 priorizam simplicidade para pequenos negócios. Cadastro, vendas, encomendas, recebimentos e histórico precisam continuar acessíveis para que o plano gratuito resolva o trabalho diário.
- **Oferecer recursos avançados no Avança:** UC26–UC32 descrevem análises, exportações, sugestões, metas e alertas, que ampliam a capacidade de compreender os dados e planejar o negócio.
- **Aplicar o plano por negócio e no servidor:** os casos de uso exigem isolamento entre negócios (RT01); por isso, o plano e suas permissões devem ser verificados no backend, sem depender apenas de ocultar opções na interface.
- **Preservar dados após troca ou cancelamento:** histórico e autoria são regras transversais (RT02). A mudança de plano não deve apagar registros nem bloquear o histórico essencial.
- **Respeitar o escopo offline:** RT08 garante registro local de vendas e consulta ao histórico; não se deve prometer uso offline para outros recursos sem decisão específica.
- **Aguardar regras pendentes antes de ativar recursos dependentes:** análises, recomendações, metas e alertas precisam das definições indicadas em D03, D05 e D08–D10 para evitar cálculos e comportamentos não aprovados.
- **Definir a cobrança da assinatura separadamente:** os casos de uso deixam pagamentos das vendas fora da Ergane. A cobrança recorrente da própria assinatura não está especificada e precisa de regras próprias antes de ser implementada.

## Regras essenciais

- A assinatura fica vinculada ao negócio; a autorização de cada usuário continua independente.
- O plano Dia a Dia cobre o trabalho diário. Troca de plano, cancelamento ou falha de cobrança não apaga dados nem bloqueia o histórico essencial.
- A proposta usa preço fixo por negócio, sem tarifa por venda. Os pagamentos das vendas dos clientes ficam fora da Ergane, conforme o documento de casos de uso; a cobrança da assinatura ainda precisa ser definida.
- Antes da confirmação, a interface informa recursos, preço, periodicidade, renovação e efeito do cancelamento. O servidor valida o acesso aos recursos de cada plano.

## Pontos para validação

1. Testar o preço com usuários. Hipótese de teste: 14 dias do Avança sem cartão, iniciados quando a pessoa ativa um recurso pago; ao final, a conta retorna ao Dia a Dia sem perder dados.
2. As condições de cobrança, renovação, carência, cancelamento, falhas de pagamento e eventuais limites não estão definidas nos documentos consultados.
3. Resolver a divergência entre RN09–RN10, que mencionam pagamento e câmbio nas vendas, e o documento de casos de uso, que mantém os pagamentos fora do app e exclui integrações neste escopo.

## Critérios mínimos de aceite

- Uma conta Dia a Dia conclui os fluxos principais de produtos, vendas, encomendas, recebimentos, conferência e histórico.
- Alterar ou encerrar uma assinatura não remove registros nem afeta dados de outro negócio.
- A tela informa claramente as condições do plano, e o servidor aplica as permissões correspondentes.

Como referência de mercado, não como validação do preço, o Kyte oferece planos pagos a partir de R$ 49,90/mês, e o GestãoClick divulga planos anuais a partir de R$ 119/mês, com escopo mais amplo.

**Base:** HU00–HU03; RN09–RN10; casos de uso v1.0 (24/10/2026), especialmente RT01/RT02/RT05/RT08, D03/D05/D08–D11 e UC25–UC32.
