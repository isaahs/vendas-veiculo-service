package br.com.fiap.sout.vendas.adapter.out.persistence;

import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import br.com.fiap.sout.vendas.domain.model.Venda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VendaRepositoryAdapterTest {

    private VendaJpaRepository repository;
    private VendaEntityMapper mapper;
    private VendaRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(VendaJpaRepository.class);
        mapper = new VendaEntityMapperImpl();
        adapter = new VendaRepositoryAdapter(repository, mapper);
    }

    @Test
    void shouldSalvarSuccessfully() {
        LocalDateTime agora = LocalDateTime.now();
        Venda domain = new Venda(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "12345678901",
            agora,
            "pay-code-123",
            StatusVenda.PENDENTE_PAGAMENTO,
            agora.plusMinutes(10)
        );

        VendaEntity entity = mapper.toEntity(domain);
        when(repository.save(any(VendaEntity.class))).thenReturn(entity);

        Venda result = adapter.salvar(domain);

        assertNotNull(result);
        assertEquals(domain.id(), result.id());
        verify(repository, times(1)).save(any(VendaEntity.class));
    }

    @Test
    void shouldBuscarPorCodigoPagamento() {
        String codigo = "pay-code-123";
        VendaEntity entity = VendaEntity.builder()
            .id(UUID.randomUUID())
            .itemCatalogoId(UUID.randomUUID())
            .cpfComprador("12345678901")
            .dataVenda(LocalDateTime.now())
            .codigoPagamento(codigo)
            .status(StatusVenda.PENDENTE_PAGAMENTO)
            .expiraEm(LocalDateTime.now().plusMinutes(10))
            .build();

        when(repository.findByCodigoPagamento(codigo)).thenReturn(Optional.of(entity));

        Optional<Venda> result = adapter.buscarPorCodigoPagamento(codigo);

        assertTrue(result.isPresent());
        assertEquals(entity.getId(), result.get().id());
    }

    @Test
    void shouldBuscarVendasExpiradasPendentes() {
        LocalDateTime agora = LocalDateTime.now();
        VendaEntity entity = VendaEntity.builder()
            .id(UUID.randomUUID())
            .itemCatalogoId(UUID.randomUUID())
            .cpfComprador("12345678901")
            .dataVenda(agora.minusMinutes(15))
            .codigoPagamento("pay-123")
            .status(StatusVenda.PENDENTE_PAGAMENTO)
            .expiraEm(agora.minusMinutes(5))
            .build();

        when(repository.findByStatusAndExpiraEmBefore(StatusVenda.PENDENTE_PAGAMENTO, agora))
            .thenReturn(List.of(entity));

        List<Venda> result = adapter.buscarVendasExpiradasPendentes(agora);

        assertEquals(1, result.size());
        assertEquals(entity.getId(), result.get(0).id());
    }

    @Test
    void shouldAtualizarStatusSePendenteWhenLinesAffectedIsGreaterThanZero() {
        String codigo = "pay-code-123";
        StatusVenda novoStatus = StatusVenda.CONFIRMADA;
        VendaEntity entity = VendaEntity.builder()
            .id(UUID.randomUUID())
            .itemCatalogoId(UUID.randomUUID())
            .cpfComprador("12345678901")
            .dataVenda(LocalDateTime.now())
            .codigoPagamento(codigo)
            .status(novoStatus)
            .expiraEm(LocalDateTime.now().plusMinutes(10))
            .build();

        when(repository.atualizarStatusSePendente(codigo, novoStatus)).thenReturn(1);
        when(repository.findByCodigoPagamento(codigo)).thenReturn(Optional.of(entity));

        Optional<Venda> result = adapter.atualizarStatusSePendente(codigo, novoStatus);

        assertTrue(result.isPresent());
        assertEquals(novoStatus, result.get().status());
    }

    @Test
    void shouldReturnEmptyWhenAtualizarStatusSePendenteLinesAffectedIsZero() {
        String codigo = "pay-code-123";
        StatusVenda novoStatus = StatusVenda.CONFIRMADA;

        when(repository.atualizarStatusSePendente(codigo, novoStatus)).thenReturn(0);

        Optional<Venda> result = adapter.atualizarStatusSePendente(codigo, novoStatus);

        assertFalse(result.isPresent());
        verify(repository, never()).findByCodigoPagamento(anyString());
    }
}
