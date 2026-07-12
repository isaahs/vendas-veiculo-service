package br.com.fiap.sout.vendas.adapter.in.web.dto;

import br.com.fiap.sout.vendas.domain.enums.StatusItemCatalogo;
import java.math.BigDecimal;
import java.util.UUID;

public record ItemCatalogoResponseDto(
    UUID id,
    UUID veiculoId,
    String marca,
    String modelo,
    Integer ano,
    String cor,
    BigDecimal preco,
    String placa,
    StatusItemCatalogo status
) {}
