package com.trivium.service;

import com.trivium.entity.Cart;
import com.trivium.entity.Order;
import com.trivium.entity.Product;
import com.trivium.repository.CartRepository;
import com.trivium.repository.OrderRepository;
import com.trivium.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private CartRepository cartRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ProductRepository productRepo;

    //  Place order from cart
    @Transactional
    public void placeOrder(int customerId) {
        List<Cart> cartItems = cartRepo.findByCustomerId(customerId);
        double total = 0.0;

        for (Cart item : cartItems) {
            Product product = productRepo.findById(item.getProductId()).orElse(null);
            if (product != null && product.getStock() >= item.getQuantity()) {
                product.setStock(product.getStock() - item.getQuantity());
                productRepo.save(product);

                double baseAmount = item.getPrice() * item.getQuantity();
                double extraCharge = item.getPrice() < 500 ? 50 : 0;
                double finalAmount = baseAmount + extraCharge;

                Order order = new Order();
                order.setCustomerId(customerId);
                order.setTotalAmount(finalAmount); 
                order.setStatus("Pending");
                order.setOrderDate(LocalDateTime.now());
                order.setProductId(item.getProductId());

                orderRepo.save(order);
                total += finalAmount;
            }
        }

        cartRepo.deleteByCustomerId(customerId);
    }

    // Get order history
    public List<Order> getOrdersByCustomerId(int customerId) {
        return orderRepo.findByCustomerId(customerId);
    }
    
    // update order status (admin)
    public boolean updateOrderStatus(int orderId, String newStatus) {
        Order order = orderRepo.findById(orderId).orElse(null);
        
        if (order == null) {
            return false; 
        }

        // Only allow valid status changes
        if (newStatus.equalsIgnoreCase("Shipped") || newStatus.equalsIgnoreCase("Delivered")) {
            order.setStatus(newStatus);
            orderRepo.save(order);
            return true;
        }

        return false; 
    }



}
