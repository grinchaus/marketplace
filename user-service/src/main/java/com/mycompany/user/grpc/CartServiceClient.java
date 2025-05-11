package com.mycompany.user.grpc;

import com.mycompany.cartUser.AddItemRequest;
import com.mycompany.cartUser.AddItemResponse;
import com.mycompany.cartUser.CartServiceGrpc;
import com.mycompany.cartUser.GetCartItemsRequest;
import com.mycompany.cartUser.GetCartItemsResponse;
import com.mycompany.cartUser.RemoveItemRequest;
import com.mycompany.cartUser.RemoveItemResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Component;

@Component
public class CartServiceClient {

  private static final String CART_SERVICE_HOST = "cart-service";
  private static final int CART_SERVICE_PORT = 6566;

  private final CartServiceGrpc.CartServiceBlockingStub blockingStub;
  private final ManagedChannel channel;

  public CartServiceClient() {
    channel =
        ManagedChannelBuilder.forAddress(CART_SERVICE_HOST, CART_SERVICE_PORT)
            .usePlaintext()
            .build();
    blockingStub = CartServiceGrpc.newBlockingStub(channel);
  }

  public AddItemResponse addItemToCart(String userId, String productId, int quantity) {
    AddItemRequest request =
        AddItemRequest.newBuilder()
            .setUserId(userId)
            .setProductId(productId)
            .setQuantity(quantity)
            .build();
    return blockingStub.addItemToCart(request);
  }

  public GetCartItemsResponse getCartItems(String userId) {
    GetCartItemsRequest request = GetCartItemsRequest.newBuilder().setUserId(userId).build();
    return blockingStub.getCartItems(request);
  }

  public RemoveItemResponse removeItemFromCart(String userId, String productId, int quantity) {
    RemoveItemRequest request =
        RemoveItemRequest.newBuilder()
            .setUserId(userId)
            .setProductId(productId)
            .setQuantity(quantity)
            .build();
    return blockingStub.removeItemFromCart(request);
  }

  public void shutdown() {
    if (channel != null && !channel.isShutdown()) {
      channel.shutdown();
    }
  }
}
