package com.connectfood.auth.entrypoint.rest.controller;

import com.connectfood.auth.application.usecase.LoginUseCase;
import com.connectfood.auth.application.usecase.RefreshTokenUseCase;
import com.connectfood.auth.application.usecase.RegisterUserUseCase;
import com.connectfood.auth.entrypoint.rest.dto.LoginRequest;
import com.connectfood.auth.entrypoint.rest.dto.MeResponse;
import com.connectfood.auth.entrypoint.rest.dto.RefreshRequest;
import com.connectfood.auth.entrypoint.rest.dto.RegisterRequest;
import com.connectfood.auth.entrypoint.rest.dto.TokensResponse;
import com.connectfood.auth.entrypoint.rest.mapper.AuthRestMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final RegisterUserUseCase registerUserUseCase;
  private final LoginUseCase loginUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;

  public AuthController(
      RegisterUserUseCase registerUserUseCase,
      LoginUseCase loginUseCase,
      RefreshTokenUseCase refreshTokenUseCase
  ) {
    this.registerUserUseCase = registerUserUseCase;
    this.loginUseCase = loginUseCase;
    this.refreshTokenUseCase = refreshTokenUseCase;
  }

  @PostMapping("/register")
  public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
    registerUserUseCase.execute(AuthRestMapper.toInput(request));
    return ResponseEntity.status(201)
        .build();
  }

  @PostMapping("/login")
  public ResponseEntity<TokensResponse> login(@Valid @RequestBody LoginRequest request) {
    var out = loginUseCase.execute(AuthRestMapper.toInput(request));
    return ResponseEntity.ok(new TokensResponse(out.accessToken(), out.refreshToken(), out.expiresInSeconds()));
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokensResponse> refresh(@Valid @RequestBody RefreshRequest request) {
    var out = refreshTokenUseCase.execute(AuthRestMapper.toInput(request));
    return ResponseEntity.ok(new TokensResponse(out.accessToken(), out.refreshToken(), out.expiresInSeconds()));
  }

  @GetMapping("/me")
  public ResponseEntity<MeResponse> me() {
    var auth = SecurityContextHolder.getContext()
        .getAuthentication();

    if (auth == null || auth instanceof AnonymousAuthenticationToken) {
      return ResponseEntity.status(401)
          .build();
    }

    var userUuid = String.valueOf(auth.getPrincipal());
    var roles = auth.getAuthorities()
        .stream()
        .map(a -> a.getAuthority()
            .replace("ROLE_", ""))
        .collect(java.util.stream.Collectors.toSet());

    return ResponseEntity.ok(new MeResponse(userUuid, roles));
  }
}
