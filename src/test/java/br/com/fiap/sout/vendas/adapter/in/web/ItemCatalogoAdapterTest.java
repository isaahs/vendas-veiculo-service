package br.com.fiap.sout.vendas.adapter.in.web;

import br.com.fiap.sout.vendas.adapter.in.web.dto.SincronizarVeiculoRequestDto;
import br.com.fiap.sout.vendas.adapter.in.web.mapper.ItemCatalogoWebMapper;
import br.com.fiap.sout.vendas.application.ports.in.ListarVeiculosDisponiveisPort;
import br.com.fiap.sout.vendas.application.ports.in.ListarVeiculosVendidosPort;
import br.com.fiap.sout.vendas.application.ports.in.SincronizarItemCatalogoPort;
import br.com.fiap.sout.vendas.application.ports.in.VerificarVeiculoVendidoPort;
import br.com.fiap.sout.vendas.domain.exceptions.AssinaturaAusenteException;
import br.com.fiap.sout.vendas.domain.exceptions.AssinaturaInvalidaException;
import br.com.fiap.sout.vendas.domain.model.ItemCatalogo;
import br.com.fiap.sout.vendas.infra.security.JwtAuthenticationFilter;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItemCatalogoAdapterTest {

    private MockMvc mockMvc;
    private SincronizarItemCatalogoPort sincronizarItemCatalogoPort;
    private ListarVeiculosDisponiveisPort listarVeiculosDisponiveisPort;
    private ListarVeiculosVendidosPort listarVeiculosVendidosPort;
    private VerificarVeiculoVendidoPort verificarVeiculoVendidoPort;
    private ItemCatalogoWebMapper webMapper;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        sincronizarItemCatalogoPort = mock(SincronizarItemCatalogoPort.class);
        listarVeiculosDisponiveisPort = mock(ListarVeiculosDisponiveisPort.class);
        listarVeiculosVendidosPort = mock(ListarVeiculosVendidosPort.class);
        verificarVeiculoVendidoPort = mock(VerificarVeiculoVendidoPort.class);
        webMapper = mock(ItemCatalogoWebMapper.class);

        ItemCatalogoAdapter controller = new ItemCatalogoAdapter(
                sincronizarItemCatalogoPort,
                listarVeiculosDisponiveisPort,
                listarVeiculosVendidosPort,
                verificarVeiculoVendidoPort,
                webMapper
        );

        HandlerExceptionResolver exceptionResolver = (req, res, handler, ex) -> {
            try {
                ResponseEntity<ProblemDetail> responseEntity;
                GlobalExceptionHandler handlerAdvice = new GlobalExceptionHandler();
                if (ex instanceof AssinaturaAusenteException) {
                    responseEntity = handlerAdvice.handleAssinaturaAusente((AssinaturaAusenteException) ex);
                } else if (ex instanceof AssinaturaInvalidaException) {
                    responseEntity = handlerAdvice.handleAssinaturaInvalida((AssinaturaInvalidaException) ex);
                } else {
                    return null;
                }
                res.setStatus(responseEntity.getStatusCode().value());
                res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                res.getWriter().write(objectMapper.writeValueAsString(responseEntity.getBody()));
                return new ModelAndView();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(
                "minha-chave-secreta-m2m-super-secreta-e-longa-para-hmac-sha256",
                exceptionResolver
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .addFilters(jwtFilter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String generateToken() {
        return JWT.create()
                .withSubject("catalog-service")
                .sign(Algorithm.HMAC256("minha-chave-secreta-m2m-super-secreta-e-longa-para-hmac-sha256"));
    }

    @Test
    void sincronizar_whenAuthenticated_shouldReturn201Created() throws Exception {
        UUID veiculoId = UUID.randomUUID();
        SincronizarVeiculoRequestDto request = new SincronizarVeiculoRequestDto(
                veiculoId, "Ford", "Ka", 2020, "Vermelho", BigDecimal.valueOf(45000), "KAA1234"
        );

        mockMvc.perform(post("/veiculos")
                        .header("Authorization", "Bearer " + generateToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(sincronizarItemCatalogoPort, times(1)).sincronizar(any(ItemCatalogo.class));
    }

    @Test
    void sincronizar_whenUnauthenticated_shouldReturn400BadRequest() throws Exception {
        UUID veiculoId = UUID.randomUUID();
        SincronizarVeiculoRequestDto request = new SincronizarVeiculoRequestDto(
                veiculoId, "Ford", "Ka", 2020, "Vermelho", BigDecimal.valueOf(45000), "KAA1234"
        );

        mockMvc.perform(post("/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarDisponiveis_shouldReturnPagedList() throws Exception {
        when(listarVeiculosDisponiveisPort.listarDisponiveis(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/veiculos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(listarVeiculosDisponiveisPort, times(1)).listarDisponiveis(any(Pageable.class));
    }

    @Test
    void listarVendidos_shouldReturnPagedList() throws Exception {
        when(listarVeiculosVendidosPort.listarVendidos(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/veiculos/vendidos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(listarVeiculosVendidosPort, times(1)).listarVendidos(any(Pageable.class));
    }

    @Test
    void verificarVendido_whenAuthenticated_shouldReturnBoolean() throws Exception {
        UUID veiculoId = UUID.randomUUID();
        when(verificarVeiculoVendidoPort.verificarVeiculoVendido(veiculoId)).thenReturn(true);

        mockMvc.perform(get("/veiculos/" + veiculoId + "/vendido")
                        .header("Authorization", "Bearer " + generateToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(verificarVeiculoVendidoPort, times(1)).verificarVeiculoVendido(veiculoId);
    }
}
