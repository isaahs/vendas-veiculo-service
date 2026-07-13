package br.com.fiap.sout.vendas.adapter.out.persistence;

import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import br.com.fiap.sout.vendas.domain.model.Venda;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VendaEntityMapperTest {

    private final VendaEntityMapper mapper = new VendaEntityMapperImpl();

    @Test
    void shouldReturnNullWhenMappingNull() {
        assertNull(mapper.toDomain(null));
        assertNull(mapper.toEntity(null));
    }

    @Test
    void shouldMapEntityToDomain() {
        LocalDateTime agora = LocalDateTime.now();
        VendaEntity entity = VendaEntity.builder()
            .id(UUID.randomUUID())
            .itemCatalogoId(UUID.randomUUID())
            .cpfComprador("12345678901")
            .dataVenda(agora)
            .codigoPagamento("pay-code-123")
            .status(StatusVenda.PENDENTE_PAGAMENTO)
            .expiraEm(agora.plusMinutes(10))
            .build();

        Venda domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(entity.getId(), domain.id());
        assertEquals(entity.getItemCatalogoId(), domain.itemCatalogoId());
        assertEquals(entity.getCpfComprador(), domain.cpfComprador());
        assertEquals(entity.getDataVenda(), domain.dataVenda());
        assertEquals(entity.getCodigoPagamento(), domain.codigoPagamento());
        assertEquals(entity.getStatus(), domain.status());
        assertEquals(entity.getExpiraEm(), domain.expiraEm());
    }

    @Test
    void shouldMapDomainToEntity() {
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

        assertNotNull(entity);
        assertEquals(domain.id(), entity.getId());
        assertEquals(domain.itemCatalogoId(), entity.getItemCatalogoId());
        assertEquals(domain.cpfComprador(), entity.getCpfComprador());
        assertEquals(domain.dataVenda(), entity.getDataVenda());
        assertEquals(domain.codigoPagamento(), entity.getCodigoPagamento());
        assertEquals(domain.status(), entity.getStatus());
        assertEquals(domain.expiraEm(), entity.getExpiraEm());
    }
}
