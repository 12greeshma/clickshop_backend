package com.trivium.service;

import com.trivium.entity.Cart;

import java.util.List;

public interface CartService {

    Cart addToCart(Cart cart);

    List<Cart> getCartItemsByCustomerId(int customerId);

    void clearCartByCustomerId(int customerId);
    
   Cart updateCartItem(int cartId, int quantity);
   Cart updateCartItemByCustomerAndProduct(int customerId, int productId, int quantity);
   void removeCartItemByCustomerAndProduct(int customerId, int productId);
    
}
