package br.com.fiap.sout.vendas.domain.exceptions;

public class VeiculoIndisponivelException extends RuntimeException {
    public VeiculoIndisponivelException(String message) {
        super(message);
    }
}
