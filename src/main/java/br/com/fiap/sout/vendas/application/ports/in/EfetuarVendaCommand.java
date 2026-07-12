package br.com.fiap.sout.vendas.application.ports.in;

import java.util.UUID;

public record EfetuarVendaCommand(
    String cpfComprador,
    UUID itemCatalogoId
) {
    public EfetuarVendaCommand {
        if (cpfComprador == null || cpfComprador.isBlank()) {
            throw new IllegalArgumentException("O CPF do comprador é obrigatório");
        }
        if (itemCatalogoId == null) {
            throw new IllegalArgumentException("O ID do item do catálogo é obrigatório");
        }
    }
}
