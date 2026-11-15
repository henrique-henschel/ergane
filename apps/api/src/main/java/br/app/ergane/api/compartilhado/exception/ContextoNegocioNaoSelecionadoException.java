package br.app.ergane.api.compartilhado.exception;

public class ContextoNegocioNaoSelecionadoException extends RegraNegocioException {
    public ContextoNegocioNaoSelecionadoException() {
        super("Erro! Selecione um negócio para continuar.");
    }
}
