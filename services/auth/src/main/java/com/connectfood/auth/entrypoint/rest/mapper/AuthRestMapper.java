package com.connectfood.auth.entrypoint.rest.mapper;

import com.connectfood.auth.application.dto.LoginInput;
import com.connectfood.auth.application.dto.RegisterUserInput;
import com.connectfood.auth.entrypoint.rest.dto.LoginRequest;
import com.connectfood.auth.entrypoint.rest.dto.RegisterRequest;

public final class AuthRestMapper {

  private AuthRestMapper() {
  }

  public static RegisterUserInput toInput(RegisterRequest request) {
    return new RegisterUserInput(request.email(), request.password(), request.roleName());
  }

  public static LoginInput toInput(LoginRequest request) {
    return new LoginInput(request.email(), request.password());
  }
}
