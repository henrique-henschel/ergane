package br.app.ergane.api.compartilhado.exception;

public class ContextoContaNaoSelecionadaException extends RegraNegocioException {
    public ContextoContaNaoSelecionadaException() {
        super("Erro! Selecione um negócio para continuar.");
    }
}
