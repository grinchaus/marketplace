package com.mycompany.user.grpc;

import com.mycompany.productUser.GetAllProductsRequest;
import com.mycompany.productUser.GetAllProductsResponse;
import com.mycompany.productUser.GetProductsByCriteriaRequest;
import com.mycompany.productUser.GetProductsByCriteriaResponse;
import com.mycompany.productUser.ProductServiceGrpc;
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

  public GetAllProductsResponse getAllProducts() {
    GetAllProductsRequest request = GetAllProductsRequest.newBuilder().build();
    return blockingStub.getAllProducts(request);
  }

  public GetProductsByCriteriaResponse getProductsByCriteria(String category, String name) {
    GetProductsByCriteriaRequest request =
        GetProductsByCriteriaRequest.newBuilder().setCategory(category).setName(name).build();
    return blockingStub.getProductsByCriteria(request);
  }

  public void shutdown() {
    if (channel != null && !channel.isShutdown()) {
      channel.shutdown();
    }
  }
}
