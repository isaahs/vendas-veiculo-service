package br.com.fiap.sout.vendas.application.usecases;

import br.com.fiap.sout.vendas.application.ports.out.ItemCatalogoRepositoryPort;
import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ListarVeiculosVendidosUseCaseTest {

    private ItemCatalogoRepositoryPort itemCatalogoRepositoryPort;
    private ListarVeiculosVendidosUseCase useCase;

    @BeforeEach
    void setUp() {
        itemCatalogoRepositoryPort = mock(ItemCatalogoRepositoryPort.class);
        useCase = new ListarVeiculosVendidosUseCase(itemCatalogoRepositoryPort);
    }

    @Test
    void listarVendidos_shouldDelegateToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ItemCatalogo> page = new PageImpl<>(Collections.emptyList());

        when(itemCatalogoRepositoryPort.listarVendidos(pageable)).thenReturn(page);

        Page<ItemCatalogo> result = useCase.listarVendidos(pageable);

        assertThat(result).isSameAs(page);
        verify(itemCatalogoRepositoryPort).listarVendidos(pageable);
    }
}
