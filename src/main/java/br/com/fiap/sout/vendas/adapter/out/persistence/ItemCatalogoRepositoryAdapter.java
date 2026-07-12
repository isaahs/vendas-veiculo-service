package br.com.fiap.sout.vendas.adapter.out.persistence;

import br.com.fiap.sout.vendas.application.ports.out.ItemCatalogoRepositoryPort;
import br.com.fiap.sout.vendas.domain.enums.StatusItemCatalogo;
import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Component
public class ItemCatalogoRepositoryAdapter implements ItemCatalogoRepositoryPort {

    private final ItemCatalogoJpaRepository repository;
    private final ItemCatalogoEntityMapper mapper;

    public ItemCatalogoRepositoryAdapter(ItemCatalogoJpaRepository repository, ItemCatalogoEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ItemCatalogo salvar(ItemCatalogo itemCatalogo) {
        ItemCatalogoEntity entity = mapper.toEntity(itemCatalogo);
        ItemCatalogoEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ItemCatalogo> buscarPorId(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ItemCatalogo> buscarPorVeiculoId(UUID veiculoId) {
        return repository.findByVeiculoId(veiculoId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemCatalogo> listarDisponiveis(Pageable pageable) {
        return repository.findByStatusOrderByPrecoAsc(StatusItemCatalogo.DISPONIVEL, pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemCatalogo> listarVendidos(Pageable pageable) {
        return repository.findByStatusOrderByPrecoAsc(StatusItemCatalogo.VENDIDO, pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public int reservar(UUID id) {
        return repository.reservar(id);
    }

    @Override
    @Transactional
    public void marcarComoVendido(UUID id) {
        repository.marcarComoVendido(id);
    }

    @Override
    @Transactional
    public void marcarComoDisponivel(UUID id) {
        repository.marcarComoDisponivel(id);
    }
}
