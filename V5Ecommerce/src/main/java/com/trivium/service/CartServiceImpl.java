package com.trivium.service;

import com.trivium.entity.Cart;
import com.trivium.entity.Product;
import com.trivium.exception.OutOfStockException;
import com.trivium.repository.CartRepository;
import com.trivium.repository.ProductRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepo;

    @Autowired
    private ProductRepository productRepo;

    @Override
    public Cart addToCart(Cart cart) {
        // Validate product existence
        Product product = productRepo.findById(cart.getProductId()).orElse(null);
        if (product == null) {
            throw new RuntimeException("Product not found with ID: " + cart.getProductId());
        }

        if (product.getStock() < cart.getQuantity()) {
            throw new RuntimeException("Not enough stock available. Only " + product.getStock() + " left.");
        }

        if (cart.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        cart.setProductName(product.getName());
        cart.setPrice(product.getPrice());
        cart.setImageUrl(product.getImageUrl());
       
        return cartRepo.save(cart);
    }


    @Override
    public List<Cart> getCartItemsByCustomerId(int customerId) {
        return cartRepo.findByCustomerId(customerId);
    }

    @Override
    public void clearCartByCustomerId(int customerId) {
        cartRepo.deleteByCustomerId(customerId);
    }
    
    @Override
    public Cart updateCartItem(int cartId, int quantity) {
        Cart cart = cartRepo.findById(cartId)
            .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + cartId));

        cart.setQuantity(quantity);
        return cartRepo.save(cart);
    }
    

    @Override
    @Transactional
    public Cart updateCartItemByCustomerAndProduct(int customerId, int productId, int newQuantity) {
        // Fetch the cart item by customerId and productId
        Cart cart = cartRepo.findByCustomerIdAndProductId(customerId, productId)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Get the current quantity in the cart
        int currentQuantity = cart.getQuantity();

        // Fetch the product details
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        // Adjust stock based on the quantity change
        if (newQuantity > currentQuantity) {
            // If the new quantity is greater, check stock availability
            int diff = newQuantity - currentQuantity;
            if (product.getStock() < diff) {
                throw new OutOfStockException("Not enough stock available");
            }
            

            product.setStock(product.getStock() - diff); // Reduce the stock
        } else if (newQuantity < currentQuantity) {
            // If the new quantity is less, return the stock back
            int diff = currentQuantity - newQuantity;
            product.setStock(product.getStock() + diff); // Increase the stock
        }

        // Save the updated product stock
        productRepo.save(product);

        // Update the cart quantity
        cart.setQuantity(newQuantity);
        
        // Save the updated cart
        return cartRepo.save(cart);
    }

    @Override
    @Transactional
    public void removeCartItemByCustomerAndProduct(int customerId, int productId) {
        Cart cartItem = cartRepo.findByCustomerIdAndProductId(customerId, productId)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));

        cartRepo.delete(cartItem);  
    }
}
