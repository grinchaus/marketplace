package com.mycompany.cart.model;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("Cart")
public class Cart {

  @Id private String id;
  @Indexed private String userId;
  private Map<String, Integer> items = new HashMap<>();
}
