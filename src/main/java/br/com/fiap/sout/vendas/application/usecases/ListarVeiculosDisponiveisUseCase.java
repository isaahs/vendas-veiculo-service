package br.com.fiap.sout.vendas.application.usecases;

import br.com.fiap.sout.vendas.application.ports.in.ListarVeiculosDisponiveisPort;
import br.com.fiap.sout.vendas.application.ports.out.ItemCatalogoRepositoryPort;
import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ListarVeiculosDisponiveisUseCase implements ListarVeiculosDisponiveisPort {

    private final ItemCatalogoRepositoryPort itemCatalogoRepositoryPort;

    public ListarVeiculosDisponiveisUseCase(ItemCatalogoRepositoryPort itemCatalogoRepositoryPort) {
        this.itemCatalogoRepositoryPort = itemCatalogoRepositoryPort;
    }

    @Override
    public Page<ItemCatalogo> listarDisponiveis(Pageable pageable) {
        return itemCatalogoRepositoryPort.listarDisponiveis(pageable);
    }
}
