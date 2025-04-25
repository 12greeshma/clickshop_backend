package com.trivium.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trivium.dto.CustomerDTO;
import com.trivium.dto.OrderStatusRequest;
import com.trivium.entity.Order;
import com.trivium.entity.Product;
import com.trivium.repository.CustomerRepository;
import com.trivium.repository.OrderRepository;
import com.trivium.service.OrderService;
import com.trivium.service.ProductService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getAdminDashboard() {
        return ResponseEntity.ok("Welcome Admin!");
    }
    
    @Autowired
    private ProductService productService;

    @Autowired
    private OrderRepository orderRepo;
    
    @Autowired
    private CustomerRepository customerRepo;
    
    @Autowired
    private OrderService orderService;

    //  List products
    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    //  Add product
    @PostMapping("/products")
    public Product addProduct(@RequestBody Product product) {
        return productService.addProduct(product);
    }

    @PutMapping("/products/{id}/stock")
    public ResponseEntity<Product> updateProductStock(@PathVariable int id, @RequestBody Map<String, Integer> stockUpdate) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        int newStock = stockUpdate.get("stock");
        product.setStock(newStock);

        Product updatedProduct = productService.saveProduct(product); 
        return ResponseEntity.ok(updatedProduct);
    }


    //  Delete product
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    //  View all orders
    @GetMapping("/orders")
    public List<Order> viewOrders() {
        return orderRepo.findAll();
    }
    
    
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        long totalProducts = productService.countProducts();
        long lowStockProducts = productService.countProductsWithLowStock(5);
        long totalOrders = orderRepo.count();
        long totalCustomers = productService.countRegisteredCustomers(); // You can rename appropriately

        Map<String, Object> analytics = Map.of(
            "totalProducts", totalProducts,
            "lowStockProducts", lowStockProducts,
            "totalOrders", totalOrders,
            "totalCustomers", totalCustomers
        );

        return ResponseEntity.ok(analytics);
    }
    
    
 // Add this method to AdminController
    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CustomerDTO>> getAllCustomersBasicInfo() {
        List<CustomerDTO> customerList = customerRepo.findAll().stream()
            .map(c -> new CustomerDTO(c.getId(), c.getName(), c.getEmail(), c.getMobileNumber()))
            .collect(Collectors.toList());

        return ResponseEntity.ok(customerList);
    }

    
    @PutMapping("/orders/{orderId}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable int orderId,
            @RequestBody OrderStatusRequest statusRequest) {
        
        String status = statusRequest.getStatus();
        boolean updated = orderService.updateOrderStatus(orderId, status);
        
        if (updated) {
            return ResponseEntity.ok("Order status updated to " + status);
        } else {
            return ResponseEntity.badRequest().body("Invalid status or order not found");
        }
    }


}
