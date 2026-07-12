package br.com.fiap.sout.vendas.application.usecases;

import br.com.fiap.sout.vendas.application.ports.in.SincronizarItemCatalogoPort;
import br.com.fiap.sout.vendas.application.ports.out.ItemCatalogoRepositoryPort;
import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import java.util.Optional;

public class SincronizarItemCatalogoUseCase implements SincronizarItemCatalogoPort {

    private final ItemCatalogoRepositoryPort itemCatalogoRepositoryPort;

    public SincronizarItemCatalogoUseCase(ItemCatalogoRepositoryPort itemCatalogoRepositoryPort) {
        this.itemCatalogoRepositoryPort = itemCatalogoRepositoryPort;
    }

    @Override
    public void sincronizar(ItemCatalogo itemCatalogo) {
        Optional<ItemCatalogo> existenteOpt = itemCatalogoRepositoryPort.buscarPorVeiculoId(itemCatalogo.veiculoId());

        if (existenteOpt.isPresent()) {
            ItemCatalogo existente = existenteOpt.get();
            ItemCatalogo itemAtualizado = new ItemCatalogo(
                existente.id(),
                itemCatalogo.veiculoId(),
                itemCatalogo.marca(),
                itemCatalogo.modelo(),
                itemCatalogo.ano(),
                itemCatalogo.cor(),
                itemCatalogo.preco(),
                itemCatalogo.placa(),
                existente.status()
            );
            itemCatalogoRepositoryPort.salvar(itemAtualizado);
        } else {
            itemCatalogoRepositoryPort.salvar(itemCatalogo);
        }
    }
}
