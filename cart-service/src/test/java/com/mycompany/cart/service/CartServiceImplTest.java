package com.mycompany.cart.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mycompany.cart.grpc.AddItemRequest;
import com.mycompany.cart.grpc.AddItemResponse;
import com.mycompany.cart.grpc.GetCartItemsRequest;
import com.mycompany.cart.grpc.GetCartItemsResponse;
import com.mycompany.cart.grpc.RemoveItemRequest;
import com.mycompany.cart.grpc.RemoveItemResponse;
import com.mycompany.cart.model.Cart;
import com.mycompany.cart.repository.CartRepository;
import io.grpc.stub.StreamObserver;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Getter
class TestStreamObserver<T> implements StreamObserver<T> {
  private T response;

  @Override
  public void onNext(T value) {
    this.response = value;
  }

  @Override
  public void onError(Throwable t) {}

  @Override
  public void onCompleted() {}
}

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

  @Mock private CartRepository cartRepository;

  @InjectMocks private CartServiceImpl cartService;

  private final String userId = "user1";
  private final String productId = "prod1";

  @BeforeEach
  public void setUp() {}

  @Test
  @DisplayName("addItemToCart: Добавление нового товара в пустую корзину")
  public void testAddItemToCart_NewItem() {
    AddItemRequest request =
        AddItemRequest.newBuilder()
            .setUserId(userId)
            .setProductId(productId)
            .setQuantity(2)
            .build();

    TestStreamObserver<AddItemResponse> streamObserver = new TestStreamObserver<>();

    when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

    cartService.addItemToCart(request, streamObserver);

    ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
    verify(cartRepository, times(1)).save(cartCaptor.capture());
    Cart savedCart = cartCaptor.getValue();
    assertEquals(userId, savedCart.getUserId());
    assertTrue(savedCart.getItems().containsKey(productId));
    assertEquals(2, savedCart.getItems().get(productId).intValue());

    AddItemResponse response = streamObserver.getResponse();
    assertNotNull(response);
    assertEquals("Item added successfully", response.getStatus());
  }

  @Test
  @DisplayName("getCartItems: Получение элементов корзины для существующего пользователя")
  public void testGetCartItems() {
    Cart cart = new Cart();
    cart.setId(userId);
    cart.setUserId(userId);
    Map<String, Integer> items = new HashMap<>();
    items.put(productId, 3);
    cart.setItems(items);

    when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

    GetCartItemsRequest request = GetCartItemsRequest.newBuilder().setUserId(userId).build();

    TestStreamObserver<GetCartItemsResponse> streamObserver = new TestStreamObserver<>();

    cartService.getCartItems(request, streamObserver);

    GetCartItemsResponse response = streamObserver.getResponse();
    assertNotNull(response);
    assertEquals(1, response.getItemsCount());
    assertEquals(productId, response.getItems(0).getProductId());
    assertEquals(3, response.getItems(0).getQuantity());
  }

  @Test
  @DisplayName("removeItemFromCart: Успешное уменьшение количества товара в корзине")
  public void testRemoveItemFromCart_Success() {
    Cart cart = new Cart();
    cart.setId(userId);
    cart.setUserId(userId);
    Map<String, Integer> items = new HashMap<>();
    items.put(productId, 5);
    cart.setItems(items);

    when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

    RemoveItemRequest request =
        RemoveItemRequest.newBuilder()
            .setUserId(userId)
            .setProductId(productId)
            .setQuantity(3)
            .build();

    TestStreamObserver<RemoveItemResponse> streamObserver = new TestStreamObserver<>();

    cartService.removeItemFromCart(request, streamObserver);

    ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
    verify(cartRepository, times(1)).save(cartCaptor.capture());
    Cart savedCart = cartCaptor.getValue();
    assertEquals(2, savedCart.getItems().get(productId).intValue());

    RemoveItemResponse response = streamObserver.getResponse();
    assertNotNull(response);
    assertEquals("Item removed successfully", response.getStatus());
  }

  @Test
  @DisplayName("removeItemFromCart: Полное удаление товара из корзины при совпадении количества")
  public void testRemoveItemFromCart_RemoveComplete() {
    Cart cart = new Cart();
    cart.setId(userId);
    cart.setUserId(userId);
    Map<String, Integer> items = new HashMap<>();
    items.put(productId, 3);
    cart.setItems(items);

    when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

    RemoveItemRequest request =
        RemoveItemRequest.newBuilder()
            .setUserId(userId)
            .setProductId(productId)
            .setQuantity(3)
            .build();

    TestStreamObserver<RemoveItemResponse> streamObserver = new TestStreamObserver<>();

    cartService.removeItemFromCart(request, streamObserver);

    ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
    verify(cartRepository, times(1)).save(cartCaptor.capture());
    Cart savedCart = cartCaptor.getValue();
    assertFalse(savedCart.getItems().containsKey(productId));

    RemoveItemResponse response = streamObserver.getResponse();
    assertNotNull(response);
    assertEquals("Item removed successfully", response.getStatus());
  }

  @Test
  @DisplayName("removeItemFromCart: Ошибка удаления, когда корзина для пользователя не найдена")
  public void testRemoveItemFromCart_CartNotFound() {
    when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

    RemoveItemRequest request =
        RemoveItemRequest.newBuilder()
            .setUserId(userId)
            .setProductId(productId)
            .setQuantity(1)
            .build();

    TestStreamObserver<RemoveItemResponse> streamObserver = new TestStreamObserver<>();

    cartService.removeItemFromCart(request, streamObserver);

    verify(cartRepository, never()).save(any());

    RemoveItemResponse response = streamObserver.getResponse();
    assertNotNull(response);
    assertEquals("Cart not found for user", response.getStatus());
  }
}
