package com.mycompany.cart.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {

  @NotBlank(message = "User ID is required")
  private String userId;

  @NotBlank(message = "Items is required")
  private Map<String, Integer> items = new HashMap<>();
}
