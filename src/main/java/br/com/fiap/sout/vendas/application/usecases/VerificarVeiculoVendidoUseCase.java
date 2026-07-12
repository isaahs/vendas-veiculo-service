package br.com.fiap.sout.vendas.application.usecases;

import br.com.fiap.sout.vendas.application.ports.in.VerificarVeiculoVendidoPort;
import br.com.fiap.sout.vendas.application.ports.out.ItemCatalogoRepositoryPort;
import br.com.fiap.sout.vendas.domain.enums.StatusItemCatalogo;
import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import java.util.Optional;
import java.util.UUID;

public class VerificarVeiculoVendidoUseCase implements VerificarVeiculoVendidoPort {

    private final ItemCatalogoRepositoryPort itemCatalogoRepositoryPort;

    public VerificarVeiculoVendidoUseCase(ItemCatalogoRepositoryPort itemCatalogoRepositoryPort) {
        this.itemCatalogoRepositoryPort = itemCatalogoRepositoryPort;
    }

    @Override
    public boolean verificarVeiculoVendido(UUID veiculoId) {
        Optional<ItemCatalogo> itemOpt = itemCatalogoRepositoryPort.buscarPorVeiculoId(veiculoId);
        return itemOpt.map(item -> item.status() == StatusItemCatalogo.VENDIDO).orElse(false);
    }
}
