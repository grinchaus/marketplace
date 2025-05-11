package com.mycompany.order.service;

import com.mycompany.order.grpc.CancelOrderRequest;
import com.mycompany.order.grpc.CancelOrderResponse;
import com.mycompany.order.grpc.CreateOrderRequest;
import com.mycompany.order.grpc.CreateOrderResponse;
import com.mycompany.order.grpc.OrderServiceGrpc;
import com.mycompany.order.model.Order;
import com.mycompany.order.repository.OrderRepository;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class OrderServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {

  private final OrderRepository orderRepository;

  public OrderServiceImpl(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Override
  public void createOrder(
      CreateOrderRequest request, StreamObserver<CreateOrderResponse> responseObserver) {
    if (request == null || request.getUserId().isEmpty() || request.getProductIdsCount() == 0) {
      responseObserver.onError(
          new IllegalArgumentException("Invalid order request: missing userId or productIds"));
      return;
    }

    String generatedId = UUID.randomUUID().toString();

    Order order = new Order();
    order.setId(generatedId);
    order.setUserId(request.getUserId());
    order.setItems(new ArrayList<>(request.getProductIdsList()));

    orderRepository.save(order);

    CreateOrderResponse response =
        CreateOrderResponse.newBuilder()
            .setOrderId(generatedId)
            .setStatus("Order created successfully")
            .build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void cancelOrder(
      CancelOrderRequest request, StreamObserver<CancelOrderResponse> responseObserver) {
    if (request == null || request.getOrderId().isEmpty() || request.getUserId().isEmpty()) {
      responseObserver.onError(
          new IllegalArgumentException("Invalid cancel order request: missing orderId or userId"));
      return;
    }

    Optional<Order> optionalOrder = orderRepository.findById(request.getOrderId());
    if (optionalOrder.isEmpty()) {
      responseObserver.onError(new IllegalArgumentException("Order not found"));
      return;
    }

    Order order = optionalOrder.get();

    orderRepository.delete(order);

    CancelOrderResponse response =
        CancelOrderResponse.newBuilder().setStatus("Order cancelled successfully").build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
