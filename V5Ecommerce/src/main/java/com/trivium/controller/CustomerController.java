package com.trivium.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import com.trivium.dto.OtpVerificationRequest;
import com.trivium.entity.Cart;
import com.trivium.entity.Customer;
import com.trivium.entity.Order;
import com.trivium.entity.Product;
import com.trivium.repository.CartRepository;
import com.trivium.repository.CustomerRepository;
import com.trivium.repository.ProductRepository;
import com.trivium.service.CartService;
import com.trivium.service.OrderService;
import com.trivium.service.ProductService;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepo;
    
    @Autowired
    private ProductRepository productRepo;
    
    @Autowired
    private CartRepository cartRepo;
    
    // Test endpoint
    @GetMapping("/hello")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String helloCustomer() {
        return "Hello, Customer!";
    }
    
    
   


    //  View all products
    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }
    
    
    @PutMapping("/cart/update")
    public ResponseEntity<Cart> updateCartItem(
            Authentication authentication,
            @RequestBody Cart cartDetails) {

        String email = authentication.getName();
        Customer customer = customerRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        Cart updatedCart = cartService.updateCartItemByCustomerAndProduct(
            customer.getId(),
            cartDetails.getProductId(),
            cartDetails.getQuantity()
        );

        return ResponseEntity.ok(updatedCart);
    }


//    @PostMapping("/cart/add")
//    public Cart addToCart(Authentication authentication, @RequestBody Cart cart) {
//        // Extract email from authenticated user
//        String email = authentication.getName();
//
//        // Lookup customer by email
//        Customer customer = customerRepo.findByEmail(email)
//            .orElseThrow(() -> new RuntimeException("Customer not found"));
//
//        // Set customerId from the authenticated customer
//        cart.setCustomerId(customer.getId());
//
//        return cartService.addToCart(cart);
//    }
    
    @PostMapping("/cart/add")
    public Cart addToCart(Authentication authentication, @RequestBody Cart cart) {
        // Extract email from authenticated user
        String email = authentication.getName();

        // Lookup customer by email
        Customer customer = customerRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Set customerId from the authenticated customer
        cart.setCustomerId(customer.getId());

        // ✅ Fetch the product being added to the cart
        Product product = productRepo.findById(cart.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));

        // ✅ Check if the product is in stock
        if (product.getStock() <= 0) {
            throw new RuntimeException("Product is out of stock");
        }

        // ✅ Check if the product already exists in the cart for this customer
        Cart existingCartItem = cartRepo.findByCustomerIdAndProductId(customer.getId(), cart.getProductId())
            .orElse(null); // null if not found

        if (existingCartItem != null) {
            // If product already in cart, update the quantity
            int updatedQuantity = existingCartItem.getQuantity() + cart.getQuantity();
            existingCartItem.setQuantity(updatedQuantity);

            // Update the stock by the quantity being added to the cart
            product.setStock(product.getStock() - cart.getQuantity());
            productRepo.save(product); // Save the updated product stock

            return cartRepo.save(existingCartItem); // Save the updated cart item
        } else {
            // Otherwise, add a new item to the cart
            product.setStock(product.getStock() - cart.getQuantity()); // Decrease stock
            productRepo.save(product); // Save the updated stock

            return cartService.addToCart(cart); 
        }
    }



    @GetMapping("/cart")
    public List<Cart> getCartItems(Authentication authentication) {
        String email = authentication.getName(); // from JWT
        Customer customer = customerRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return cartService.getCartItemsByCustomerId(customer.getId());
    }


    @PostMapping("/place-order")
    public ResponseEntity<Map<String, String>> placeOrder(Authentication authentication) {
        String email = authentication.getName();

        Customer customer = customerRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        orderService.placeOrder(customer.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Order placed successfully");

        return ResponseEntity.ok(response);
    }

    
    @DeleteMapping("/cart/remove/{productId}")
    public ResponseEntity<List<Cart>> removeCartItem(Authentication authentication, @PathVariable int productId) {
        String email = authentication.getName();
        Customer customer = customerRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        try {
            // Remove the item from the cart
            cartService.removeCartItemByCustomerAndProduct(customer.getId(), productId);

            // Fetch the updated cart
            List<Cart> updatedCartItems = cartService.getCartItemsByCustomerId(customer.getId());

            // Return the updated cart as the response
            return ResponseEntity.ok(updatedCartItems);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<Order>> getOrders(Authentication authentication) {
        String email = authentication.getName();
        Customer customer = customerRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<Order> orders = orderService.getOrdersByCustomerId(customer.getId());

        return ResponseEntity.ok(orders);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/products/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable int productId) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        return ResponseEntity.ok(product);
    }




}
