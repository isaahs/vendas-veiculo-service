package br.com.fiap.sout.vendas.application.ports.out;

import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface ItemCatalogoRepositoryPort {
    ItemCatalogo salvar(ItemCatalogo itemCatalogo);
    Optional<ItemCatalogo> buscarPorId(UUID id);
    Optional<ItemCatalogo> buscarPorVeiculoId(UUID veiculoId);
    Page<ItemCatalogo> listarDisponiveis(Pageable pageable);
    Page<ItemCatalogo> listarVendidos(Pageable pageable);
    int reservar(UUID id);
    void marcarComoVendido(UUID id);
    void marcarComoDisponivel(UUID id);
}
