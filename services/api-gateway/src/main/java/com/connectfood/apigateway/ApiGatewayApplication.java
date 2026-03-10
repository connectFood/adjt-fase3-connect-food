package com.connectfood.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {

  public static void main(String[] args) {
    // Avoid Netty usage of terminally deprecated Unsafe methods on newer JDKs.
    System.setProperty("io.netty.noUnsafe", "true");
    SpringApplication.run(ApiGatewayApplication.class, args);
  }
}
