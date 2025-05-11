package com.mycompany.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mycompany.order.grpc.CancelOrderRequest;
import com.mycompany.order.grpc.CancelOrderResponse;
import com.mycompany.order.grpc.CreateOrderRequest;
import com.mycompany.order.grpc.CreateOrderResponse;
import com.mycompany.order.model.Order;
import com.mycompany.order.repository.OrderRepository;
import io.grpc.stub.StreamObserver;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

  @Mock private OrderRepository orderRepository;

  @InjectMocks private OrderServiceImpl orderService;

  private final String validUserId = "user123";
  private final String validProductId = "prod456";

  @Getter
  private static class TestStreamObserver<T> implements StreamObserver<T> {
    private T response;
    private Throwable error;

    @Override
    public void onNext(T value) {
      this.response = value;
    }

    @Override
    public void onError(Throwable t) {
      this.error = t;
    }

    @Override
    public void onCompleted() {}
  }

  @BeforeEach
  public void setUp() {}

  @Test
  @DisplayName("createOrder: Успешное создание заказа")
  public void testCreateOrder_Success() {
    CreateOrderRequest request =
        CreateOrderRequest.newBuilder()
            .setUserId(validUserId)
            .addProductIds(validProductId)
            .build();

    TestStreamObserver<CreateOrderResponse> streamObserver = new TestStreamObserver<>();

    orderService.createOrder(request, streamObserver);

    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository, times(1)).save(orderCaptor.capture());
    Order savedOrder = orderCaptor.getValue();

    assertEquals(validUserId, savedOrder.getUserId());
    assertTrue(savedOrder.getItems().contains(validProductId));
    assertNotNull(savedOrder.getId());

    CreateOrderResponse response = streamObserver.getResponse();
    assertNotNull(response);
    assertEquals(savedOrder.getId(), response.getOrderId());
    assertEquals("Order created successfully", response.getStatus());
  }

  @Test
  @DisplayName("createOrder: Ошибка при пустом запросе")
  public void testCreateOrder_InvalidRequest() {
    CreateOrderRequest request = CreateOrderRequest.newBuilder().setUserId("").build();

    TestStreamObserver<CreateOrderResponse> streamObserver = new TestStreamObserver<>();

    orderService.createOrder(request, streamObserver);

    assertNotNull(streamObserver.getError());
    assertInstanceOf(IllegalArgumentException.class, streamObserver.getError());
  }

  @Test
  @DisplayName("cancelOrder: Успешная отмена заказа")
  public void testCancelOrder_Success() {
    String orderId = UUID.randomUUID().toString();
    Order order = new Order();
    order.setId(orderId);
    order.setUserId(validUserId);
    order.getItems().add(validProductId);

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    CancelOrderRequest request =
        CancelOrderRequest.newBuilder().setOrderId(orderId).setUserId(validUserId).build();

    TestStreamObserver<CancelOrderResponse> streamObserver = new TestStreamObserver<>();

    orderService.cancelOrder(request, streamObserver);

    verify(orderRepository, times(1)).delete(order);

    CancelOrderResponse response = streamObserver.getResponse();
    assertNotNull(response);
    assertEquals("Order cancelled successfully", response.getStatus());
  }

  @Test
  @DisplayName("cancelOrder: Ошибка при отсутствии заказа")
  public void testCancelOrder_OrderNotFound() {
    String orderId = UUID.randomUUID().toString();

    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    CancelOrderRequest request =
        CancelOrderRequest.newBuilder().setOrderId(orderId).setUserId(validUserId).build();

    TestStreamObserver<CancelOrderResponse> streamObserver = new TestStreamObserver<>();

    orderService.cancelOrder(request, streamObserver);

    assertNotNull(streamObserver.getError());
    assertInstanceOf(IllegalArgumentException.class, streamObserver.getError());
    verify(orderRepository, never()).delete(any(Order.class));
  }
}
