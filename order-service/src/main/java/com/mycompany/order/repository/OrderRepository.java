package com.mycompany.order.repository;

import com.mycompany.order.model.Order;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends CrudRepository<Order, String> {
  Optional<Order> findByUserId(String userId);
}
