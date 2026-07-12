package br.com.fiap.sout.vendas.application.usecases;

import br.com.fiap.sout.vendas.application.ports.out.ItemCatalogoRepositoryPort;
import br.com.fiap.sout.vendas.application.ports.out.VendaRepositoryPort;
import br.com.fiap.sout.vendas.domain.enums.StatusPagamento;
import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import br.com.fiap.sout.vendas.domain.exceptions.VendaNaoEncontradaException;
import br.com.fiap.sout.vendas.domain.model.Venda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProcessarPagamentoUseCaseTest {

    private VendaRepositoryPort vendaRepositoryPort;
    private ItemCatalogoRepositoryPort itemCatalogoRepositoryPort;
    private ProcessarPagamentoUseCase useCase;

    @BeforeEach
    void setUp() {
        vendaRepositoryPort = mock(VendaRepositoryPort.class);
        itemCatalogoRepositoryPort = mock(ItemCatalogoRepositoryPort.class);
        useCase = new ProcessarPagamentoUseCase(vendaRepositoryPort, itemCatalogoRepositoryPort);
    }

    @Test
    void processarPagamento_whenVendaDoesNotExist_shouldThrowVendaNaoEncontradaException() {
        String code = "cod-123";

        when(vendaRepositoryPort.atualizarStatusSePendente(code, StatusVenda.CONFIRMADA)).thenReturn(Optional.empty());
        when(vendaRepositoryPort.buscarPorCodigoPagamento(code)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.processarPagamento(code, StatusPagamento.APROVADO))
                .isInstanceOf(VendaNaoEncontradaException.class)
                .hasMessageContaining("não encontrada");

        verify(vendaRepositoryPort).atualizarStatusSePendente(code, StatusVenda.CONFIRMADA);
        verify(vendaRepositoryPort).buscarPorCodigoPagamento(code);
    }

    @Test
    void processarPagamento_whenPaymentApprovedAndStatusIsPendente_shouldConfirmVendaAndMarkAsVendido() {
        String code = "cod-123";
        UUID itemCatalogoId = UUID.randomUUID();
        Venda venda = new Venda(UUID.randomUUID(), itemCatalogoId, "111", LocalDateTime.now(), code, StatusVenda.PENDENTE_PAGAMENTO, LocalDateTime.now().plusHours(1));
        Venda vendaConfirmada = new Venda(venda.id(), itemCatalogoId, "111", venda.dataVenda(), code, StatusVenda.CONFIRMADA, venda.expiraEm());

        when(vendaRepositoryPort.atualizarStatusSePendente(code, StatusVenda.CONFIRMADA)).thenReturn(Optional.of(vendaConfirmada));

        useCase.processarPagamento(code, StatusPagamento.APROVADO);

        verify(vendaRepositoryPort).atualizarStatusSePendente(code, StatusVenda.CONFIRMADA);
        verify(itemCatalogoRepositoryPort).marcarComoVendido(itemCatalogoId);
    }

    @Test
    void processarPagamento_whenPaymentCancelledAndStatusIsPendente_shouldCancelVendaAndMarkAsDisponivel() {
        String code = "cod-123";
        UUID itemCatalogoId = UUID.randomUUID();
        Venda venda = new Venda(UUID.randomUUID(), itemCatalogoId, "111", LocalDateTime.now(), code, StatusVenda.PENDENTE_PAGAMENTO, LocalDateTime.now().plusHours(1));
        Venda vendaCancelada = new Venda(venda.id(), itemCatalogoId, "111", venda.dataVenda(), code, StatusVenda.CANCELADA, venda.expiraEm());

        when(vendaRepositoryPort.atualizarStatusSePendente(code, StatusVenda.CANCELADA)).thenReturn(Optional.of(vendaCancelada));

        useCase.processarPagamento(code, StatusPagamento.CANCELADO);

        verify(vendaRepositoryPort).atualizarStatusSePendente(code, StatusVenda.CANCELADA);
        verify(itemCatalogoRepositoryPort).marcarComoDisponivel(itemCatalogoId);
    }

    @Test
    void processarPagamento_whenVendaAlreadyProcessed_shouldReturnIdempotentWithoutUpdatingItem() {
        String code = "cod-123";
        UUID itemCatalogoId = UUID.randomUUID();
        Venda venda = new Venda(UUID.randomUUID(), itemCatalogoId, "111", LocalDateTime.now(), code, StatusVenda.CONFIRMADA, LocalDateTime.now().plusHours(1));

        when(vendaRepositoryPort.atualizarStatusSePendente(code, StatusVenda.CONFIRMADA)).thenReturn(Optional.empty());
        when(vendaRepositoryPort.buscarPorCodigoPagamento(code)).thenReturn(Optional.of(venda));

        useCase.processarPagamento(code, StatusPagamento.APROVADO);

        verify(vendaRepositoryPort).atualizarStatusSePendente(code, StatusVenda.CONFIRMADA);
        verify(vendaRepositoryPort).buscarPorCodigoPagamento(code);
        verify(itemCatalogoRepositoryPort, never()).marcarComoVendido(any(UUID.class));
    }
}
