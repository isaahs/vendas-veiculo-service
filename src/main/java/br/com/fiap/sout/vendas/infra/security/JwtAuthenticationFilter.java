package br.com.fiap.sout.vendas.infra.security;

import br.com.fiap.sout.vendas.domain.exceptions.AssinaturaAusenteException;
import br.com.fiap.sout.vendas.domain.exceptions.AssinaturaInvalidaException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
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

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final String jwtSecret;
    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationFilter(
            @Value("${jwt.secret}") String jwtSecret,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        this.jwtSecret = jwtSecret;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean isInternalRoute = (request.getMethod().equalsIgnoreCase("POST") && path.equals("/veiculos")) ||
                (request.getMethod().equalsIgnoreCase("GET") && path.matches("^/veiculos/[^/]+/vendido$"));

        if (isInternalRoute) {
            String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                resolver.resolveException(request, response, null, new AssinaturaAusenteException("Token JWT ausente ou malformado no header Authorization."));
                return;
            }

            String token = authorizationHeader.substring(7);
            try {
                Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(token);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        decodedJWT.getSubject(),
                        null,
                        Collections.emptyList()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JWTVerificationException e) {
                resolver.resolveException(request, response, null, new AssinaturaInvalidaException("Token JWT inválido ou expirado."));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
