package com.trivium.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trivium.entity.Cart;
import com.trivium.entity.Product;

public interface CartRepository extends JpaRepository<Cart, Integer> {
	
	List<Cart> findByCustomerId(int customerId);
    void deleteByCustomerId(int customerId);
   Optional<Cart> findByCustomerIdAndProductId(int customerId, int productId);
  //  public boolean removeCartItemByCustomerAndProduct(int customerId, int productId);
   
   void deleteByCustomerIdAndProductId(int customerId, int productId);
}