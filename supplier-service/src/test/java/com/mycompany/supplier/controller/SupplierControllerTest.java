package com.mycompany.supplier.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.supplier.dto.SupplierDto;
import com.mycompany.supplier.service.SupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class SupplierControllerTest {

  private MockMvc mockMvc;

  @Mock private SupplierService supplierService;

  @InjectMocks private SupplierController supplierController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(supplierController).build();
  }

  @Test
  @DisplayName("Успешная регистрация нового поставщика")
  public void testRegisterSupplier() throws Exception {
    SupplierDto inputDto =
        SupplierDto.builder()
            .email("supplier@example.com")
            .password("password123")
            .companyName("Best Supplier Ltd")
            .build();

    SupplierDto outputDto =
        SupplierDto.builder()
            .id(1L)
            .email("supplier@example.com")
            .companyName("Best Supplier Ltd")
            .build();

    when(supplierService.registerSupplier(any(SupplierDto.class))).thenReturn(outputDto);

    mockMvc
        .perform(
            post("/supplier")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.email").value("supplier@example.com"))
        .andExpect(jsonPath("$.companyName").value("Best Supplier Ltd"));
  }

  @Test
  @DisplayName("Успешное обновление данных поставщика")
  public void testUpdateSupplier() throws Exception {
    Long supplierId = 1L;
    SupplierDto inputDto =
        SupplierDto.builder()
            .email("supplier@example.com")
            .companyName("Updated Supplier Co")
            .password("newpassword")
            .build();

    SupplierDto outputDto =
        SupplierDto.builder()
            .id(supplierId)
            .email("supplier@example.com")
            .companyName("Updated Supplier Co")
            .build();

    when(supplierService.updateSupplier(eq(supplierId), any(SupplierDto.class)))
        .thenReturn(outputDto);

    mockMvc
        .perform(
            put("/supplier/{id}", supplierId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(supplierId))
        .andExpect(jsonPath("$.email").value("supplier@example.com"))
        .andExpect(jsonPath("$.companyName").value("Updated Supplier Co"));
  }
}
