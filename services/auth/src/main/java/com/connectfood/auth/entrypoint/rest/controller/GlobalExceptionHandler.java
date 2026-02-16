package com.connectfood.auth.entrypoint.rest.controller;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import com.connectfood.auth.domain.exception.BadRequestException;
import com.connectfood.auth.domain.exception.NotFoundException;
import com.connectfood.auth.domain.exception.UnauthorizedException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ProblemDetail> handleBadRequest(
      BadRequestException ex,
      HttpServletRequest request
  ) {
    return buildProblem(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request.getRequestURI(), null);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ProblemDetail> handleUnauthorized(
      UnauthorizedException ex,
      HttpServletRequest request
  ) {
    return buildProblem(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request.getRequestURI(), null);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ProblemDetail> handleNotFound(
      NotFoundException ex,
      HttpServletRequest request
  ) {
    return buildProblem(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request.getRequestURI(), null);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidation(
      MethodArgumentNotValidException ex,
      HttpServletRequest request
  ) {
    var errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(this::toValidationError)
        .collect(Collectors.toList());

    return buildProblem(
        HttpStatus.BAD_REQUEST,
        "Validation Error",
        "One or more fields are invalid",
        request.getRequestURI(),
        Map.of("errors", errors)
    );
  }

  // Spring Security: 401 (Authentication)
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ProblemDetail> handleAuthentication(
      AuthenticationException ex,
      HttpServletRequest request
  ) {
    return buildProblem(HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication failed", request.getRequestURI(),
        null
    );
  }

  // Spring Security: 403 (Authorization)
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> handleAccessDenied(
      AccessDeniedException ex,
      HttpServletRequest request
  ) {
    return buildProblem(HttpStatus.FORBIDDEN, "Forbidden", "Access denied", request.getRequestURI(), null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGeneric(
      Exception ex,
      HttpServletRequest request
  ) {
    return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Unexpected error",
        request.getRequestURI(), null
    );
  }

  private ResponseEntity<ProblemDetail> buildProblem(
      HttpStatus status,
      String title,
      String detail,
      String instance,
      Map<String, Object> extensions
  ) {
    var problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(title);

    // RFC7807 fields
    problem.setType(java.net.URI.create("https://connectfood.com/problems/" + toSlug(title)));
    problem.setInstance(java.net.URI.create(instance));

    // extensions
    problem.setProperty("timestamp", OffsetDateTime.now()
        .toString()
    );

    if (extensions != null) {
      extensions.forEach(problem::setProperty);
    }

    return ResponseEntity.status(status)
        .body(problem);
  }

  private Map<String, String> toValidationError(FieldError fe) {
    return Map.of(
        "field", fe.getField(),
        "message", fe.getDefaultMessage() == null ? "Invalid value" : fe.getDefaultMessage()
    );
  }

  private String toSlug(String value) {
    return value.toLowerCase()
        .replace(" ", "-")
        .replace("/", "-");
  }
}
