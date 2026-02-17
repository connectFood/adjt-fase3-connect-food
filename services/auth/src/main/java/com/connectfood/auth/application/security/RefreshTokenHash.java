package com.connectfood.auth.application.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class RefreshTokenHash {

  private RefreshTokenHash() {
  }

  public static String sha256(String value) {
    try {
      var md = MessageDigest.getInstance("SHA-256");
      var bytes = md.digest(value.getBytes(StandardCharsets.UTF_8));

      var sb = new StringBuilder();
      for (byte b : bytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();

    } catch (Exception e) {
      throw new IllegalStateException("Unable to hash refresh token", e);
    }
  }
}
