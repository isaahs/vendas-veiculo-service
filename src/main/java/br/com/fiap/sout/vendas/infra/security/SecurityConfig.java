package br.com.fiap.sout.vendas.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final HmacSignatureFilter hmacSignatureFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, HmacSignatureFilter hmacSignatureFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.hmacSignatureFilter = hmacSignatureFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/health", "/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/veiculos").permitAll()
                .requestMatchers(HttpMethod.GET, "/veiculos/vendidos").permitAll()
                .requestMatchers(HttpMethod.POST, "/vendas").permitAll()
                .requestMatchers(HttpMethod.POST, "/veiculos").authenticated()
                .requestMatchers(HttpMethod.GET, "/veiculos/*/vendido").authenticated()
                .requestMatchers(HttpMethod.POST, "/pagamentos/webhook").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(hmacSignatureFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
