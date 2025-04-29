package com.trivium.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trivium.entity.Product;
import com.trivium.repository.CustomerRepository;
import com.trivium.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepo;
    @Autowired
    private CustomerRepository customerRepo;
    
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public Product addProduct(Product product) {
        return productRepo.save(product);
    }

    public Product updateProduct(int id, Product updatedProduct) {
        Product product = productRepo.findById(id).orElseThrow();
        product.setName(updatedProduct.getName());
        product.setPrice(updatedProduct.getPrice());
        product.setStock(updatedProduct.getStock());
        product.setDescription(updatedProduct.getDescription());
        return productRepo.save(product);
    }

    public void deleteProduct(int id) {
        productRepo.deleteById(id);
    }
    public Product updateStock(int id, int newStock) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setStock(newStock);
        return productRepo.save(product);
    }
    
    public Product getProductById(int id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
    public Product saveProduct(Product product) {
        return productRepo.save(product);
    }
    
// dashboard analytics
    public long countProducts() {
        return productRepo.count();
    }
    
    public long countProductsWithLowStock(int threshold) {
        return productRepo.countByStockLessThan(threshold);
    }
    
    public long countRegisteredCustomers() {
        return customerRepo.countByRole("ROLE_CUSTOMER");
    }
}
