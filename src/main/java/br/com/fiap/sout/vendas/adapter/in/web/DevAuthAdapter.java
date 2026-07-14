package br.com.fiap.sout.vendas.adapter.in.web;

import br.com.fiap.sout.vendas.adapter.in.web.dto.DevHmacSignatureRequestDto;
import br.com.fiap.sout.vendas.adapter.in.web.dto.DevHmacSignatureResponseDto;
import br.com.fiap.sout.vendas.adapter.in.web.dto.DevTokenResponseDto;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Só existe com o profile "dev" ativo — nunca deve ir para produção, pois emite tokens M2M e assinaturas válidas sem autenticação real.
@RestController
@RequestMapping("/auth")
@Profile("dev")
public class DevAuthAdapter {

    private static final long EXPIRACAO_SEGUNDOS = 3600;

    private final String jwtSecret;
    private final String hmacSecret;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DevAuthAdapter(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${hmac.secret}") String hmacSecret
    ) {
        this.jwtSecret = jwtSecret;
        this.hmacSecret = hmacSecret;
    }

    @PostMapping("/dev-token")
    @Operation(summary = "[DEV ONLY] Gera um token JWT M2M para testar as rotas internas no Swagger")
    public ResponseEntity<DevTokenResponseDto> gerarTokenDev() {
        Date expiraEm = new Date(System.currentTimeMillis() + EXPIRACAO_SEGUNDOS * 1000);
        String token = JWT.create()
                .withSubject("catalogo-service")
                .withExpiresAt(expiraEm)
                .sign(Algorithm.HMAC256(jwtSecret));

        return ResponseEntity.ok(new DevTokenResponseDto(token, "Bearer", EXPIRACAO_SEGUNDOS));
    }

    @PostMapping("/dev-hmac-signature")
    @Operation(summary = "[DEV ONLY] Gera o corpo canônico e o header X-Signature para testar o webhook de pagamentos no Swagger")
    public ResponseEntity<DevHmacSignatureResponseDto> gerarAssinaturaDev(@Valid @RequestBody DevHmacSignatureRequestDto request) throws Exception {
        String body = objectMapper.writeValueAsString(request);

        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        sha256Hmac.init(new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = sha256Hmac.doFinal(body.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            String h = Integer.toHexString(0xff & b);
            if (h.length() == 1) hex.append('0');
            hex.append(h);
        }

        return ResponseEntity.ok(new DevHmacSignatureResponseDto(body, hex.toString()));
    }
}
