package br.app.ergane.api.identidade.exception;

public class AutenticacaoIndisponivelException extends RuntimeException {
    public AutenticacaoIndisponivelException() {
        super("Erro! A autenticação está temporariamente indisponível.");
    }
}
