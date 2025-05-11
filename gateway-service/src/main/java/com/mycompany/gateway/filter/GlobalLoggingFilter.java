package com.mycompany.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

  private static final Logger logger = LoggerFactory.getLogger(GlobalLoggingFilter.class);

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    logger.info(
        "Request received: {} {}",
        exchange.getRequest().getMethod(),
        exchange.getRequest().getURI());
    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
