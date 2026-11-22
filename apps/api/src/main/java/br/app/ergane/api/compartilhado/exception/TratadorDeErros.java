package br.app.ergane.api.compartilhado.exception;

import br.app.ergane.api.identidade.exception.AutenticacaoIndisponivelException;
import br.app.ergane.api.identidade.exception.FalhaAutenticacaoException;
import br.app.ergane.api.identidade.exception.LoginBloqueadoException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.core.JacksonException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(FalhaAutenticacaoException.class)
    public ResponseEntity<ProblemDetail> autenticacao(FalhaAutenticacaoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(LoginBloqueadoException.class)
    public ResponseEntity<ProblemDetail> tentativas(LoginBloqueadoException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.RETRY_AFTER, Long.toString(ex.getSegundosEspera()))
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage()));
    }

    @ExceptionHandler(AutenticacaoIndisponivelException.class)
    public ResponseEntity<ProblemDetail> autenticacaoIndisponivel(AutenticacaoIndisponivelException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(RecursoNaoEncontradoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ProblemDetail regraNegocio(RegraNegocioException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (primeira, segunda) -> primeira
                ));

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Erro! Dados inválidos.");
        problema.setProperty("campos", campos);
        return problema;
    }

    // Última barreira: o índice único do banco barrou algo que passou pelo service
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integridade(DataIntegrityViolationException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "Erro! A operação conflita com dados já existentes.");
    }

    // Bloqueio otimista (@Version): outra transação alterou o registro antes desta
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail versaoConcorrente(ObjectOptimisticLockingFailureException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "Erro! O registro foi alterado por outra operação. Atualize e tente novamente.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail jsonInvalido(HttpMessageNotReadableException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Erro! Corpo da requisição inválido.");

        if (ex.getMostSpecificCause() instanceof JacksonException jackson && !jackson.getPath().isEmpty()) {
            String campo = jackson.getPath().getLast().getPropertyName();
            if (campo != null) {
                problema.setProperty("campos", Map.of(campo, "Erro! Valor em formato inválido."));
            }
        }
        return problema;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail parametroInvalido(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Erro! Parâmetro inválido.");
        problema.setProperty("campos", Map.of(ex.getName(), "Erro! Valor em formato inválido."));
        return problema;
    }

}
