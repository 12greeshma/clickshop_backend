package com.trivium.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trivium.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
	
	long countByStockLessThan(int threshold);

}