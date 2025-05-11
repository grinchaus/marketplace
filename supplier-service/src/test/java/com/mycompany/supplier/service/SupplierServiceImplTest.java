package com.mycompany.supplier.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mycompany.supplier.dto.SupplierDto;
import com.mycompany.supplier.model.Supplier;
import com.mycompany.supplier.repository.SupplierRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class SupplierServiceImplTest {

  @Mock private SupplierRepository supplierRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private SupplierServiceImpl supplierService;

  @Test
  public void testRegisterSupplier_success() {
    SupplierDto supplierDto =
        SupplierDto.builder()
            .email("test@example.com")
            .password("password123")
            .companyName("Test Company")
            .build();

    when(supplierRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
    String hashedPassword = "hashedPassword123";
    when(passwordEncoder.encode("password123")).thenReturn(hashedPassword);

    Supplier supplierToSave =
        Supplier.builder()
            .email("test@example.com")
            .hashedPassword(hashedPassword)
            .companyName("Test Company")
            .build();

    Supplier savedSupplier =
        Supplier.builder()
            .id(1L)
            .email("test@example.com")
            .hashedPassword(hashedPassword)
            .companyName("Test Company")
            .build();
    when(supplierRepository.save(any(Supplier.class))).thenReturn(savedSupplier);

    SupplierDto result = supplierService.registerSupplier(supplierDto);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("test@example.com", result.getEmail());
    assertEquals("Test Company", result.getCompanyName());
    verify(supplierRepository, times(1)).findByEmail("test@example.com");
    verify(passwordEncoder, times(1)).encode("password123");
    verify(supplierRepository, times(1)).save(any(Supplier.class));
  }

  @Test
  public void testRegisterSupplier_existingEmail_throwsException() {
    SupplierDto supplierDto =
        SupplierDto.builder()
            .email("existing@example.com")
            .password("password")
            .companyName("Existing Company")
            .build();

    Supplier existingSupplier =
        Supplier.builder()
            .id(2L)
            .email("existing@example.com")
            .hashedPassword("hashed")
            .companyName("Existing Company")
            .build();

    when(supplierRepository.findByEmail("existing@example.com"))
        .thenReturn(Optional.of(existingSupplier));

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              supplierService.registerSupplier(supplierDto);
            });
    assertEquals("Supplier with this email already exists", exception.getMessage());
    verify(supplierRepository, times(1)).findByEmail("existing@example.com");
    verify(passwordEncoder, never()).encode(anyString());
    verify(supplierRepository, never()).save(any(Supplier.class));
  }

  @Test
  public void testUpdateSupplier_successWithoutPassword() {
    Long supplierId = 1L;
    SupplierDto supplierDto = SupplierDto.builder().companyName("Updated Company").build();
    Supplier existingSupplier =
        Supplier.builder()
            .id(supplierId)
            .email("update@example.com")
            .hashedPassword("oldHashedPassword")
            .companyName("Old Company")
            .build();

    when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(existingSupplier));

    Supplier updatedSupplier =
        Supplier.builder()
            .id(supplierId)
            .email(existingSupplier.getEmail())
            .hashedPassword(existingSupplier.getHashedPassword())
            .companyName("Updated Company")
            .build();
    when(supplierRepository.save(existingSupplier)).thenReturn(updatedSupplier);

    SupplierDto updatedDto = supplierService.updateSupplier(supplierId, supplierDto);

    assertNotNull(updatedDto);
    assertEquals(supplierId, updatedDto.getId());
    assertEquals("update@example.com", updatedDto.getEmail());
    assertEquals("Updated Company", updatedDto.getCompanyName());
    verify(passwordEncoder, never()).encode(anyString());
    verify(supplierRepository, times(1)).save(existingSupplier);
  }

  @Test
  public void testUpdateSupplier_successWithPassword() {
    Long supplierId = 1L;
    SupplierDto supplierDto =
        SupplierDto.builder().companyName("Updated Company").password("newPassword").build();
    Supplier existingSupplier =
        Supplier.builder()
            .id(supplierId)
            .email("updatepass@example.com")
            .hashedPassword("oldHashedPassword")
            .companyName("Old Company")
            .build();

    when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(existingSupplier));
    String newHashedPassword = "newHashedPassword";
    when(passwordEncoder.encode("newPassword")).thenReturn(newHashedPassword);

    Supplier updatedSupplier =
        Supplier.builder()
            .id(supplierId)
            .email(existingSupplier.getEmail())
            .hashedPassword(newHashedPassword)
            .companyName("Updated Company")
            .build();
    when(supplierRepository.save(existingSupplier)).thenReturn(updatedSupplier);

    SupplierDto updatedDto = supplierService.updateSupplier(supplierId, supplierDto);

    assertNotNull(updatedDto);
    assertEquals(supplierId, updatedDto.getId());
    assertEquals("updatepass@example.com", updatedDto.getEmail());
    assertEquals("Updated Company", updatedDto.getCompanyName());
    verify(passwordEncoder, times(1)).encode("newPassword");
    verify(supplierRepository, times(1)).save(existingSupplier);
  }

  @Test
  public void testUpdateSupplier_nonExistingSupplier_throwsException() {
    Long supplierId = 99L;
    SupplierDto supplierDto = SupplierDto.builder().companyName("Some Company").build();

    when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              supplierService.updateSupplier(supplierId, supplierDto);
            });
    assertEquals("Supplier with this id does not exist", exception.getMessage());
    verify(supplierRepository, times(1)).findById(supplierId);
    verify(supplierRepository, never()).save(any(Supplier.class));
  }
}
