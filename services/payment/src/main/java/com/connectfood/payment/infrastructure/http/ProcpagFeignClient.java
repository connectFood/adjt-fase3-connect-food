package com.connectfood.payment.infrastructure.http;

import com.connectfood.payment.infrastructure.http.dto.ProcpagRequest;
import com.connectfood.payment.infrastructure.http.dto.ProcpagResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "procpagClient", url = "${procpag.base-url}")
public interface ProcpagFeignClient {

  @PostMapping(value = "/requisicao", consumes = "application/json")
  ProcpagResponse requisicao(@RequestBody ProcpagRequest request);
}
