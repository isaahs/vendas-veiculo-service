package br.com.fiap.sout.vendas.adapter.in.web.dto;

public record DevTokenResponseDto(
    String token,
    String tokenType,
    long expiraEmSegundos
) {}
