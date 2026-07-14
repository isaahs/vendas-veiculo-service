package br.com.fiap.sout.vendas.adapter.in.web.dto;

public record DevHmacSignatureResponseDto(
    String body,
    String xSignature
) {}
