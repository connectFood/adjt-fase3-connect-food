package com.connectfood.apigateway.infrastructure.config;

import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;

@Configuration
public class JwtDecoderConfig {

  @Bean
  public ReactiveJwtDecoder jwtDecoder(
      @Value("${security.jwt.secret}") String secret,
      @Value("${security.jwt.issuer-uri}") String issuer
  ) {
    var secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    var decoder = NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    var validator = new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer));
    decoder.setJwtValidator(validator);
    return decoder;
  }
}
