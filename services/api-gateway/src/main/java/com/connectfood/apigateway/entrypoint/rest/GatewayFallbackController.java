package com.connectfood.apigateway.entrypoint.rest;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class GatewayFallbackController {

  @GetMapping("/{service}")
  public ResponseEntity<ProblemDetail> fallback(@PathVariable("service") String service) {
    var detail = "Service temporarily unavailable: " + service;
    var problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, detail);
    problem.setTitle("Upstream Service Unavailable");
    problem.setType(java.net.URI.create("https://connectfood.com/problems/upstream-unavailable"));
    problem.setProperty("service", service);
    problem.setProperty("timestamp", OffsetDateTime.now().toString());
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
  }
}
