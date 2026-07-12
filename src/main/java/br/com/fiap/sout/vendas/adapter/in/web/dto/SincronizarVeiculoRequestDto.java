package br.com.fiap.sout.vendas.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record SincronizarVeiculoRequestDto(
    @NotNull(message = "O ID do veículo é obrigatório")
    UUID veiculoId,

    @NotBlank(message = "A marca é obrigatória")
    String marca,

    @NotBlank(message = "O modelo é obrigatório")
    String modelo,

    @NotNull(message = "O ano é obrigatório")
    Integer ano,

    @NotBlank(message = "A cor é obrigatória")
    String cor,

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser maior que zero")
    BigDecimal preco,

    @NotBlank(message = "A placa é obrigatória")
    String placa
) {}
