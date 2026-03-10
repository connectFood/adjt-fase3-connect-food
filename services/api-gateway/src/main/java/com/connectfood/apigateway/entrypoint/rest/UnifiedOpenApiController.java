package com.connectfood.apigateway.entrypoint.rest;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@RestController
public class UnifiedOpenApiController {

  private final WebClient webClient;
  private final ObjectMapper objectMapper;
  private final String authUrl;
  private final String orderUrl;
  private final String paymentUrl;
  private final String restaurantUrl;

  public UnifiedOpenApiController(
      WebClient.Builder webClientBuilder,
      @Value("${services.auth-url}") String authUrl,
      @Value("${services.order-url}") String orderUrl,
      @Value("${services.payment-url}") String paymentUrl,
      @Value("${services.restaurant-url}") String restaurantUrl
  ) {
    this.webClient = webClientBuilder.build();
    this.objectMapper = new ObjectMapper().findAndRegisterModules();
    this.authUrl = authUrl;
    this.orderUrl = orderUrl;
    this.paymentUrl = paymentUrl;
    this.restaurantUrl = restaurantUrl;
  }

  @GetMapping(value = "/v3/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<String>> unifiedOpenApi() {
    return Mono.zip(
            fetch("auth", authUrl),
            fetch("order", orderUrl),
            fetch("payment", paymentUrl),
            fetch("restaurant", restaurantUrl)
        )
        .map(tuple -> merge(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4()))
        .map(this::toJson)
        .map(json -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json))
        .onErrorReturn(
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(minimalValidOpenApiJson())
        );
  }

  private Mono<ServiceSpec> fetch(String serviceName, String baseUrl) {
    return webClient.get()
        .uri(baseUrl + "/v3/api-docs")
        .retrieve()
        .bodyToMono(String.class)
        .map(body -> new ServiceSpec(serviceName, parse(body)))
        .onErrorReturn(new ServiceSpec(serviceName, objectMapper.createObjectNode()));
  }

  private ObjectNode parse(String body) {
    try {
      return (ObjectNode) objectMapper.readTree(body);
    } catch (Exception e) {
      return objectMapper.createObjectNode();
    }
  }

  private ObjectNode merge(ServiceSpec... specs) {
    ObjectNode merged = objectMapper.createObjectNode();
    merged.put("openapi", "3.0.1");

    ObjectNode info = objectMapper.createObjectNode();
    info.put("title", "ConnectFood - Unified API");
    info.put("version", "v1");
    merged.set("info", info);

    ObjectNode paths = objectMapper.createObjectNode();
    ObjectNode components = objectMapper.createObjectNode();
    ArrayNode tags = objectMapper.createArrayNode();
    Set<String> tagNames = new LinkedHashSet<>();

    for (ServiceSpec spec : specs) {
      ObjectNode node = spec.spec().deepCopy();
      rewriteRefs(node, spec.serviceName());
      mergePaths(paths, node.path("paths"));
      mergeComponents(components, node.path("components"), spec.serviceName());
      mergeTags(tags, tagNames, node.path("tags"));
    }

    merged.set("paths", paths);
    merged.set("components", components);
    merged.set("tags", tags);
    return merged;
  }

  private String toJson(ObjectNode node) {
    try {
      return objectMapper.writeValueAsString(node);
    } catch (Exception e) {
      return minimalValidOpenApiJson();
    }
  }

  private String minimalValidOpenApiJson() {
    return """
        {"openapi":"3.0.1","info":{"title":"ConnectFood - Unified API","version":"v1"},"paths":{}}
        """;
  }

  private void mergePaths(ObjectNode target, JsonNode sourcePaths) {
    if (!sourcePaths.isObject()) {
      return;
    }

    Iterator<Map.Entry<String, JsonNode>> it = sourcePaths.fields();
    while (it.hasNext()) {
      Map.Entry<String, JsonNode> pathEntry = it.next();
      String path = pathEntry.getKey();
      ObjectNode sourceOperations = (ObjectNode) pathEntry.getValue();

      if (!target.has(path)) {
        target.set(path, sourceOperations);
        continue;
      }

      ObjectNode existingOperations = (ObjectNode) target.get(path);
      Iterator<Map.Entry<String, JsonNode>> methods = sourceOperations.fields();
      while (methods.hasNext()) {
        Map.Entry<String, JsonNode> methodEntry = methods.next();
        if (!existingOperations.has(methodEntry.getKey())) {
          existingOperations.set(methodEntry.getKey(), methodEntry.getValue());
        }
      }
    }
  }

  private void mergeComponents(ObjectNode target, JsonNode sourceComponents, String serviceName) {
    if (!sourceComponents.isObject()) {
      return;
    }

    Iterator<Map.Entry<String, JsonNode>> it = sourceComponents.fields();
    while (it.hasNext()) {
      Map.Entry<String, JsonNode> typeEntry = it.next();
      String type = typeEntry.getKey();
      JsonNode values = typeEntry.getValue();
      if (!values.isObject()) {
        continue;
      }

      ObjectNode targetType = target.with(type);
      Iterator<Map.Entry<String, JsonNode>> valueIt = values.fields();
      while (valueIt.hasNext()) {
        Map.Entry<String, JsonNode> valueEntry = valueIt.next();
        targetType.set(serviceName + "_" + valueEntry.getKey(), valueEntry.getValue());
      }
    }
  }

  private void mergeTags(ArrayNode target, Set<String> names, JsonNode sourceTags) {
    if (!sourceTags.isArray()) {
      return;
    }
    for (JsonNode tag : sourceTags) {
      String name = tag.path("name").asText();
      if (!name.isBlank() && names.add(name)) {
        target.add(tag);
      }
    }
  }

  private void rewriteRefs(JsonNode node, String serviceName) {
    if (node == null || node.isMissingNode()) {
      return;
    }

    if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;
      JsonNode refNode = objectNode.get("$ref");
      if (refNode != null && refNode.isTextual()) {
        String ref = refNode.asText();
        String marker = "#/components/";
        if (ref.startsWith(marker)) {
          String[] parts = ref.substring(marker.length()).split("/");
          if (parts.length == 2) {
            objectNode.put("$ref", marker + parts[0] + "/" + serviceName + "_" + parts[1]);
          }
        }
      }

      Iterator<JsonNode> fields = objectNode.elements();
      while (fields.hasNext()) {
        rewriteRefs(fields.next(), serviceName);
      }
    } else if (node.isArray()) {
      for (JsonNode item : node) {
        rewriteRefs(item, serviceName);
      }
    }
  }

  private record ServiceSpec(String serviceName, ObjectNode spec) {
  }
}
