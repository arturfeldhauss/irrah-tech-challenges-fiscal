package br.com.irrah.fiscal.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarArgumentoInvalido(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String mensagem = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Existem campos inválidos na requisição.");

        return construirResposta(
                HttpStatus.BAD_REQUEST,
                "Dados inválidos",
                mensagem,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarMensagemNaoLegivel(
            HttpServletRequest request) {

        return construirResposta(
                HttpStatus.BAD_REQUEST,
                "Requisição inválida",
                "O corpo da requisição está inválido ou contém valores não reconhecidos.",
                request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> tratarArgumentoIlegal(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        return construirResposta(
                HttpStatus.BAD_REQUEST,
                "Regra de negócio inválida",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroResponse> tratarCredenciaisInvalidas(
            HttpServletRequest request) {

        return construirResposta(
                HttpStatus.UNAUTHORIZED,
                "Não autorizado",
                "E-mail ou senha inválidos.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroInterno(
            HttpServletRequest request) {

        return construirResposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno",
                "Ocorreu um erro interno inesperado.",
                request
        );
    }

    private ResponseEntity<ErroResponse> construirResposta(
            HttpStatus status,
            String erro,
            String mensagem,
            HttpServletRequest request) {

        ErroResponse corpo = new ErroResponse(
                Instant.now(),
                status.value(),
                erro,
                mensagem,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(corpo);
    }
}
