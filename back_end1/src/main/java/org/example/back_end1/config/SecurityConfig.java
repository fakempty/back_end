package org.example.back_end1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Дозволяє використовувати @PreAuthorize на рівнях методів (Вимога Лаб №1, AC7)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Публічні ендпоінти (документація, головна сторінка тощо)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/swagger-ui.html", "/error").permitAll()
                        // Обмеження на рівні SecurityFilterChain (Вимога Лаб №1, AC6)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Усі інші запити вимагають автентифікації (OIDC або JWT)
                        .requestMatchers("/api/**").permitAll()
            .anyRequest().authenticated()
                )
                // 1. OIDC автентифікація для UI-користувачів (перенаправлення на Keycloak)
                .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/swagger-ui/index.html", true))
                // 2. JWT Resource Server для прямих API-запитів з Postman/curl
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(org.springframework.security.config.Customizer.withDefaults()));

        return http.build();
    }
}