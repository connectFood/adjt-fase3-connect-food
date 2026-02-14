package com.connectfood.auth.application.security;

public interface PasswordHasher {
  String hash(String raw);

  boolean matches(String raw, String hashed);
}
