package br.com.fiap.sout.vendas.adapter.in.web.dto;

import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import java.time.LocalDateTime;
import java.util.UUID;

public record VendaResponseDto(
    UUID id,
    UUID itemCatalogoId,
    String cpfComprador,
    LocalDateTime dataVenda,
    String codigoPagamento,
    StatusVenda status,
    LocalDateTime expiraEm
) {}
