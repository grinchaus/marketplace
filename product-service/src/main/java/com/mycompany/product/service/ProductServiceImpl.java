package com.mycompany.product.service;

import com.mycompany.product.grpc.AddProductRequest;
import com.mycompany.product.grpc.AddProductResponse;
import com.mycompany.product.grpc.DeleteProductRequest;
import com.mycompany.product.grpc.DeleteProductResponse;
import com.mycompany.product.grpc.GetAllProductsRequest;
import com.mycompany.product.grpc.GetAllProductsResponse;
import com.mycompany.product.grpc.GetProductsByCriteriaRequest;
import com.mycompany.product.grpc.GetProductsByCriteriaResponse;
import com.mycompany.product.grpc.ProductServiceGrpc;
import com.mycompany.product.model.Product;
import com.mycompany.product.repository.ProductRepository;
import io.grpc.stub.StreamObserver;
import java.util.List;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class ProductServiceImpl extends ProductServiceGrpc.ProductServiceImplBase {

  private final ProductRepository productRepository;

  public ProductServiceImpl(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public void addProduct(
      AddProductRequest request, StreamObserver<AddProductResponse> responseObserver) {
    com.mycompany.product.grpc.Product grpcProduct = request.getProduct();
    Product product =
        Product.builder()
            .idProduct(grpcProduct.getIdProduct())
            .idSupplier(grpcProduct.getIdSupplier())
            .name(grpcProduct.getName())
            .description(grpcProduct.getDescription())
            .price(grpcProduct.getPrice())
            .category(grpcProduct.getCategory())
            .build();
    productRepository.save(product);

    AddProductResponse response = AddProductResponse.newBuilder().setStatus("SUCCESS").build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void deleteProduct(
      DeleteProductRequest request, StreamObserver<DeleteProductResponse> responseObserver) {
    String supplierId = request.getIdSupplier();
    String productId = request.getIdProduct();
    DeleteProductResponse.Builder responseBuilder = DeleteProductResponse.newBuilder();

    Product product = productRepository.findById(productId).orElse(null);
    if (product != null && product.getIdSupplier().equals(supplierId)) {
      productRepository.delete(product);
      responseBuilder.setStatus("SUCCESS");
    } else {
      responseBuilder.setStatus("FAILURE");
    }
    responseObserver.onNext(responseBuilder.build());
    responseObserver.onCompleted();
  }

  @Override
  public void getAllProducts(
      GetAllProductsRequest request, StreamObserver<GetAllProductsResponse> responseObserver) {
    List<Product> products = productRepository.findAll();
    GetAllProductsResponse.Builder responseBuilder = GetAllProductsResponse.newBuilder();
    products.forEach(
        product -> {
          com.mycompany.product.grpc.Product grpcProduct =
              com.mycompany.product.grpc.Product.newBuilder()
                  .setIdProduct(product.getIdProduct())
                  .setIdSupplier(product.getIdSupplier())
                  .setName(product.getName())
                  .setDescription(product.getDescription())
                  .setPrice(product.getPrice())
                  .setCategory(product.getCategory())
                  .build();
          responseBuilder.addProducts(grpcProduct);
        });
    responseObserver.onNext(responseBuilder.build());
    responseObserver.onCompleted();
  }

  @Override
  public void getProductsByCriteria(
      GetProductsByCriteriaRequest request,
      StreamObserver<GetProductsByCriteriaResponse> responseObserver) {
    String category = request.getCategory();
    String name = request.getName();
    List<Product> products = productRepository.findAll();
    GetProductsByCriteriaResponse.Builder responseBuilder =
        GetProductsByCriteriaResponse.newBuilder();

    products.forEach(
        product -> {
          if (product.getCategory().equalsIgnoreCase(category)
              && product.getName().contains(name)) {
            com.mycompany.product.grpc.Product grpcProduct =
                com.mycompany.product.grpc.Product.newBuilder()
                    .setIdProduct(product.getIdProduct())
                    .setIdSupplier(product.getIdSupplier())
                    .setName(product.getName())
                    .setDescription(product.getDescription())
                    .setPrice(product.getPrice())
                    .setCategory(product.getCategory())
                    .build();
            responseBuilder.addProducts(grpcProduct);
          }
        });
    responseObserver.onNext(responseBuilder.build());
    responseObserver.onCompleted();
  }
}
