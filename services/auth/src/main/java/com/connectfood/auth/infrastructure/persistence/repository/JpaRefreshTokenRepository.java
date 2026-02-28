package com.connectfood.auth.infrastructure.persistence.repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import com.connectfood.auth.infrastructure.persistence.entity.RefreshTokenEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

  @Query("""
      select rt from RefreshTokenEntity rt
      where rt.tokenHash = :hash
        and rt.revokedAt is null
        and rt.expiresAt > :now
      """)
  Optional<RefreshTokenEntity> findValidByHash(
      @Param("hash") String hash,
      @Param("now") OffsetDateTime now
  );

  Optional<RefreshTokenEntity> findByUuid(UUID uuid);
}
