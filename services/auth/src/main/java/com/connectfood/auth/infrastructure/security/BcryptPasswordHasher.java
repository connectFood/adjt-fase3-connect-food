package com.connectfood.auth.infrastructure.security;

import com.connectfood.auth.application.security.PasswordHasher;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasher implements PasswordHasher {

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  @Override
  public String hash(final String raw) {
    return encoder.encode(raw);
  }

  @Override
  public boolean matches(final String raw, final String hashed) {
    return encoder.matches(raw, hashed);
  }
}
