package com.connectfood.auth.infrastructure.persistence.repository.common;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

import com.connectfood.auth.infrastructure.persistence.entity.common.BaseEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseJpaRepository<T extends BaseEntity> extends JpaRepository<T, Long>, Serializable {

  Optional<T> findByUuid(UUID uuid);
}
