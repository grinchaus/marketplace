package com.mycompany.product.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mycompany.product.grpc.AddProductRequest;
import com.mycompany.product.grpc.AddProductResponse;
import com.mycompany.product.grpc.DeleteProductRequest;
import com.mycompany.product.grpc.DeleteProductResponse;
import com.mycompany.product.grpc.GetAllProductsRequest;
import com.mycompany.product.grpc.GetAllProductsResponse;
import com.mycompany.product.grpc.GetProductsByCriteriaRequest;
import com.mycompany.product.grpc.GetProductsByCriteriaResponse;
import com.mycompany.product.model.Product;
import com.mycompany.product.repository.ProductRepository;
import io.grpc.stub.StreamObserver;
import java.util.Arrays;
import java.util.List;
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

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

  @Mock private ProductRepository productRepository;

  @InjectMocks private ProductServiceImpl productService;

  private com.mycompany.product.grpc.Product grpcProduct;

  @BeforeEach
  public void setUp() {
    grpcProduct =
        com.mycompany.product.grpc.Product.newBuilder()
            .setIdProduct("p123")
            .setIdSupplier("s456")
            .setName("Test Product")
            .setDescription("This is a test product")
            .setPrice(99.99)
            .setCategory("Test Category")
            .build();
  }

  @Test
  @DisplayName("addProduct: Успешное добавление продукта")
  public void testAddProduct() {
    AddProductRequest request = AddProductRequest.newBuilder().setProduct(grpcProduct).build();

    TestStreamObserver<AddProductResponse> streamObserver = new TestStreamObserver<>();

    productService.addProduct(request, streamObserver);

    ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
    verify(productRepository, times(1)).save(captor.capture());
    Product savedProduct = captor.getValue();
    assertEquals("p123", savedProduct.getIdProduct());
    assertEquals("s456", savedProduct.getIdSupplier());
    assertEquals("Test Product", savedProduct.getName());
    assertEquals("This is a test product", savedProduct.getDescription());
    assertEquals(99.99, savedProduct.getPrice());
    assertEquals("Test Category", savedProduct.getCategory());

    AddProductResponse response = streamObserver.getResponse();
    assertNotNull(response);
    assertEquals("SUCCESS", response.getStatus());
  }

  @Test
  @DisplayName("deleteProduct: Успешное удаление продукта")
  public void testDeleteProduct_Success() {
    Product existingProduct =
        Product.builder()
            .idProduct("p123")
            .idSupplier("s456")
            .name("Test Product")
            .description("This is a test product")
            .price(99.99)
            .category("Test Category")
            .build();
    when(productRepository.findById("p123")).thenReturn(Optional.of(existingProduct));

    DeleteProductRequest request =
        DeleteProductRequest.newBuilder().setIdSupplier("s456").setIdProduct("p123").build();

    TestStreamObserver<DeleteProductResponse> streamObserver = new TestStreamObserver<>();
    productService.deleteProduct(request, streamObserver);

    verify(productRepository, times(1)).delete(existingProduct);

    DeleteProductResponse response = streamObserver.getResponse();
    assertNotNull(response);
    assertEquals("SUCCESS", response.getStatus());
  }

  @Test
  @DisplayName("deleteProduct: Неуспешное удаление продукта (не совпадает supplierId)")
  public void testDeleteProduct_Failure() {
    Product existingProduct =
        Product.builder()
            .idProduct("p123")
            .idSupplier("otherSupplier")
            .name("Test Product")
            .description("This is a test product")
            .price(99.99)
            .category("Test Category")
            .build();
    when(productRepository.findById("p123")).thenReturn(Optional.of(existingProduct));

    DeleteProductRequest request =
        DeleteProductRequest.newBuilder().setIdSupplier("s456").setIdProduct("p123").build();

    TestStreamObserver<DeleteProductResponse> streamObserver = new TestStreamObserver<>();
    productService.deleteProduct(request, streamObserver);

    verify(productRepository, never()).delete(any());

    DeleteProductResponse response = streamObserver.getResponse();
    assertNotNull(response);
    assertEquals("FAILURE", response.getStatus());
  }

  @Test
  @DisplayName("getAllProducts: Возвращает список всех продуктов")
  public void testGetAllProducts() {
    Product product1 =
        Product.builder()
            .idProduct("p123")
            .idSupplier("s456")
            .name("Test Product")
            .description("This is a test product")
            .price(99.99)
            .category("Test Category")
            .build();
    Product product2 =
        Product.builder()
            .idProduct("p124")
            .idSupplier("s457")
            .name("Another Product")
            .description("Another test product")
            .price(49.99)
            .category("Another Category")
            .build();

    when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

    GetAllProductsRequest request = GetAllProductsRequest.newBuilder().build();

    TestStreamObserver<GetAllProductsResponse> streamObserver = new TestStreamObserver<>();
    productService.getAllProducts(request, streamObserver);

    GetAllProductsResponse response = streamObserver.getResponse();
    assertNotNull(response);
    List<com.mycompany.product.grpc.Product> grpcProducts = response.getProductsList();
    assertEquals(2, grpcProducts.size());
  }

  @Test
  @DisplayName("getProductsByCriteria: Возвращает продукты по критериям")
  public void testGetProductsByCriteria() {
    Product product1 =
        Product.builder()
            .idProduct("p123")
            .idSupplier("s456")
            .name("Test Product")
            .description("This is a test product")
            .price(99.99)
            .category("Test Category")
            .build();
    Product product2 =
        Product.builder()
            .idProduct("p124")
            .idSupplier("s457")
            .name("Another Product")
            .description("Another test product")
            .price(49.99)
            .category("Another Category")
            .build();

    when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

    GetProductsByCriteriaRequest request =
        GetProductsByCriteriaRequest.newBuilder()
            .setCategory("Test Category")
            .setName("Test")
            .build();

    TestStreamObserver<GetProductsByCriteriaResponse> streamObserver = new TestStreamObserver<>();
    productService.getProductsByCriteria(request, streamObserver);

    GetProductsByCriteriaResponse response = streamObserver.getResponse();
    assertNotNull(response);
    List<com.mycompany.product.grpc.Product> grpcProducts = response.getProductsList();
    assertEquals(1, grpcProducts.size());
    assertEquals("p123", grpcProducts.getFirst().getIdProduct());
  }
}
