package com.connectfood.auth.domain.port;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {
  UUID save(UUID userUuid, String tokenHash, Instant expiresAt);

  Optional<RefreshTokenRecord> findValidByTokenHash(String tokenHash, Instant now);

  void revoke(UUID refreshTokenUuid, Instant revokedAt);

  record RefreshTokenRecord(UUID uuid, UUID userUuid, Instant expiresAt) {
  }
}
