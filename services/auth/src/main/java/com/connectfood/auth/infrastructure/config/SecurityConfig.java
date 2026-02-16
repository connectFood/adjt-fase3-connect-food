package com.connectfood.auth.infrastructure.config;

import com.connectfood.auth.infrastructure.security.JwtAuthenticationFilter;
import com.connectfood.auth.infrastructure.security.JwtValidator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtValidator validator) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health", "/actuator/info")
            .permitAll()

            .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/refresh")
            .permitAll()

            .requestMatchers(HttpMethod.GET, "/auth/me")
            .authenticated()

            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
            .permitAll()

            .anyRequest()
            .authenticated()
        )
        .addFilterBefore(new JwtAuthenticationFilter(validator), UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
