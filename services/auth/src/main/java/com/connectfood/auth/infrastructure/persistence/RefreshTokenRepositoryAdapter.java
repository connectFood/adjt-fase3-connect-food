package com.connectfood.auth.infrastructure.persistence;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import com.connectfood.auth.domain.port.RefreshTokenRepositoryPort;
import com.connectfood.auth.infrastructure.persistence.entity.RefreshTokenEntity;
import com.connectfood.auth.infrastructure.persistence.repository.JpaRefreshTokenRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

  private final JpaRefreshTokenRepository jpa;

  public RefreshTokenRepositoryAdapter(JpaRefreshTokenRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  @Transactional
  public UUID save(UUID userUuid, String tokenHash, Instant expiresAt) {
    var entity = new RefreshTokenEntity();
    entity.setUuid(UUID.randomUUID());
    entity.setUserUuid(userUuid);
    entity.setTokenHash(tokenHash);
    entity.setExpiresAt(OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC));
    entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
    var saved = jpa.save(entity);
    return saved.getUuid();
  }

  @Override
  public Optional<RefreshTokenRecord> findValidByTokenHash(String tokenHash, Instant now) {
    var nowDt = OffsetDateTime.ofInstant(now, ZoneOffset.UTC);
    return jpa.findValidByHash(tokenHash, nowDt)
        .map(rt -> new RefreshTokenRecord(
            rt.getUuid(),
            rt.getUserUuid(),
            rt.getExpiresAt()
                .toInstant()
        ));
  }

  @Override
  @Transactional
  public void revoke(UUID refreshTokenUuid, Instant revokedAt) {
    var entity = jpa.findByUuid(refreshTokenUuid)
        .orElseThrow();
    entity.setRevokedAt(OffsetDateTime.ofInstant(revokedAt, ZoneOffset.UTC));
    jpa.save(entity);
  }
}
