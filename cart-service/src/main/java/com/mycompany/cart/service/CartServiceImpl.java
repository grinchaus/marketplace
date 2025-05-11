package com.mycompany.cart.service;

import com.mycompany.cart.grpc.AddItemRequest;
import com.mycompany.cart.grpc.AddItemResponse;
import com.mycompany.cart.grpc.CartItem;
import com.mycompany.cart.grpc.CartServiceGrpc;
import com.mycompany.cart.grpc.GetCartItemsRequest;
import com.mycompany.cart.grpc.GetCartItemsResponse;
import com.mycompany.cart.grpc.RemoveItemRequest;
import com.mycompany.cart.grpc.RemoveItemResponse;
import com.mycompany.cart.model.Cart;
import com.mycompany.cart.repository.CartRepository;
import io.grpc.stub.StreamObserver;
import java.util.Optional;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class CartServiceImpl extends CartServiceGrpc.CartServiceImplBase {

  private final CartRepository cartRepository;

  public CartServiceImpl(CartRepository cartRepository) {
    this.cartRepository = cartRepository;
  }

  @Override
  public void addItemToCart(
      AddItemRequest request, StreamObserver<AddItemResponse> responseObserver) {
    String userId = request.getUserId();
    String productId = request.getProductId();
    int quantity = request.getQuantity();

    Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
    Cart cart =
        optionalCart.orElseGet(
            () -> {
              Cart newCart = new Cart();
              newCart.setId(userId);
              newCart.setUserId(userId);
              return newCart;
            });

    cart.getItems().merge(productId, quantity, Integer::sum);
    cartRepository.save(cart);

    AddItemResponse response =
        AddItemResponse.newBuilder().setStatus("Item added successfully").build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getCartItems(
      GetCartItemsRequest request, StreamObserver<GetCartItemsResponse> responseObserver) {
    String userId = request.getUserId();

    GetCartItemsResponse.Builder responseBuilder = GetCartItemsResponse.newBuilder();
    Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
    if (optionalCart.isPresent()) {
      Cart cart = optionalCart.get();
      cart.getItems()
          .forEach(
              (prodId, qty) -> {
                CartItem item = CartItem.newBuilder().setProductId(prodId).setQuantity(qty).build();
                responseBuilder.addItems(item);
              });
    }
    responseObserver.onNext(responseBuilder.build());
    responseObserver.onCompleted();
  }

  @Override
  public void removeItemFromCart(
      RemoveItemRequest request, StreamObserver<RemoveItemResponse> responseObserver) {
    String userId = request.getUserId();
    String productId = request.getProductId();
    int quantity = request.getQuantity();

    String status;
    Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
    if (optionalCart.isPresent()) {
      Cart cart = optionalCart.get();
      Integer currentQuantity = cart.getItems().get(productId);
      if (currentQuantity != null) {
        if (currentQuantity <= quantity) {
          cart.getItems().remove(productId);
        } else {
          cart.getItems().put(productId, currentQuantity - quantity);
        }
        cartRepository.save(cart);
        status = "Item removed successfully";
      } else {
        status = "Item not found in cart";
      }
    } else {
      status = "Cart not found for user";
    }

    RemoveItemResponse response = RemoveItemResponse.newBuilder().setStatus(status).build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
