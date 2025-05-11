package com.mycompany.gateway.filter;

import com.mycompany.gateway.security.JwtTokenValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

  private final JwtTokenValidator jwtValidator;
  private static final List<String> WHITELIST =
      List.of("/auth/signup", "/auth/signin", "/auth/refreshToken");

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getPath().value();
    if (WHITELIST.stream().anyMatch(path::startsWith)) {
      return chain.filter(exchange);
    }

    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }

    String token = authHeader.substring(7);
    return jwtValidator
        .validate(token)
        .flatMap(
            claims -> {
              ServerHttpRequest mutated =
                  exchange.getRequest().mutate().header("X-Auth-User", claims.getSubject()).build();
              return chain.filter(exchange.mutate().request(mutated).build());
            })
        .onErrorResume(
            e -> {
              exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
              return exchange.getResponse().setComplete();
            });
  }

  @Override
  public int getOrder() {
    return -0;
  }
}
