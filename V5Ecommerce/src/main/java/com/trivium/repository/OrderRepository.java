package com.trivium.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trivium.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {
	
	List<Order> findByCustomerId(int customerId);
	

}