package com.connectfood.restaurant.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.connectfood.restaurant.domain.model.Restaurant;
import com.connectfood.restaurant.domain.port.RestaurantRepositoryPort;
import com.connectfood.restaurant.infrastructure.persistence.mapper.RestaurantInfraMapper;
import com.connectfood.restaurant.infrastructure.persistence.repository.JpaRestaurantRepository;

import org.springframework.stereotype.Component;

@Component
public class RestaurantRepositoryAdapter implements RestaurantRepositoryPort {

  private final JpaRestaurantRepository jpaRepository;

  public RestaurantRepositoryAdapter(JpaRestaurantRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Restaurant save(Restaurant restaurant) {
    var saved = jpaRepository.save(RestaurantInfraMapper.toEntity(restaurant));
    return RestaurantInfraMapper.toDomain(saved);
  }

  @Override
  public Optional<Restaurant> findByUuid(UUID uuid) {
    return jpaRepository.findByUuid(uuid)
        .map(RestaurantInfraMapper::toDomain);
  }

  @Override
  public List<Restaurant> findAll() {
    return jpaRepository.findAllByOrderByNameAsc()
        .stream()
        .map(RestaurantInfraMapper::toDomain)
        .toList();
  }
}
