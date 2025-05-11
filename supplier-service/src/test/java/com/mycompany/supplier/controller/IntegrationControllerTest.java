package com.mycompany.supplier.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.UnknownFieldSet;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import com.mycompany.productSupplier.AddProductResponse;
import com.mycompany.productSupplier.DeleteProductResponse;
import com.mycompany.productSupplier.Product;
import com.mycompany.supplier.grpc.ProductServiceClient;
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

  @InjectMocks private IntegrationController integrationController;

  private ObjectMapper objectMapper;

  @JsonIgnoreProperties({"defaultInstanceForType", "unknownFields"})
  private abstract static class UnknownFieldSetMixin {}

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new ProtobufModule());
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
  @DisplayName("Успешное добавление продукта через интеграцию")
  public void testAddProduct() throws Exception {
    Product testProduct = Product.newBuilder().setIdProduct("p123").setName("Test Product").build();

    AddProductResponse addResponse = AddProductResponse.newBuilder().setStatus("p123").build();

    when(productServiceClient.addProduct(any(Product.class))).thenReturn(addResponse);

    String productJson = "{\"idProduct\":\"p123\", \"name\":\"Test Product\"}";

    mockMvc
        .perform(
            post("/supplier/integrations/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value("p123"));
  }

  @Test
  @DisplayName("Успешное удаление продукта через интеграцию")
  public void testDeleteProduct() throws Exception {
    String supplierId = "s456";
    String productID = "p123";

    DeleteProductResponse deleteResponse =
        DeleteProductResponse.newBuilder().setStatus("Product deleted successfully").build();

    when(productServiceClient.deleteProduct(eq(supplierId), eq(productID)))
        .thenReturn(deleteResponse);

    mockMvc
        .perform(
            delete("/supplier/integrations/products/{productID}", productID)
                .param("supplierId", supplierId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value("Product deleted successfully"));
  }
}
