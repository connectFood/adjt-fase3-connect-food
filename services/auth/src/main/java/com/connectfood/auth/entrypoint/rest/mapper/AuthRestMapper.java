package com.connectfood.auth.entrypoint.rest.mapper;

import com.connectfood.auth.application.dto.LoginInput;
import com.connectfood.auth.application.dto.RefreshInput;
import com.connectfood.auth.application.dto.RegisterUserInput;
import com.connectfood.auth.application.dto.RegisterUserOutput;
import com.connectfood.auth.entrypoint.rest.dto.LoginRequest;
import com.connectfood.auth.entrypoint.rest.dto.RefreshRequest;
import com.connectfood.auth.entrypoint.rest.dto.RegisterRequest;
import com.connectfood.auth.entrypoint.rest.dto.RegisterResponse;

public final class AuthRestMapper {

  private AuthRestMapper() {
  }

  public static RegisterUserInput toInput(RegisterRequest request) {
    return new RegisterUserInput(request.name(), request.email(), request.password(), request.roleName());
  }

  public static RegisterResponse toResponse(RegisterUserOutput output) {
    return new RegisterResponse(output.uuid(), output.name(), output.email(), output.role());
  }

  public static LoginInput toInput(LoginRequest request) {
    return new LoginInput(request.email(), request.password());
  }

  public static RefreshInput toInput(RefreshRequest request) {
    return new RefreshInput(request.refreshToken());
  }
}
