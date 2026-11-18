package com.ticketbooking.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_GET_URLS = {
            "/api/home",
            "/api/search",
            "/api/reviews/**",
            "/api/cinema/movies/**",
            "/api/cinema/sessions/**",
            "/api/concert/concerts/**",
            "/api/sport/teams/**",
            "/api/sport/matches/**"
    };

    private static final String[] ADMIN_URLS = {
            "/api/cinema/movies/**",
            "/api/cinema/sessions/**",
            "/api/concert/concerts/**",
            "/api/sport/teams/**",
            "/api/sport/matches/**",
            "/api/users/**"
    };

    private static final String[] ALL_BOOKINGS_URLS = {
            "/api/cinema/bookings",
            "/api/concert/bookings",
            "/api/sport/bookings"
    };

    private static final String[] ADMIN_CANCEL_URLS = {
            "/api/cinema/bookings/*/cancel",
            "/api/concert/bookings/*/cancel",
            "/api/sport/bookings/*/cancel"
    };

    private final JwtService jwtService;
    private final String allowedOrigin;

    public SecurityConfig(JwtService jwtService, @Value("${app.cors.allowed-origin}") String allowedOrigin) {
        this.jwtService = jwtService;
        this.allowedOrigin = allowedOrigin;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, e) ->
                                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Войдите в аккаунт"))
                        .accessDeniedHandler((request, response, e) ->
                                writeError(response, HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав")))
                .authorizeHttpRequests(rules -> rules
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_URLS).permitAll()
                        .requestMatchers(ADMIN_URLS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, ALL_BOOKINGS_URLS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, ADMIN_CANCEL_URLS).hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
