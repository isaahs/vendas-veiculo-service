package br.com.fiap.sout.vendas.adapter.in.web.mapper;

import br.com.fiap.sout.vendas.adapter.in.web.dto.VendaResponseDto;
import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import br.com.fiap.sout.vendas.domain.model.Venda;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VendaWebMapperTest {

    private final VendaWebMapper mapper = new VendaWebMapperImpl();

    @Test
    void shouldReturnNullWhenDomainIsNull() {
        assertNull(mapper.toResponseDto(null));
    }

    @Test
    void shouldMapDomainToResponseDto() {
        UUID id = UUID.randomUUID();
        UUID itemCatalogoId = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();
        Venda domain = new Venda(
            id,
            itemCatalogoId,
            "12345678901",
            agora,
            "pay-code-123",
            StatusVenda.PENDENTE_PAGAMENTO,
            agora.plusMinutes(10)
        );

        VendaResponseDto dto = mapper.toResponseDto(domain);

        assertNotNull(dto);
        assertEquals(domain.id(), dto.id());
        assertEquals(domain.itemCatalogoId(), dto.itemCatalogoId());
        assertEquals(domain.cpfComprador(), dto.cpfComprador());
        assertEquals(domain.dataVenda(), dto.dataVenda());
        assertEquals(domain.codigoPagamento(), dto.codigoPagamento());
        assertEquals(domain.status(), dto.status());
        assertEquals(domain.expiraEm(), dto.expiraEm());
    }
}
