package com.connectfood.auth.infrastructure.security;

import java.io.IOException;

import com.connectfood.auth.entrypoint.rest.dto.common.ProblemDetailsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ProblemDetailsAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  public ProblemDetailsAccessDeniedHandler(final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final @NonNull AccessDeniedException accessDeniedException
  ) throws IOException {

    final var status = HttpStatus.FORBIDDEN;

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    final var body = new ProblemDetailsResponse(
        "https://httpstatuses.com/" + status.value(),
        status.getReasonPhrase(),
        status.value(),
        "Acesso negado",
        request.getRequestURI()
    );

    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
