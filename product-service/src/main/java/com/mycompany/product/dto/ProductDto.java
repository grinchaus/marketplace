package com.mycompany.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

  @NotBlank(message = "Supplier ID is required")
  private String idSupplier;

  @NotBlank(message = "Product ID is required")
  private String idProduct;

  @NotBlank(message = "Product name is required")
  private String name;

  @NotBlank(message = "Product description is required")
  private String description;

  @Positive(message = "Price must be positive")
  private double price;

  @NotBlank(message = "Category is required")
  private String category;
}
