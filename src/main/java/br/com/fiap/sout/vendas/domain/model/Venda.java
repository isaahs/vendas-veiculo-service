package br.com.fiap.sout.vendas.domain.model;

import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import java.time.LocalDateTime;
import java.util.UUID;

public record Venda(
    UUID id,
    UUID itemCatalogoId,
    String cpfComprador,
    LocalDateTime dataVenda,
    String codigoPagamento,
    StatusVenda status,
    LocalDateTime expiraEm
) {}
