package br.com.fiap.sout.vendas.infra.security;

import br.com.fiap.sout.vendas.domain.exceptions.AssinaturaAusenteException;
import br.com.fiap.sout.vendas.domain.exceptions.AssinaturaInvalidaException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerExceptionResolver;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private final String secret = "my-secret-key";
    private HandlerExceptionResolver resolver;
    private JwtAuthenticationFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        resolver = mock(HandlerExceptionResolver.class);
        filter = new JwtAuthenticationFilter(secret, resolver);
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
    void shouldSkipFilterWhenRouteIsNotInternal() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/veiculos");
        when(request.getMethod()).thenReturn("GET");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldResolveExceptionWhenHeaderIsMissingOnInternalRoute() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/veiculos");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(resolver, times(1)).resolveException(eq(request), eq(response), eq(null), any(AssinaturaAusenteException.class));
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldResolveExceptionWhenHeaderDoesNotStartWithBearerOnInternalRoute() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/veiculos");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Basic user:pass");

        filter.doFilterInternal(request, response, filterChain);

        verify(resolver, times(1)).resolveException(eq(request), eq(response), eq(null), any(AssinaturaAusenteException.class));
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldResolveExceptionWhenTokenIsInvalidOnInternalRoute() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/veiculos");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidtoken");

        filter.doFilterInternal(request, response, filterChain);

        verify(resolver, times(1)).resolveException(eq(request), eq(response), eq(null), any(AssinaturaInvalidaException.class));
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldAuthenticateSuccessfullyWhenTokenIsValidOnInternalRoute() throws ServletException, IOException {
        String token = JWT.create()
                .withSubject("catalogo-service")
                .sign(Algorithm.HMAC256(secret));

        when(request.getRequestURI()).thenReturn("/veiculos");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("catalogo-service", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void shouldAuthenticateSuccessfullyOnGetVendidoRoute() throws ServletException, IOException {
        String token = JWT.create()
                .withSubject("catalogo-service")
                .sign(Algorithm.HMAC256(secret));

        when(request.getRequestURI()).thenReturn("/veiculos/123e4567-e89b-12d3-a456-426614174000/vendido");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("catalogo-service", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
