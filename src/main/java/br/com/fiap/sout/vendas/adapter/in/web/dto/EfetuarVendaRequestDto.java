package br.com.fiap.sout.vendas.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EfetuarVendaRequestDto(
    @NotBlank(message = "O CPF do comprador é obrigatório")
    String cpfComprador,

    @NotNull(message = "O ID do item do catálogo é obrigatório")
    UUID itemCatalogoId
) {}
