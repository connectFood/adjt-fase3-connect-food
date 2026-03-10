package com.connectfood.restaurant.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.connectfood.restaurant.infrastructure.persistence.entity.RestaurantEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRestaurantRepository extends JpaRepository<RestaurantEntity, Long> {
  Optional<RestaurantEntity> findByUuid(UUID uuid);

  List<RestaurantEntity> findAllByOrderByNameAsc();
}
