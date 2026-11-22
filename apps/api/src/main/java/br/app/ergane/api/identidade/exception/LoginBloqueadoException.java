package br.app.ergane.api.identidade.exception;

public class LoginBloqueadoException extends RuntimeException {
    private final long segundosEspera;

    public LoginBloqueadoException(long segundosEspera) {
        super("Erro! Aguarde antes de tentar acessar novamente.");
        this.segundosEspera = segundosEspera;
    }

    public long getSegundosEspera() {
        return segundosEspera;
    }
}
