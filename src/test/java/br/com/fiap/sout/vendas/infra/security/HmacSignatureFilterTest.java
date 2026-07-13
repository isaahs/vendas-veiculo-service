package br.com.fiap.sout.vendas.infra.security;

import br.com.fiap.sout.vendas.domain.exceptions.AssinaturaAusenteException;
import br.com.fiap.sout.vendas.domain.exceptions.AssinaturaInvalidaException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HmacSignatureFilterTest {

    private final String secret = "hmac-secret-key-1234567890";
    private HandlerExceptionResolver resolver;
    private HmacSignatureFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        resolver = mock(HandlerExceptionResolver.class);
        filter = new HmacSignatureFilter(secret, resolver);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSkipFilterWhenRouteIsNotWebhook() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/vendas");
        when(request.getMethod()).thenReturn("POST");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldResolveExceptionWhenSignatureHeaderIsMissingOnWebhookRoute() throws ServletException, IOException {
        setupMockRequestBody("{\"id\":123}");
        when(request.getRequestURI()).thenReturn("/pagamentos/webhook");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Signature")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(resolver, times(1)).resolveException(any(), eq(response), eq(null), any(AssinaturaAusenteException.class));
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldResolveExceptionWhenSignatureHeaderIsEmptyOnWebhookRoute() throws ServletException, IOException {
        setupMockRequestBody("{\"id\":123}");
        when(request.getRequestURI()).thenReturn("/pagamentos/webhook");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Signature")).thenReturn("   ");

        filter.doFilterInternal(request, response, filterChain);

        verify(resolver, times(1)).resolveException(any(), eq(response), eq(null), any(AssinaturaAusenteException.class));
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldResolveExceptionWhenSignatureIsInvalidOnWebhookRoute() throws ServletException, IOException {
        setupMockRequestBody("{\"id\":123}");
        when(request.getRequestURI()).thenReturn("/pagamentos/webhook");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Signature")).thenReturn("invalidsignature");

        filter.doFilterInternal(request, response, filterChain);

        verify(resolver, times(1)).resolveException(any(), eq(response), eq(null), any(AssinaturaInvalidaException.class));
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldAuthenticateSuccessfullyWhenSignatureIsValidOnWebhookRoute() throws ServletException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        String body = "{\"codigoPagamento\":\"pay-123\",\"status\":\"APROVADO\"}";
        setupMockRequestBody(body);
        String validSignature = calculateHmac(body.getBytes(StandardCharsets.UTF_8));

        when(request.getRequestURI()).thenReturn("/pagamentos/webhook");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Signature")).thenReturn(validSignature);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(CachedBodyHttpServletRequest.class), eq(response));
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("webhook-pagamento", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private void setupMockRequestBody(String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ServletInputStream sis = new ServletInputStream() {
            @Override
            public boolean isFinished() { return bais.available() == 0; }
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setReadListener(jakarta.servlet.ReadListener readListener) {}
            @Override
            public int read() { return bais.read(); }
        };
        when(request.getInputStream()).thenReturn(sis);
    }

    private String calculateHmac(byte[] body) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
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
