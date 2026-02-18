package com.connectfood.auth.infrastructure.security;

import java.io.IOException;

import com.connectfood.auth.entrypoint.rest.dto.common.ProblemDetailsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ProblemDetailsAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  public ProblemDetailsAuthenticationEntryPoint(final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final AuthenticationException authException
  ) throws IOException {

    final var status = HttpStatus.UNAUTHORIZED;

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    final var body = new ProblemDetailsResponse(
        "https://httpstatuses.com/" + status.value(),
        status.getReasonPhrase(),
        status.value(),
        authException.getMessage() == null ? "Usuário não está autenticado" : authException.getMessage(),
        request.getRequestURI()
    );

    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
