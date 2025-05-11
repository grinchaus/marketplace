package com.mycompany.supplier.controller;

import com.mycompany.supplier.dto.SupplierDto;
import com.mycompany.supplier.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/supplier")
public class SupplierController {

  private final SupplierService supplierService;

  public SupplierController(SupplierService supplierService) {
    this.supplierService = supplierService;
  }

  @PostMapping
  public ResponseEntity<SupplierDto> registerSupplier(@RequestBody @Valid SupplierDto supplierDto) {
    SupplierDto registeredSupplier = supplierService.registerSupplier(supplierDto);
    return new ResponseEntity<>(registeredSupplier, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<SupplierDto> updateSupplier(
      @PathVariable Long id, @RequestBody @Valid SupplierDto supplierDto) {
    SupplierDto updatedSupplier = supplierService.updateSupplier(id, supplierDto);
    return ResponseEntity.ok(updatedSupplier);
  }
}
