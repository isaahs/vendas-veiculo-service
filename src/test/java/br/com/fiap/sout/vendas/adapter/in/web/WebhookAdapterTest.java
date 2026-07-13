package br.com.fiap.sout.vendas.adapter.in.web;

import br.com.fiap.sout.vendas.adapter.in.web.dto.WebhookPagamentoRequestDto;
import br.com.fiap.sout.vendas.application.ports.in.ProcessarPagamentoPort;
import br.com.fiap.sout.vendas.domain.enums.StatusPagamento;
import br.com.fiap.sout.vendas.domain.exceptions.AssinaturaAusenteException;
import br.com.fiap.sout.vendas.domain.exceptions.AssinaturaInvalidaException;
import br.com.fiap.sout.vendas.infra.security.HmacSignatureFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebhookAdapterTest {

    private MockMvc mockMvc;
    private ProcessarPagamentoPort processarPagamentoPort;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        processarPagamentoPort = mock(ProcessarPagamentoPort.class);

        WebhookAdapter controller = new WebhookAdapter(processarPagamentoPort);

        HandlerExceptionResolver exceptionResolver = (req, res, handler, ex) -> {
            try {
                ResponseEntity<ProblemDetail> responseEntity;
                GlobalExceptionHandler handlerAdvice = new GlobalExceptionHandler();
                if (ex instanceof AssinaturaAusenteException) {
                    responseEntity = handlerAdvice.handleAssinaturaAusente((AssinaturaAusenteException) ex);
                } else if (ex instanceof AssinaturaInvalidaException) {
                    responseEntity = handlerAdvice.handleAssinaturaInvalida((AssinaturaInvalidaException) ex);
                } else {
                    ex.printStackTrace(); // Imprime qualquer outra exceção para debug
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

        HmacSignatureFilter hmacFilter = new HmacSignatureFilter(
                "minha-chave-secreta-hmac-super-secreta-e-longa-para-webhooks",
                exceptionResolver
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilters(hmacFilter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String calculateHmac(String body) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(
                "minha-chave-secreta-hmac-super-secreta-e-longa-para-webhooks".getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(body.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Test
    void processarPagamento_whenValidHmac_shouldReturn200Ok() throws Exception {
        String codigoPagamento = "cod-123";
        WebhookPagamentoRequestDto request = new WebhookPagamentoRequestDto(codigoPagamento, StatusPagamento.APROVADO);
        String body = objectMapper.writeValueAsString(request);
        String signature = calculateHmac(body);

        mockMvc.perform(post("/pagamentos/webhook")
                        .header("X-Signature", signature)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(processarPagamentoPort, times(1)).processarPagamento(eq(codigoPagamento), eq(StatusPagamento.APROVADO));
    }

    @Test
    void processarPagamento_whenInvalidHmac_shouldReturn403Forbidden() throws Exception {
        String codigoPagamento = "cod-123";
        WebhookPagamentoRequestDto request = new WebhookPagamentoRequestDto(codigoPagamento, StatusPagamento.APROVADO);
        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/pagamentos/webhook")
                        .header("X-Signature", "assinatura-completamente-invalida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Assinatura Inválida"));

        verify(processarPagamentoPort, never()).processarPagamento(anyString(), any(StatusPagamento.class));
    }

    @Test
    void processarPagamento_whenMissingHmac_shouldReturn400BadRequest() throws Exception {
        String codigoPagamento = "cod-123";
        WebhookPagamentoRequestDto request = new WebhookPagamentoRequestDto(codigoPagamento, StatusPagamento.APROVADO);
        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/pagamentos/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Assinatura Ausente"));

        verify(processarPagamentoPort, never()).processarPagamento(anyString(), any(StatusPagamento.class));
    }
}
