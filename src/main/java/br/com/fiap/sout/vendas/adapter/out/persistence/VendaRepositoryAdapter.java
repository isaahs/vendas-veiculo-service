package br.com.fiap.sout.vendas.adapter.out.persistence;

import br.com.fiap.sout.vendas.application.ports.out.VendaRepositoryPort;
import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import br.com.fiap.sout.vendas.domain.model.Venda;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class VendaRepositoryAdapter implements VendaRepositoryPort {

    private final VendaJpaRepository repository;
    private final VendaEntityMapper mapper;

    public VendaRepositoryAdapter(VendaJpaRepository repository, VendaEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Venda salvar(Venda venda) {
        VendaEntity entity = mapper.toEntity(venda);
        VendaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Venda> buscarPorCodigoPagamento(String codigoPagamento) {
        return repository.findByCodigoPagamento(codigoPagamento).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venda> buscarVendasExpiradasPendentes(LocalDateTime agora) {
        return repository.findByStatusAndExpiraEmBefore(StatusVenda.PENDENTE_PAGAMENTO, agora)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<Venda> atualizarStatusSePendente(String codigoPagamento, StatusVenda novoStatus) {
        int linhasAfetadas = repository.atualizarStatusSePendente(codigoPagamento, novoStatus);
        if (linhasAfetadas > 0) {
            return repository.findByCodigoPagamento(codigoPagamento).map(mapper::toDomain);
        }
        return Optional.empty();
    }
}
