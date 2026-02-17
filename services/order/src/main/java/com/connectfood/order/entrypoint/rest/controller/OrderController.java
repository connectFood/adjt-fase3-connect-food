package com.connectfood.order.entrypoint.rest.controller;

import java.util.UUID;

import com.connectfood.order.application.usecase.CreateOrderUseCase;
import com.connectfood.order.application.usecase.GetOrderByUuidUseCase;
import com.connectfood.order.application.usecase.ListOrdersByCustomerUseCase;
import com.connectfood.order.entrypoint.rest.dto.CreateOrderRequest;
import com.connectfood.order.entrypoint.rest.mapper.OrderRestMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
public class OrderController {

  private final CreateOrderUseCase createOrderUseCase;
  private final GetOrderByUuidUseCase getOrderByUuidUseCase;
  private final ListOrdersByCustomerUseCase listOrdersByCustomerUseCase;

  public OrderController(
      CreateOrderUseCase createOrderUseCase,
      GetOrderByUuidUseCase getOrderByUuidUseCase,
      ListOrdersByCustomerUseCase listOrdersByCustomerUseCase
  ) {
    this.createOrderUseCase = createOrderUseCase;
    this.getOrderByUuidUseCase = getOrderByUuidUseCase;
    this.listOrdersByCustomerUseCase = listOrdersByCustomerUseCase;
  }

  @PostMapping
  public ResponseEntity<?> create(@Valid @RequestBody CreateOrderRequest request) {
    var customerUuid = getAuthenticatedUserUuid();
    var out = createOrderUseCase.execute(customerUuid, OrderRestMapper.toInput(request));
    return ResponseEntity.status(201)
        .body(out);
  }

  @GetMapping("/{uuid}")
  public ResponseEntity<?> getByUuid(@PathVariable("uuid") UUID uuid) {
    return ResponseEntity.ok(getOrderByUuidUseCase.execute(uuid));
  }

  @GetMapping
  public ResponseEntity<?> listMine() {
    var customerUuid = getAuthenticatedUserUuid();
    return ResponseEntity.ok(listOrdersByCustomerUseCase.execute(customerUuid));
  }

  private UUID getAuthenticatedUserUuid() {
    var auth = SecurityContextHolder.getContext()
        .getAuthentication();
    return UUID.fromString(String.valueOf(auth.getPrincipal()));
  }
}
