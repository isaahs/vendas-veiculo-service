package br.com.fiap.sout.vendas.domain.exceptions;

public class VendaNaoEncontradaException extends RuntimeException {
    public VendaNaoEncontradaException(String message) {
        super(message);
    }
}
