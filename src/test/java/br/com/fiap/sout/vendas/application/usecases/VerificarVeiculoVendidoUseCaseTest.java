package br.com.fiap.sout.vendas.application.usecases;

import br.com.fiap.sout.vendas.application.ports.out.ItemCatalogoRepositoryPort;
import br.com.fiap.sout.vendas.domain.enums.StatusItemCatalogo;
import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class VerificarVeiculoVendidoUseCaseTest {

    private ItemCatalogoRepositoryPort itemCatalogoRepositoryPort;
    private VerificarVeiculoVendidoUseCase useCase;

    @BeforeEach
    void setUp() {
        itemCatalogoRepositoryPort = mock(ItemCatalogoRepositoryPort.class);
        useCase = new VerificarVeiculoVendidoUseCase(itemCatalogoRepositoryPort);
    }

    @Test
    void verificarVeiculoVendido_whenVeiculoExistsAndIsVendido_shouldReturnTrue() {
        UUID veiculoId = UUID.randomUUID();
        ItemCatalogo item = new ItemCatalogo(
                UUID.randomUUID(), veiculoId, "Ford", "Focus", 2019, "Azul", BigDecimal.valueOf(70000), "KLO9089", StatusItemCatalogo.VENDIDO
        );

        when(itemCatalogoRepositoryPort.buscarPorVeiculoId(veiculoId)).thenReturn(Optional.of(item));

        boolean result = useCase.verificarVeiculoVendido(veiculoId);

        assertThat(result).isTrue();
    }

    @Test
    void verificarVeiculoVendido_whenVeiculoExistsAndIsNotVendido_shouldReturnFalse() {
        UUID veiculoId = UUID.randomUUID();
        ItemCatalogo item = new ItemCatalogo(
                UUID.randomUUID(), veiculoId, "Ford", "Focus", 2019, "Azul", BigDecimal.valueOf(70000), "KLO9089", StatusItemCatalogo.DISPONIVEL
        );

        when(itemCatalogoRepositoryPort.buscarPorVeiculoId(veiculoId)).thenReturn(Optional.of(item));

        boolean result = useCase.verificarVeiculoVendido(veiculoId);

        assertThat(result).isFalse();
    }

    @Test
    void verificarVeiculoVendido_whenVeiculoDoesNotExist_shouldReturnFalse() {
        UUID veiculoId = UUID.randomUUID();

        when(itemCatalogoRepositoryPort.buscarPorVeiculoId(veiculoId)).thenReturn(Optional.empty());

        boolean result = useCase.verificarVeiculoVendido(veiculoId);

        assertThat(result).isFalse();
    }
}
