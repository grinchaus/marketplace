package com.mycompany.cart.repository;

import com.mycompany.cart.model.Cart;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends CrudRepository<Cart, String> {
  Optional<Cart> findByUserId(String userId);
}
