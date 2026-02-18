package com.connectfood.auth.infrastructure.persistence.entity;

import com.connectfood.auth.infrastructure.persistence.entity.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roles", schema = "auth")
public class RoleEntity extends BaseEntity {

  @Column(nullable = false, unique = true, length = 50)
  private String name;

  @Column()
  private String description;
}
