package com.mycompany.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ErrorHandlingFilter implements GlobalFilter, Ordered {
  private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingFilter.class);
  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return chain
        .filter(exchange)
        .onErrorResume(
            ex -> {
              logger.error("Exception caught in gateway", ex);

              Map<String, Object> errorBody =
                  Map.of(
                      "timestamp", Instant.now().toString(),
                      "path", exchange.getRequest().getPath().value(),
                      "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                      "error", ex.getClass().getSimpleName(),
                      "message", ex.getMessage());
              byte[] bytes;
              try {
                bytes = mapper.writeValueAsBytes(errorBody);
              } catch (Exception je) {
                bytes = ("{\"error\":\"SerializationFailure\"}").getBytes(StandardCharsets.UTF_8);
              }

              exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
              exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
              DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
              return exchange.getResponse().writeWith(Mono.just(buffer));
            });
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}
