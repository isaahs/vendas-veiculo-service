package br.com.fiap.sout.vendas.application.ports.in;

import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EfetuarVendaCommandTest {

    @Test
    void shouldConstructSuccessfullyWhenArgumentsAreValid() {
        UUID id = UUID.randomUUID();
        EfetuarVendaCommand command = new EfetuarVendaCommand("12345678901", id);
        assertEquals("12345678901", command.cpfComprador());
        assertEquals(id, command.itemCatalogoId());
    }

    @Test
    void shouldThrowExceptionWhenCpfCompradorIsNull() {
        UUID id = UUID.randomUUID();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            new EfetuarVendaCommand(null, id)
        );
        assertEquals("O CPF do comprador é obrigatório", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCpfCompradorIsBlank() {
        UUID id = UUID.randomUUID();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            new EfetuarVendaCommand("   ", id)
        );
        assertEquals("O CPF do comprador é obrigatório", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenItemCatalogoIdIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            new EfetuarVendaCommand("12345678901", null)
        );
        assertEquals("O ID do item do catálogo é obrigatório", exception.getMessage());
    }
}
