package br.com.fiap.sout.vendas.application.ports.in;

import br.com.fiap.sout.vendas.domain.enums.StatusPagamento;

public interface ProcessarPagamentoPort {
    void processarPagamento(String codigoPagamento, StatusPagamento status);
}
