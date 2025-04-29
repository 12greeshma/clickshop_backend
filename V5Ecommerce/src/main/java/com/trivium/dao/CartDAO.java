package com.trivium.dao;



import com.trivium.entity.Cart;

import java.util.List;

public interface CartDAO {
    Cart save(Cart cart);
    List<Cart> findByCustomerId(int customerId);
    void deleteByCustomerId(int customerId);
}
