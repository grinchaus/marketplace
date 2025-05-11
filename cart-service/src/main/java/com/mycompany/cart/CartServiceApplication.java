package com.mycompany.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableRedisRepositories(basePackages = "com.mycompany.cart.repository")
public class CartServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(CartServiceApplication.class, args);
  }
}
