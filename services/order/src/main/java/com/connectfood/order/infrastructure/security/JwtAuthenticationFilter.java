package com.connectfood.order.infrastructure.security;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtValidator validator;

  public JwtAuthenticationFilter(JwtValidator validator) {
    this.validator = validator;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    var header = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (header != null && header.startsWith("Bearer ")) {
      var token = header.substring(7).trim();
      token = token.replaceAll("\\s+", "");

      try {
        var principal = validator.validateAndExtract(token);
        var authorities = principal.roles()
            .stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
            .collect(Collectors.toSet());

        var auth = new UsernamePasswordAuthenticationToken(
            principal.userUuid()
                .toString(),
            null,
            authorities
        );

        SecurityContextHolder.getContext()
            .setAuthentication(auth);
      } catch (Exception e) {
        e.printStackTrace();
        SecurityContextHolder.clearContext();
      }
    }

    chain.doFilter(request, response);
  }
}
