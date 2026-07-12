package br.com.fiap.sout.vendas.adapter.in.web.dto;

import br.com.fiap.sout.vendas.domain.enums.StatusPagamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WebhookPagamentoRequestDto(
    @NotBlank(message = "O código de pagamento é obrigatório")
    String codigoPagamento,

    @NotNull(message = "O status do pagamento é obrigatório")
    StatusPagamento status
) {}
