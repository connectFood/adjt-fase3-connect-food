package com.connectfood.auth.infrastructure.security;

import java.io.IOException;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtValidator validator;
  private final AuthenticationEntryPoint authenticationEntryPoint;

  public JwtAuthenticationFilter(
      final JwtValidator validator,
      final AuthenticationEntryPoint authenticationEntryPoint
  ) {
    this.validator = validator;
    this.authenticationEntryPoint = authenticationEntryPoint;
  }

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      @NonNull final HttpServletResponse response,
      @NonNull final FilterChain filterChain
  ) throws ServletException, IOException {

    final var header = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (header != null && header.startsWith("Bearer ")) {
      final var token = header.substring(7);

      try {
        final var principal = validator.validateAndExtract(token);

        final var authorities = principal.roles()
            .stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
            .collect(Collectors.toSet());

        final var auth = new UsernamePasswordAuthenticationToken(
            principal.userUuid()
                .toString(),
            null,
            authorities
        );

        SecurityContextHolder.getContext()
            .setAuthentication(auth);

      } catch (Exception ex) {
        SecurityContextHolder.clearContext();

        authenticationEntryPoint.commence(
            request,
            response,
            new BadCredentialsException("Token inválido ou expirado", ex)
        );
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}
