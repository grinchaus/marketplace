package com.mycompany.supplier.repository;

import com.mycompany.supplier.model.Supplier;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
  Optional<Supplier> findByEmail(String email);
}
