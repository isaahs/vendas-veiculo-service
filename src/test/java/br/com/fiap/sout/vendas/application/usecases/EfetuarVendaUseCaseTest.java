package br.com.fiap.sout.vendas.application.usecases;

import br.com.fiap.sout.vendas.application.ports.in.EfetuarVendaCommand;
import br.com.fiap.sout.vendas.application.ports.out.ItemCatalogoRepositoryPort;
import br.com.fiap.sout.vendas.application.ports.out.VendaRepositoryPort;
import br.com.fiap.sout.vendas.domain.enums.StatusItemCatalogo;
import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import br.com.fiap.sout.vendas.domain.exceptions.VeiculoIndisponivelException;
import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import br.com.fiap.sout.vendas.domain.model.Venda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EfetuarVendaUseCaseTest {

    private ItemCatalogoRepositoryPort itemCatalogoRepositoryPort;
    private VendaRepositoryPort vendaRepositoryPort;
    private EfetuarVendaUseCase useCase;

    @BeforeEach
    void setUp() {
        itemCatalogoRepositoryPort = mock(ItemCatalogoRepositoryPort.class);
        vendaRepositoryPort = mock(VendaRepositoryPort.class);
        useCase = new EfetuarVendaUseCase(itemCatalogoRepositoryPort, vendaRepositoryPort, 15);
    }

    private ItemCatalogo itemCatalogo(UUID id, UUID veiculoId) {
        return new ItemCatalogo(
                id, veiculoId, "Chevrolet", "Onix", 2022, "Azul",
                BigDecimal.valueOf(80000), "CHE8I99", StatusItemCatalogo.DISPONIVEL
        );
    }

    @Test
    void efetuarVenda_whenVeiculoIsAvailable_shouldCreateVenda() {
        UUID veiculoId = UUID.randomUUID();
        UUID itemCatalogoId = UUID.randomUUID();
        String cpf = "123.456.789-00";
        EfetuarVendaCommand command = new EfetuarVendaCommand(cpf, veiculoId);

        when(itemCatalogoRepositoryPort.buscarPorVeiculoId(veiculoId))
                .thenReturn(Optional.of(itemCatalogo(itemCatalogoId, veiculoId)));
        when(itemCatalogoRepositoryPort.reservar(itemCatalogoId)).thenReturn(1);
        when(vendaRepositoryPort.salvar(any(Venda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Venda result = useCase.efetuarVenda(command);

        assertThat(result).isNotNull();
        assertThat(result.itemCatalogoId()).isEqualTo(itemCatalogoId);
        assertThat(result.cpfComprador()).isEqualTo(cpf);
        assertThat(result.status()).isEqualTo(StatusVenda.PENDENTE_PAGAMENTO);
        assertThat(result.codigoPagamento()).isNotBlank();
        assertThat(result.expiraEm()).isAfter(LocalDateTime.now());
        verify(itemCatalogoRepositoryPort).reservar(itemCatalogoId);
        verify(vendaRepositoryPort).salvar(any(Venda.class));
    }

    @Test
    void efetuarVenda_whenVeiculoIsNotAvailable_shouldThrowVeiculoIndisponivelException() {
        UUID veiculoId = UUID.randomUUID();
        UUID itemCatalogoId = UUID.randomUUID();
        String cpf = "123.456.789-00";
        EfetuarVendaCommand command = new EfetuarVendaCommand(cpf, veiculoId);

        when(itemCatalogoRepositoryPort.buscarPorVeiculoId(veiculoId))
                .thenReturn(Optional.of(itemCatalogo(itemCatalogoId, veiculoId)));
        when(itemCatalogoRepositoryPort.reservar(itemCatalogoId)).thenReturn(0);

        assertThatThrownBy(() -> useCase.efetuarVenda(command))
                .isInstanceOf(VeiculoIndisponivelException.class)
                .hasMessageContaining("O veículo não está disponível");

        verify(itemCatalogoRepositoryPort).reservar(itemCatalogoId);
        verify(vendaRepositoryPort, never()).salvar(any(Venda.class));
    }

    @Test
    void efetuarVenda_whenVeiculoIdDoesNotExistNoCatalogo_shouldThrowVeiculoIndisponivelException() {
        UUID veiculoId = UUID.randomUUID();
        String cpf = "123.456.789-00";
        EfetuarVendaCommand command = new EfetuarVendaCommand(cpf, veiculoId);

        when(itemCatalogoRepositoryPort.buscarPorVeiculoId(veiculoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.efetuarVenda(command))
                .isInstanceOf(VeiculoIndisponivelException.class)
                .hasMessageContaining("O veículo não está disponível");

        verify(itemCatalogoRepositoryPort, never()).reservar(any());
        verify(vendaRepositoryPort, never()).salvar(any(Venda.class));
    }
}
