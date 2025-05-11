package com.mycompany.supplier.grpc;

import com.mycompany.productSupplier.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Component;

@Component
public class ProductServiceClient {
  private static final String PRODUCT_SERVICE_HOST = "product-service";
  private static final int PRODUCT_SERVICE_PORT = 6568;

  private final ProductServiceGrpc.ProductServiceBlockingStub blockingStub;
  private final ManagedChannel channel;

  public ProductServiceClient() {
    channel =
        ManagedChannelBuilder.forAddress(PRODUCT_SERVICE_HOST, PRODUCT_SERVICE_PORT)
            .usePlaintext()
            .build();
    blockingStub = ProductServiceGrpc.newBlockingStub(channel);
  }

  public AddProductResponse addProduct(Product product) {
    AddProductRequest request = AddProductRequest.newBuilder().setProduct(product).build();
    return blockingStub.addProduct(request);
  }

  public DeleteProductResponse deleteProduct(String supplierId, String productId) {
    DeleteProductRequest request =
        DeleteProductRequest.newBuilder().setIdSupplier(supplierId).setIdProduct(productId).build();
    return blockingStub.deleteProduct(request);
  }

  public void shutdown() {
    if (channel != null && !channel.isShutdown()) {
      channel.shutdown();
    }
  }
}
