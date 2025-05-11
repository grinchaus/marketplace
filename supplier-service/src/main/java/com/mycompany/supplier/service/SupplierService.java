package com.mycompany.supplier.service;

import com.mycompany.supplier.dto.SupplierDto;

public interface SupplierService {
  SupplierDto registerSupplier(SupplierDto supplierDto);

  SupplierDto updateSupplier(Long id, SupplierDto supplierDto);
}
