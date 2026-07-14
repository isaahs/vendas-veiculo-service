package br.com.fiap.sout.vendas.adapter.in.web;

import br.com.fiap.sout.vendas.adapter.in.web.dto.EfetuarVendaRequestDto;
import br.com.fiap.sout.vendas.adapter.in.web.dto.VendaResponseDto;
import br.com.fiap.sout.vendas.adapter.in.web.mapper.VendaWebMapper;
import br.com.fiap.sout.vendas.application.ports.in.EfetuarVendaCommand;
import br.com.fiap.sout.vendas.application.ports.in.EfetuarVendaPort;
import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import br.com.fiap.sout.vendas.domain.model.Venda;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class VendaAdapterTest {

    private MockMvc mockMvc;
    private EfetuarVendaPort efetuarVendaPort;
    private VendaWebMapper webMapper;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        efetuarVendaPort = mock(EfetuarVendaPort.class);
        webMapper = mock(VendaWebMapper.class);

        VendaAdapter controller = new VendaAdapter(efetuarVendaPort, webMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void efetuarVenda_whenValidRequest_shouldReturn201Created() throws Exception {
        UUID itemCatalogoId = UUID.randomUUID();
        EfetuarVendaRequestDto request = new EfetuarVendaRequestDto("123.456.789-00", UUID.randomUUID());

        Venda mockVenda = new Venda(
                UUID.randomUUID(), itemCatalogoId, "123.456.789-00", LocalDateTime.now(),
                "cod-pag-123", StatusVenda.PENDENTE_PAGAMENTO, LocalDateTime.now().plusMinutes(15)
        );

        VendaResponseDto mockResponse = new VendaResponseDto(
                mockVenda.id(), itemCatalogoId, "123.456.789-00", mockVenda.dataVenda(),
                "cod-pag-123", StatusVenda.PENDENTE_PAGAMENTO, mockVenda.expiraEm()
        );

        when(efetuarVendaPort.efetuarVenda(any(EfetuarVendaCommand.class))).thenReturn(mockVenda);
        when(webMapper.toResponseDto(mockVenda)).thenReturn(mockResponse);

        mockMvc.perform(post("/vendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoPagamento").value("cod-pag-123"))
                .andExpect(jsonPath("$.status").value("PENDENTE_PAGAMENTO"));

        verify(efetuarVendaPort, times(1)).efetuarVenda(any(EfetuarVendaCommand.class));
    }

    @Test
    void efetuarVenda_whenInvalidRequest_shouldReturn400BadRequest() throws Exception {
        EfetuarVendaRequestDto request = new EfetuarVendaRequestDto("", null);

        mockMvc.perform(post("/vendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erro de Validação"))
                .andExpect(jsonPath("$.invalid_fields.cpfComprador").exists())
                .andExpect(jsonPath("$.invalid_fields.veiculoId").exists());
    }
}
