package br.com.fiap.sout.vendas.application.ports.in;

import java.util.UUID;

public interface VerificarVeiculoVendidoPort {
    boolean verificarVeiculoVendido(UUID veiculoId);
}
