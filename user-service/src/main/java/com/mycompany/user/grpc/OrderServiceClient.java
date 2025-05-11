package com.mycompany.user.grpc;

import com.mycompany.orderUser.CancelOrderRequest;
import com.mycompany.orderUser.CancelOrderResponse;
import com.mycompany.orderUser.CreateOrderRequest;
import com.mycompany.orderUser.CreateOrderResponse;
import com.mycompany.orderUser.OrderServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderServiceClient {
  private static final String ORDER_SERVICE_HOST = "order-service";
  private static final int ORDER_SERVICE_PORT = 6567;

  private final OrderServiceGrpc.OrderServiceBlockingStub blockingStub;
  private final ManagedChannel channel;

  public OrderServiceClient() {
    channel =
        ManagedChannelBuilder.forAddress(ORDER_SERVICE_HOST, ORDER_SERVICE_PORT)
            .usePlaintext()
            .build();
    blockingStub = OrderServiceGrpc.newBlockingStub(channel);
  }

  public CreateOrderResponse createOrder(String userId, List<String> productsId) {
    CreateOrderRequest request =
        CreateOrderRequest.newBuilder().setUserId(userId).addAllProductIds(productsId).build();
    return blockingStub.createOrder(request);
  }

  public CancelOrderResponse cancelOrder(String orderId, String userId) {
    CancelOrderRequest request =
        CancelOrderRequest.newBuilder().setOrderId(orderId).setUserId(userId).build();
    return blockingStub.cancelOrder(request);
  }

  public void shutdown() {
    if (channel != null && !channel.isShutdown()) {
      channel.shutdown();
    }
  }
}
