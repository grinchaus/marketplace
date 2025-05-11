package com.mycompany.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ClientTypeActionFilter implements GlobalFilter, Ordered {

  private static final Logger logger = LoggerFactory.getLogger(ClientTypeActionFilter.class);

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String clientType = exchange.getRequest().getHeaders().getFirst("X-Client-Type");
    if (clientType == null) {
      logger.warn("Отсутствует заголовок X-Client-Type. Запрос заблокирован.");
      exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
      return exchange.getResponse().setComplete();
    }

    if ("user".equalsIgnoreCase(clientType)) {
      logger.info("Обрабатывается запрос от пользователя.");
      ServerHttpRequest modifiedRequest =
          exchange.getRequest().mutate().header("X-Processed-By", "UserActionFilter").build();
      exchange = exchange.mutate().request(modifiedRequest).build();
    } else if ("supplier".equalsIgnoreCase(clientType)) {
      logger.info("Обрабатывается запрос от поставщика.");
      ServerHttpRequest modifiedRequest =
          exchange.getRequest().mutate().header("X-Processed-By", "SupplierActionFilter").build();
      exchange = exchange.mutate().request(modifiedRequest).build();
    } else {
      logger.warn("Неизвестный тип клиента: {}", clientType);
      exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
      return exchange.getResponse().setComplete();
    }

    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
