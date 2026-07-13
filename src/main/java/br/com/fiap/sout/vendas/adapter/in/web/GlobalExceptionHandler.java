package br.com.fiap.sout.vendas.adapter.in.web;

import br.com.fiap.sout.vendas.domain.exceptions.AssinaturaAusenteException;
import br.com.fiap.sout.vendas.domain.exceptions.AssinaturaInvalidaException;
import br.com.fiap.sout.vendas.domain.exceptions.VeiculoIndisponivelException;
import br.com.fiap.sout.vendas.domain.exceptions.VendaNaoEncontradaException;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(VeiculoIndisponivelException.class)
    public ResponseEntity<ProblemDetail> handleVeiculoIndisponivel(VeiculoIndisponivelException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Veículo Indisponível");
        problemDetail.setType(URI.create("https://api.vendas.fiap.com.br/errors/veiculo-indisponivel"));
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(VendaNaoEncontradaException.class)
    public ResponseEntity<ProblemDetail> handleVendaNaoEncontrada(VendaNaoEncontradaException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Venda Não Encontrada");
        problemDetail.setType(URI.create("https://api.vendas.fiap.com.br/errors/venda-nao-encontrada"));
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(AssinaturaInvalidaException.class)
    public ResponseEntity<ProblemDetail> handleAssinaturaInvalida(AssinaturaInvalidaException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problemDetail.setTitle("Assinatura Inválida");
        problemDetail.setType(URI.create("https://api.vendas.fiap.com.br/errors/assinatura-invalida"));
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

    @ExceptionHandler(AssinaturaAusenteException.class)
    public ResponseEntity<ProblemDetail> handleAssinaturaAusente(AssinaturaAusenteException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Assinatura Ausente");
        problemDetail.setType(URI.create("https://api.vendas.fiap.com.br/errors/assinatura-ausente"));
        problemDetail.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Erro de validação nos campos informados");
        problemDetail.setTitle("Erro de Validação");
        problemDetail.setType(URI.create("https://api.vendas.fiap.com.br/errors/validacao"));
        problemDetail.setProperty("timestamp", Instant.now());

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problemDetail.setProperty("invalid_fields", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
}
