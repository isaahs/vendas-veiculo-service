package br.com.fiap.sout.vendas.infra.security;

import br.com.fiap.sout.vendas.domain.exceptions.AssinaturaAusenteException;
import br.com.fiap.sout.vendas.domain.exceptions.AssinaturaInvalidaException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

@Component
public class HmacSignatureFilter extends OncePerRequestFilter {

    private final String hmacSecret;
    private final HandlerExceptionResolver resolver;

    public HmacSignatureFilter(
            @Value("${hmac.secret}") String hmacSecret,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        this.hmacSecret = hmacSecret;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean isWebhookRoute = request.getMethod().equalsIgnoreCase("POST") && path.equals("/pagamentos/webhook");

        if (isWebhookRoute) {
            CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
            String signatureHeader = cachedRequest.getHeader("X-Signature");

            if (signatureHeader == null || signatureHeader.trim().isEmpty()) {
                resolver.resolveException(cachedRequest, response, null, new AssinaturaAusenteException("Assinatura ausente ou malformada no header X-Signature."));
                return;
            }

            try {
                String calculatedSignature = calculateHmac(cachedRequest.getCachedBody());
                if (!calculatedSignature.equalsIgnoreCase(signatureHeader)) {
                    resolver.resolveException(cachedRequest, response, null, new AssinaturaInvalidaException("Assinatura de webhook inválida."));
                    return;
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        "webhook-pagamento",
                        null,
                        Collections.emptyList()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(cachedRequest, response);
                return;
            } catch (Exception e) {
                resolver.resolveException(cachedRequest, response, null, new AssinaturaInvalidaException("Erro ao processar assinatura do webhook."));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String calculateHmac(byte[] body) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(body);
        
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
}
