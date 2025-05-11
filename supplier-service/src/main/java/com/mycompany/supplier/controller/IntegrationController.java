package com.mycompany.supplier.controller;

import com.mycompany.productSupplier.*;
import com.mycompany.supplier.grpc.ProductServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/supplier/integrations")
public class IntegrationController {

  private final ProductServiceClient productServiceClient;

  public IntegrationController(ProductServiceClient productServiceClient) {
    this.productServiceClient = productServiceClient;
  }

  @PostMapping("/products")
  public ResponseEntity<AddProductResponse> addProduct(@RequestBody Product product) {
    AddProductResponse response = productServiceClient.addProduct(product);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/products/{productID}")
  public ResponseEntity<DeleteProductResponse> deleteProduct(
      @RequestParam String supplierId, @PathVariable String productID) {
    DeleteProductResponse response = productServiceClient.deleteProduct(supplierId, productID);
    return ResponseEntity.ok(response);
  }
}
