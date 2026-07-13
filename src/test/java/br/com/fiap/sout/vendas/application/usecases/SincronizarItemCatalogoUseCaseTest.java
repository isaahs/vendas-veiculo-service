package br.com.fiap.sout.vendas.application.usecases;

import br.com.fiap.sout.vendas.application.ports.out.ItemCatalogoRepositoryPort;
import br.com.fiap.sout.vendas.domain.enums.StatusItemCatalogo;
import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SincronizarItemCatalogoUseCaseTest {

    private ItemCatalogoRepositoryPort itemCatalogoRepositoryPort;
    private SincronizarItemCatalogoUseCase useCase;

    @BeforeEach
    void setUp() {
        itemCatalogoRepositoryPort = mock(ItemCatalogoRepositoryPort.class);
        useCase = new SincronizarItemCatalogoUseCase(itemCatalogoRepositoryPort);
    }

    @Test
    void sincronizar_whenItemDoesNotExist_shouldCreateNewItemCatalogo() {
        UUID veiculoId = UUID.randomUUID();
        ItemCatalogo item = new ItemCatalogo(
                UUID.randomUUID(), veiculoId, "Toyota", "Corolla", 2022, "Prata", BigDecimal.valueOf(120000), "ABC1D23", StatusItemCatalogo.DISPONIVEL
        );

        when(itemCatalogoRepositoryPort.buscarPorVeiculoId(veiculoId)).thenReturn(Optional.empty());

        useCase.sincronizar(item);

        verify(itemCatalogoRepositoryPort).salvar(item);
    }

    @Test
    void sincronizar_whenItemExists_shouldUpdateExistingItemCatalogo() {
        UUID veiculoId = UUID.randomUUID();
        UUID localId = UUID.randomUUID();
        ItemCatalogo existente = new ItemCatalogo(
                localId, veiculoId, "Toyota", "Corolla", 2021, "Branco", BigDecimal.valueOf(110000), "ABC1D23", StatusItemCatalogo.RESERVADO
        );

        ItemCatalogo novoItem = new ItemCatalogo(
                UUID.randomUUID(), veiculoId, "Toyota", "Corolla Cross", 2022, "Preto", BigDecimal.valueOf(150000), "ABC1D23", StatusItemCatalogo.DISPONIVEL
        );

        when(itemCatalogoRepositoryPort.buscarPorVeiculoId(veiculoId)).thenReturn(Optional.of(existente));

        useCase.sincronizar(novoItem);

        ArgumentCaptor<ItemCatalogo> captor = ArgumentCaptor.forClass(ItemCatalogo.class);
        verify(itemCatalogoRepositoryPort).salvar(captor.capture());
        ItemCatalogo saved = captor.getValue();

        assertThat(saved.id()).isEqualTo(localId);
        assertThat(saved.veiculoId()).isEqualTo(veiculoId);
        assertThat(saved.marca()).isEqualTo("Toyota");
        assertThat(saved.modelo()).isEqualTo("Corolla Cross");
        assertThat(saved.ano()).isEqualTo(2022);
        assertThat(saved.cor()).isEqualTo("Preto");
        assertThat(saved.preco()).isEqualByComparingTo(BigDecimal.valueOf(150000));
        assertThat(saved.placa()).isEqualTo("ABC1D23");
        assertThat(saved.status()).isEqualTo(StatusItemCatalogo.RESERVADO);
    }
}
