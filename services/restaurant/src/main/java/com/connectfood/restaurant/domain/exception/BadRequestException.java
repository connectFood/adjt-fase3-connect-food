package com.connectfood.restaurant.domain.exception;

public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}
