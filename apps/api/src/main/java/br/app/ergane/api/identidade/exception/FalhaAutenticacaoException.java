package br.app.ergane.api.identidade.exception;

public class FalhaAutenticacaoException extends RuntimeException {
    public FalhaAutenticacaoException() {
        super("Erro! Não foi possível autenticar com as credenciais informadas.");
    }
}
