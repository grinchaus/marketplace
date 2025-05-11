package com.mycompany.product.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
  @Id private String idProduct;

  @Column(nullable = false)
  private String idSupplier;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "price", nullable = false)
  private double price;

  @Column(name = "category", nullable = false)
  private String category;
}
