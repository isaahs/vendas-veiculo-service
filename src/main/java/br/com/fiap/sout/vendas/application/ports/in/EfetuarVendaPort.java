package br.com.fiap.sout.vendas.application.ports.in;

import br.com.fiap.sout.vendas.domain.model.Venda;

public interface EfetuarVendaPort {
    Venda efetuarVenda(EfetuarVendaCommand command);
}
