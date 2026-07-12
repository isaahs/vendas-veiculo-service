package br.com.fiap.sout.vendas.application.usecases;

import br.com.fiap.sout.vendas.application.ports.in.ListarVeiculosVendidosPort;
import br.com.fiap.sout.vendas.application.ports.out.ItemCatalogoRepositoryPort;
import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ListarVeiculosVendidosUseCase implements ListarVeiculosVendidosPort {

    private final ItemCatalogoRepositoryPort itemCatalogoRepositoryPort;

    public ListarVeiculosVendidosUseCase(ItemCatalogoRepositoryPort itemCatalogoRepositoryPort) {
        this.itemCatalogoRepositoryPort = itemCatalogoRepositoryPort;
    }

    @Override
    public Page<ItemCatalogo> listarVendidos(Pageable pageable) {
        return itemCatalogoRepositoryPort.listarVendidos(pageable);
    }
}
