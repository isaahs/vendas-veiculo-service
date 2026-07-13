package br.com.fiap.sout.vendas.adapter.out.persistence;

import br.com.fiap.sout.vendas.domain.enums.StatusItemCatalogo;
import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ItemCatalogoEntityMapperTest {

    private final ItemCatalogoEntityMapper mapper = new ItemCatalogoEntityMapperImpl();

    @Test
    void shouldReturnNullWhenMappingNull() {
        assertNull(mapper.toDomain(null));
        assertNull(mapper.toEntity(null));
    }

    @Test
    void shouldMapEntityToDomain() {
        ItemCatalogoEntity entity = ItemCatalogoEntity.builder()
            .id(UUID.randomUUID())
            .veiculoId(UUID.randomUUID())
            .marca("Chevrolet")
            .modelo("Onix")
            .ano(2020)
            .cor("Preto")
            .preco(new BigDecimal("60000.00"))
            .placa("ABC1234")
            .status(StatusItemCatalogo.DISPONIVEL)
            .build();

        ItemCatalogo domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(entity.getId(), domain.id());
        assertEquals(entity.getVeiculoId(), domain.veiculoId());
        assertEquals(entity.getMarca(), domain.marca());
        assertEquals(entity.getModelo(), domain.modelo());
        assertEquals(entity.getAno(), domain.ano());
        assertEquals(entity.getCor(), domain.cor());
        assertEquals(entity.getPreco(), domain.preco());
        assertEquals(entity.getPlaca(), domain.placa());
        assertEquals(entity.getStatus(), domain.status());
    }

    @Test
    void shouldMapDomainToEntity() {
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

        ItemCatalogoEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(domain.id(), entity.getId());
        assertEquals(domain.veiculoId(), entity.getVeiculoId());
        assertEquals(domain.marca(), entity.getMarca());
        assertEquals(domain.modelo(), entity.getModelo());
        assertEquals(domain.ano(), entity.getAno());
        assertEquals(domain.cor(), entity.getCor());
        assertEquals(domain.preco(), entity.getPreco());
        assertEquals(domain.placa(), entity.getPlaca());
        assertEquals(domain.status(), entity.getStatus());
    }
}
