package br.com.fiap.sout.vendas.adapter.in.web.mapper;

import br.com.fiap.sout.vendas.adapter.in.web.dto.ItemCatalogoResponseDto;
import br.com.fiap.sout.vendas.domain.enums.StatusItemCatalogo;
import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ItemCatalogoWebMapperTest {

    private final ItemCatalogoWebMapper mapper = new ItemCatalogoWebMapperImpl();

    @Test
    void shouldReturnNullWhenDomainIsNull() {
        assertNull(mapper.toResponseDto(null));
    }

    @Test
    void shouldMapDomainToResponseDto() {
        ItemCatalogo domain = new ItemCatalogo(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Chevrolet",
            "Onix",
            2020,
            "Preto",
            new BigDecimal("60000.00"),
            "ABC1234",
            StatusItemCatalogo.DISPONIVEL
        );

        ItemCatalogoResponseDto dto = mapper.toResponseDto(domain);

        assertNotNull(dto);
        assertEquals(domain.id(), dto.id());
        assertEquals(domain.veiculoId(), dto.veiculoId());
        assertEquals(domain.marca(), dto.marca());
        assertEquals(domain.modelo(), dto.modelo());
        assertEquals(domain.ano(), dto.ano());
        assertEquals(domain.cor(), dto.cor());
        assertEquals(domain.preco(), dto.preco());
        assertEquals(domain.placa(), dto.placa());
        assertEquals(domain.status(), dto.status());
    }
}
