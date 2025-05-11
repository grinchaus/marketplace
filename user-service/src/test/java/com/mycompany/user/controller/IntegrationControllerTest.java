package com.mycompany.user.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.protobuf.UnknownFieldSet;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import com.mycompany.cartUser.AddItemResponse;
import com.mycompany.cartUser.CartItem;
import com.mycompany.cartUser.GetCartItemsResponse;
import com.mycompany.cartUser.RemoveItemResponse;
import com.mycompany.orderUser.CancelOrderResponse;
import com.mycompany.orderUser.CreateOrderResponse;
import com.mycompany.productUser.GetAllProductsResponse;
import com.mycompany.productUser.GetProductsByCriteriaResponse;
import com.mycompany.productUser.Product;
import com.mycompany.user.grpc.CartServiceClient;
import com.mycompany.user.grpc.OrderServiceClient;
import com.mycompany.user.grpc.ProductServiceClient;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class IntegrationControllerTest {

  private MockMvc mockMvc;

  @Mock private ProductServiceClient productServiceClient;

  @Mock private CartServiceClient cartServiceClient;

  @Mock private OrderServiceClient orderServiceClient;

  @InjectMocks private IntegrationController integrationController;

  @JsonIgnoreProperties({"defaultInstanceForType", "unknownFields"})
  private abstract static class UnknownFieldSetMixin {}

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new ProtobufModule());
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    objectMapper.addMixIn(UnknownFieldSet.class, UnknownFieldSetMixin.class);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    MappingJackson2HttpMessageConverter converter =
        new MappingJackson2HttpMessageConverter(objectMapper);
    converter.setSupportedMediaTypes(
        Arrays.asList(MediaType.APPLICATION_JSON, new MediaType("application", "x-protobuf")));

    mockMvc =
        MockMvcBuilders.standaloneSetup(integrationController)
            .setMessageConverters(converter)
            .build();
  }

  @Test
  @DisplayName("GET /users/integrations/products returns all products")
  void testGetAllProducts() throws Exception {
    Product p1 =
        Product.newBuilder()
            .setIdSupplier("s1")
            .setIdProduct("p1")
            .setName("Prod1")
            .setDescription("Desc1")
            .setPrice(10.0)
            .setCategory("Cat1")
            .build();
    Product p2 =
        Product.newBuilder()
            .setIdSupplier("s2")
            .setIdProduct("p2")
            .setName("Prod2")
            .setDescription("Desc2")
            .setPrice(20.0)
            .setCategory("Cat2")
            .build();

    GetAllProductsResponse response =
        GetAllProductsResponse.newBuilder().addProducts(p1).addProducts(p2).build();
    when(productServiceClient.getAllProducts()).thenReturn(response);

    mockMvc
        .perform(get("/users/integrations/products").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.products", hasSize(2)))
        .andExpect(jsonPath("$.products[0].idProduct").value("p1"))
        .andExpect(jsonPath("$.products[1].idProduct").value("p2"));
  }

  @Test
  @DisplayName("GET /users/integrations/products/search returns products by criteria")
  void testGetProductsByCriteria() throws Exception {
    String category = "Cat1";
    String name = "Prod";
    Product p =
        Product.newBuilder()
            .setIdSupplier("s1")
            .setIdProduct("p1")
            .setName("Prod1")
            .setDescription("Desc1")
            .setPrice(10.0)
            .setCategory(category)
            .build();
    GetProductsByCriteriaResponse response =
        GetProductsByCriteriaResponse.newBuilder().addProducts(p).build();
    when(productServiceClient.getProductsByCriteria(eq(category), eq(name))).thenReturn(response);

    mockMvc
        .perform(
            get("/users/integrations/products/search")
                .param("category", category)
                .param("name", name)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.products", hasSize(1)))
        .andExpect(jsonPath("$.products[0].category").value(category));
  }

  @Test
  @DisplayName("GET /users/integrations/cart returns cart items")
  void testGetCartItems() throws Exception {
    String userId = "u1";
    CartItem item = CartItem.newBuilder().setProductId("p1").setQuantity(2).build();
    GetCartItemsResponse response = GetCartItemsResponse.newBuilder().addItems(item).build();
    when(cartServiceClient.getCartItems(eq(userId))).thenReturn(response);

    mockMvc
        .perform(
            get("/users/integrations/cart")
                .param("userId", userId)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.items[0].productId").value("p1"));
  }

  @Test
  @DisplayName("POST /users/integrations/cart/add adds item to cart")
  void testAddItemToCart() throws Exception {
    String userId = "u1";
    String productId = "p1";
    int quantity = 2;
    AddItemResponse addResponse = AddItemResponse.newBuilder().setStatus("OK").build();
    when(cartServiceClient.addItemToCart(eq(userId), eq(productId), eq(quantity)))
        .thenReturn(addResponse);

    mockMvc
        .perform(
            post("/users/integrations/cart/add")
                .param("userId", userId)
                .param("productId", productId)
                .param("quantity", String.valueOf(quantity))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value("OK"));
  }

  @Test
  @DisplayName("POST /users/integrations/cart/remove removes item from cart")
  void testRemoveItemFromCart() throws Exception {
    String userId = "u1";
    String productId = "p1";
    int quantity = 1;
    RemoveItemResponse remResponse = RemoveItemResponse.newBuilder().setStatus("REMOVED").build();
    when(cartServiceClient.removeItemFromCart(eq(userId), eq(productId), eq(quantity)))
        .thenReturn(remResponse);

    mockMvc
        .perform(
            post("/users/integrations/cart/remove")
                .param("userId", userId)
                .param("productId", productId)
                .param("quantity", String.valueOf(quantity))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value("REMOVED"));
  }

  @Test
  @DisplayName("POST /users/integrations/order creates an order and clears cart")
  void testCreateOrder() throws Exception {
    String userId = "u1";
    CartItem item1 = CartItem.newBuilder().setProductId("p1").setQuantity(2).build();
    CartItem item2 = CartItem.newBuilder().setProductId("p2").setQuantity(3).build();
    GetCartItemsResponse cartResponse =
        GetCartItemsResponse.newBuilder().addItems(item1).addItems(item2).build();
    when(cartServiceClient.getCartItems(eq(userId))).thenReturn(cartResponse);

    CreateOrderResponse orderResponse =
        CreateOrderResponse.newBuilder().setOrderId("o1").setStatus("CREATED").build();
    when(orderServiceClient.createOrder(eq(userId), eq(Arrays.asList("p1", "p2"))))
        .thenReturn(orderResponse);

    mockMvc
        .perform(
            post("/users/integrations/order")
                .param("userId", userId)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.orderId").value("o1"))
        .andExpect(jsonPath("$.status").value("CREATED"));

    verify(cartServiceClient).removeItemFromCart(eq(userId), eq("p1"), eq(2));
    verify(cartServiceClient).removeItemFromCart(eq(userId), eq("p2"), eq(3));
  }

  @Test
  @DisplayName("POST /users/integrations/order/cancel cancels an order")
  void testCancelOrder() throws Exception {
    String orderId = "o1";
    String userId = "u1";
    CancelOrderResponse cancelResponse =
        CancelOrderResponse.newBuilder().setStatus("CANCELLED").build();
    when(orderServiceClient.cancelOrder(eq(orderId), eq(userId))).thenReturn(cancelResponse);

    mockMvc
        .perform(
            post("/users/integrations/order/cancel")
                .param("orderId", orderId)
                .param("userId", userId)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value("CANCELLED"));
  }
}
