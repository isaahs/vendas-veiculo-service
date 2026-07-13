package br.com.fiap.sout.vendas.adapter.in.web;

import br.com.fiap.sout.vendas.adapter.in.web.dto.EfetuarVendaRequestDto;
import br.com.fiap.sout.vendas.adapter.in.web.dto.SincronizarVeiculoRequestDto;
import br.com.fiap.sout.vendas.adapter.in.web.dto.WebhookPagamentoRequestDto;
import br.com.fiap.sout.vendas.adapter.out.persistence.ItemCatalogoEntity;
import br.com.fiap.sout.vendas.adapter.out.persistence.ItemCatalogoJpaRepository;
import br.com.fiap.sout.vendas.adapter.out.persistence.VendaEntity;
import br.com.fiap.sout.vendas.adapter.out.persistence.VendaJpaRepository;
import br.com.fiap.sout.vendas.domain.enums.StatusItemCatalogo;
import br.com.fiap.sout.vendas.domain.enums.StatusPagamento;
import br.com.fiap.sout.vendas.domain.enums.StatusVenda;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class VendasServiceIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ItemCatalogoJpaRepository itemCatalogoJpaRepository;

    @Autowired
    private VendaJpaRepository vendaJpaRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @BeforeEach
    void cleanDatabase() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
        vendaJpaRepository.deleteAll();
        itemCatalogoJpaRepository.deleteAll();
    }

    private String generateToken() {
        return com.auth0.jwt.JWT.create()
                .withSubject("catalog-service")
                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256("minha-chave-secreta-m2m-super-secreta-e-longa-para-hmac-sha256"));
    }

    private String calculateHmac(String body) throws Exception {
        javax.crypto.Mac sha256Hmac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(
                "minha-chave-secreta-hmac-super-secreta-e-longa-para-webhooks".getBytes(java.nio.charset.StandardCharsets.UTF_8), 
                "HmacSHA256"
        );
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        
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
    void syncVeiculo_whenUnauthenticated_shouldReturn400() throws Exception {
        SincronizarVeiculoRequestDto request = new SincronizarVeiculoRequestDto(
                UUID.randomUUID(), "BMW", "320i", 2023, "Cinza", BigDecimal.valueOf(320000), "BMW7F89"
        );

        mockMvc.perform(post("/veiculos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void syncVeiculo_whenInvalidToken_shouldReturn403() throws Exception {
        SincronizarVeiculoRequestDto request = new SincronizarVeiculoRequestDto(
                UUID.randomUUID(), "BMW", "320i", 2023, "Cinza", BigDecimal.valueOf(320000), "BMW7F89"
        );

        mockMvc.perform(post("/veiculos")
                .header("Authorization", "Bearer token-completamente-invalido")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void syncVeiculo_whenAuthenticated_shouldCreateItem() throws Exception {
        UUID veiculoId = UUID.randomUUID();
        SincronizarVeiculoRequestDto request = new SincronizarVeiculoRequestDto(
                veiculoId, "BMW", "320i", 2023, "Cinza", BigDecimal.valueOf(320000), "BMW7F89"
        );

        mockMvc.perform(post("/veiculos")
                .header("Authorization", "Bearer " + generateToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertThat(itemCatalogoJpaRepository.findByVeiculoId(veiculoId)).isPresent();
    }

    @Test
    void testEfetuarVendaFlow() throws Exception {
        ItemCatalogoEntity item = ItemCatalogoEntity.builder()
                .id(UUID.randomUUID())
                .veiculoId(UUID.randomUUID())
                .marca("Chevrolet")
                .modelo("Onix")
                .ano(2022)
                .cor("Azul")
                .preco(BigDecimal.valueOf(80000))
                .placa("CHE8I99")
                .status(StatusItemCatalogo.DISPONIVEL)
                .build();
        itemCatalogoJpaRepository.save(item);

        EfetuarVendaRequestDto vendaRequest = new EfetuarVendaRequestDto("12345678900", item.getId());
        String vendaResponseStr = mockMvc.perform(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vendaRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ItemCatalogoEntity itemReservado = itemCatalogoJpaRepository.findById(item.getId()).orElseThrow();
        assertThat(itemReservado.getStatus()).isEqualTo(StatusItemCatalogo.RESERVADO);

        var vendaNode = objectMapper.readTree(vendaResponseStr);
        String codigoPagamento = vendaNode.get("codigoPagamento").asText();

        WebhookPagamentoRequestDto webhookRequest = new WebhookPagamentoRequestDto(codigoPagamento, StatusPagamento.APROVADO);
        String requestBody = objectMapper.writeValueAsString(webhookRequest);
        String signature = calculateHmac(requestBody);

        mockMvc.perform(post("/pagamentos/webhook")
                .header("X-Signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        VendaEntity vendaFinal = vendaJpaRepository.findByCodigoPagamento(codigoPagamento).orElseThrow();
        assertThat(vendaFinal.getStatus()).isEqualTo(StatusVenda.CONFIRMADA);

        ItemCatalogoEntity itemVendido = itemCatalogoJpaRepository.findById(item.getId()).orElseThrow();
        assertThat(itemVendido.getStatus()).isEqualTo(StatusItemCatalogo.VENDIDO);
    }
}
