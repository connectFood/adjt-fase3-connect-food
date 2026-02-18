package com.connectfood.auth.infrastructure.config;

import com.connectfood.auth.infrastructure.security.JwtAuthenticationFilter;
import com.connectfood.auth.infrastructure.security.JwtValidator;
import com.connectfood.auth.infrastructure.security.ProblemDetailsAccessDeniedHandler;
import com.connectfood.auth.infrastructure.security.ProblemDetailsAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      final HttpSecurity http,
      final JwtValidator validator,
      final ObjectMapper objectMapper
  ) throws Exception {

    final var entryPoint = new ProblemDetailsAuthenticationEntryPoint(objectMapper);
    final var accessDeniedHandler = new ProblemDetailsAccessDeniedHandler(objectMapper);

    return http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(entryPoint)
            .accessDeniedHandler(accessDeniedHandler)
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health", "/actuator/info")
            .permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/refresh")
            .permitAll()
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
            .permitAll()
            .anyRequest()
            .authenticated()
        )
        .addFilterBefore(
            new JwtAuthenticationFilter(validator, entryPoint),
            UsernamePasswordAuthenticationFilter.class
        )
        .build();
  }
}
