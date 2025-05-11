package com.mycompany.user.controller;

import com.mycompany.cartUser.AddItemResponse;
import com.mycompany.cartUser.CartItem;
import com.mycompany.cartUser.GetCartItemsResponse;
import com.mycompany.cartUser.RemoveItemResponse;
import com.mycompany.orderUser.CancelOrderResponse;
import com.mycompany.orderUser.CreateOrderResponse;
import com.mycompany.productUser.GetAllProductsResponse;
import com.mycompany.productUser.GetProductsByCriteriaResponse;
import com.mycompany.user.grpc.CartServiceClient;
import com.mycompany.user.grpc.OrderServiceClient;
import com.mycompany.user.grpc.ProductServiceClient;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/integrations")
public class IntegrationController {

  private final ProductServiceClient productServiceClient;
  private final CartServiceClient cartServiceClient;
  private final OrderServiceClient orderServiceClient;

  public IntegrationController(
      ProductServiceClient productServiceClient,
      CartServiceClient cartServiceClient,
      OrderServiceClient orderServiceClient) {
    this.productServiceClient = productServiceClient;
    this.cartServiceClient = cartServiceClient;
    this.orderServiceClient = orderServiceClient;
  }

  @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GetAllProductsResponse> getAllProducts() {
    GetAllProductsResponse response = productServiceClient.getAllProducts();
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/products/search", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GetProductsByCriteriaResponse> getProductsByCriteria(
      @RequestParam String category, @RequestParam String name) {
    GetProductsByCriteriaResponse response =
        productServiceClient.getProductsByCriteria(category, name);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/cart", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GetCartItemsResponse> getCartItems(@RequestParam String userId) {
    GetCartItemsResponse response = cartServiceClient.getCartItems(userId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/cart/add")
  public ResponseEntity<AddItemResponse> addItemToCart(
      @RequestParam String userId, @RequestParam String productId, @RequestParam int quantity) {
    AddItemResponse response = cartServiceClient.addItemToCart(userId, productId, quantity);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/cart/remove")
  public ResponseEntity<RemoveItemResponse> removeItemFromCart(
      @RequestParam String userId, @RequestParam String productId, @RequestParam int quantity) {
    RemoveItemResponse response = cartServiceClient.removeItemFromCart(userId, productId, quantity);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/order")
  public ResponseEntity<CreateOrderResponse> createOrder(@RequestParam String userId) {
    GetCartItemsResponse cartResponse = cartServiceClient.getCartItems(userId);
    List<String> productIds =
        cartResponse
            .getItemsList()
            .stream()
            .map(CartItem::getProductId)
            .collect(Collectors.toList());
    CreateOrderResponse orderResponse = orderServiceClient.createOrder(userId, productIds);
    cartResponse
        .getItemsList()
        .forEach(
            item -> {
              cartServiceClient.removeItemFromCart(userId, item.getProductId(), item.getQuantity());
            });
    return ResponseEntity.ok(orderResponse);
  }

  @PostMapping("/order/cancel")
  public ResponseEntity<CancelOrderResponse> cancelOrder(
      @RequestParam String orderId, @RequestParam String userId) {
    CancelOrderResponse response = orderServiceClient.cancelOrder(orderId, userId);
    return ResponseEntity.ok(response);
  }
}
