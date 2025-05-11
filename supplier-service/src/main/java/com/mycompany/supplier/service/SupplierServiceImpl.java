package com.mycompany.supplier.service;

import com.mycompany.supplier.dto.SupplierDto;
import com.mycompany.supplier.model.Supplier;
import com.mycompany.supplier.repository.SupplierRepository;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SupplierServiceImpl implements SupplierService {

  private final SupplierRepository supplierRepository;
  private final PasswordEncoder passwordEncoder;

  public SupplierServiceImpl(
      SupplierRepository supplierRepository, PasswordEncoder passwordEncoder) {
    this.supplierRepository = supplierRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public SupplierDto registerSupplier(SupplierDto supplierDto) {
    if (supplierRepository.findByEmail(supplierDto.getEmail()).isPresent()) {
      throw new IllegalArgumentException("Supplier with this email already exists");
    }

    String hashedPassword = passwordEncoder.encode(supplierDto.getPassword());

    Supplier supplier =
        Supplier.builder()
            .email(supplierDto.getEmail())
            .hashedPassword(hashedPassword)
            .companyName(supplierDto.getCompanyName())
            .build();

    Supplier savedSupplier = supplierRepository.save(supplier);
    return mapToDto(savedSupplier);
  }

  @Override
  public SupplierDto updateSupplier(Long id, SupplierDto supplierDto) {
    Optional<Supplier> optionalSupplier = supplierRepository.findById(id);
    if (optionalSupplier.isEmpty()) {
      throw new IllegalArgumentException("Supplier with this id does not exist");
    }
    Supplier supplier = optionalSupplier.get();

    supplier.setCompanyName(supplierDto.getCompanyName());

    if (supplierDto.getPassword() != null && !supplierDto.getPassword().isEmpty()) {
      supplier.setHashedPassword(passwordEncoder.encode(supplierDto.getPassword()));
    }
    Supplier updatedSupplier = supplierRepository.save(supplier);
    return mapToDto(updatedSupplier);
  }

  private SupplierDto mapToDto(Supplier supplier) {
    return SupplierDto.builder()
        .id(supplier.getId())
        .email(supplier.getEmail())
        .companyName(supplier.getCompanyName())
        .build();
  }
}
