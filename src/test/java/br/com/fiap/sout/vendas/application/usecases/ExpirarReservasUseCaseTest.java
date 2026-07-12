package br.com.fiap.sout.vendas.application.usecases;

import br.com.fiap.sout.vendas.application.ports.out.ItemCatalogoRepositoryPort;
import br.com.fiap.sout.vendas.application.ports.out.VendaRepositoryPort;
import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import br.com.fiap.sout.vendas.domain.model.Venda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExpirarReservasUseCaseTest {

    private VendaRepositoryPort vendaRepositoryPort;
    private ItemCatalogoRepositoryPort itemCatalogoRepositoryPort;
    private ExpirarReservasUseCase useCase;

    @BeforeEach
    void setUp() {
        vendaRepositoryPort = mock(VendaRepositoryPort.class);
        itemCatalogoRepositoryPort = mock(ItemCatalogoRepositoryPort.class);
        useCase = new ExpirarReservasUseCase(vendaRepositoryPort, itemCatalogoRepositoryPort);
    }

    @Test
    void expirarReservas_shouldCancelExpiredVendasAndMakeItemsAvailable() {
        UUID itemId1 = UUID.randomUUID();
        UUID itemId2 = UUID.randomUUID();
        Venda venda1 = new Venda(UUID.randomUUID(), itemId1, "111", LocalDateTime.now().minusHours(2), "cod-1", StatusVenda.PENDENTE_PAGAMENTO, LocalDateTime.now().minusMinutes(5));
        Venda venda2 = new Venda(UUID.randomUUID(), itemId2, "222", LocalDateTime.now().minusHours(2), "cod-2", StatusVenda.PENDENTE_PAGAMENTO, LocalDateTime.now().minusMinutes(10));
        Venda venda1Cancelada = new Venda(venda1.id(), itemId1, "111", venda1.dataVenda(), "cod-1", StatusVenda.CANCELADA, venda1.expiraEm());

        when(vendaRepositoryPort.buscarVendasExpiradasPendentes(any(LocalDateTime.class))).thenReturn(Arrays.asList(venda1, venda2));
        when(vendaRepositoryPort.atualizarStatusSePendente("cod-1", StatusVenda.CANCELADA)).thenReturn(Optional.of(venda1Cancelada));
        when(vendaRepositoryPort.atualizarStatusSePendente("cod-2", StatusVenda.CANCELADA)).thenReturn(Optional.empty());

        useCase.expirarReservas();

        verify(vendaRepositoryPort).atualizarStatusSePendente("cod-1", StatusVenda.CANCELADA);
        verify(vendaRepositoryPort).atualizarStatusSePendente("cod-2", StatusVenda.CANCELADA);
        verify(itemCatalogoRepositoryPort).marcarComoDisponivel(itemId1);
        verify(itemCatalogoRepositoryPort, never()).marcarComoDisponivel(itemId2);
    }
}
