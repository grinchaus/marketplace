package com.mycompany.order.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

  @NotBlank(message = "User ID is required")
  private String userId;

  @NotBlank(message = "Items is required")
  private List<String> items = new ArrayList<>();
}
